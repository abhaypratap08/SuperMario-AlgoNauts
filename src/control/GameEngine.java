package control;

import model.Map;
import model.enemy.Enemy;
import model.enemy.Goomba;
import model.enemy.Koopa;
import model.hero.Mario;
import net.local.Client;
import net.local.Server;
import net.web.WebServer;
import utils.ImageImporter;
import view.UIManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * The core class of the program. It's responsible for handling the
 * initialization and synchronization of the other threads. It also
 * provides some runtime checks that make up the whole game's brain.
 *
 * @version 1.5.0
 */
public class GameEngine implements Runnable {
    private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public final static int HEIGHT = 720; // Height is fixed because of the map's size
    public final static int WIDTH = ((int) screenSize.getWidth()) - 80;

    private final Camera camera;
    private final InputManager inputManager;
    private final MapManager mapManager;
    private final SoundManager soundManager;
    private final UIManager uiManager;
    private final WebServer webServer;

    private Client client;
    private Server server;

    private GameStatus gameStatus;
    private boolean isRunning;
    private Thread thread;

    public GameEngine(URL serverUrl) {
        camera = new Camera();
        inputManager = new InputManager(this);
        mapManager = new MapManager();
        soundManager = new SoundManager();
        uiManager = new UIManager(this, HEIGHT, WIDTH);
        webServer = new WebServer(serverUrl);

        gameStatus = GameStatus.START_SCREEN;

        // Prepare the frame
        JFrame frame = new JFrame("Super Mario Bros");
        frame.setIconImage(ImageImporter.loadLogo());
        frame.add(uiManager);
        frame.pack();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        frame.addKeyListener(inputManager);

        // Start the thread execution
        start();
    }

    /**
     * The initializer method of the game engine,
     * which calls the {@link Thread#start()} method.
     */
    private void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * The implementation of the {@link Runnable} interface.
     */
    @Override
    public void run() {
        double amountOfTicks = 60.0;

        // Initialize time-counter variables
        long lastTime = System.nanoTime();
        double delta = 0;

        long lastTimeCheck = 0, lastTimeFireball = 0, lastTimeInvincible = 0, lastTimeStar = 0;

        while (isRunning && !thread.isInterrupted()) {
            // Calculate the ticks since the last repaint
            long now = System.nanoTime();
            delta += (now - lastTime) / (1_000_000_000 / amountOfTicks);
            lastTime = now;

            // Convert the time into milliseconds and seconds
            // for easy-use in the time-checker functions.
            long currentMillis = now / 100_000_000;
            long currentSeconds = now / 1_000_000_000;

            if (gameStatus == GameStatus.RUNNING) {
                Mario mario = mapManager.getMap().getMario();

                // Checks for Mario's invincibility
                if (mario.isInvincible()) {
                    if (lastTimeInvincible == 0) lastTimeInvincible = currentSeconds;
                    if (currentSeconds - lastTimeInvincible >= 2) {
                        mario.setInvincible(false);
                        lastTimeInvincible = 0;
                    }
                }

                // Prevents the spamming of fireballs
                if (mario.isFiring()) {
                    if (lastTimeFireball == 0) lastTimeFireball = currentMillis;
                    if (currentMillis - lastTimeFireball >= 2) {
                        mario.fire(mapManager);
                        mario.setFiring(false);
                        lastTimeFireball = 0;
                    }
                }

                // Checks for Mario's previous state after the star state ends
                if (mario.isBabyStar() || mario.isStar()) {
                    if (lastTimeStar == 0) lastTimeStar = currentSeconds;
                    if (currentSeconds - lastTimeStar >= 10) {
                        if (mario.isBabyStar()) mario.setMarioSmall();
                        else if (mario.isFire()) mario.setMarioFire();
                        else if (mario.isSuper()) mario.setMarioSuper();
                        lastTimeStar = 0;
                    }
                }

                // Decreases the time remaining every second
                int time = mapManager.getMap().getTime();
                if (lastTimeCheck == 0) lastTimeCheck = currentSeconds;
                if (currentSeconds - lastTimeCheck >= 1) {
                    if (!mapManager.getMap().getEndPoint().isTouched()) mapManager.getMap().setTime(time - 1);
                    lastTimeCheck = 0;
                }
                if (time == 0) gameStatus = GameStatus.OUT_OF_TIME;
            }

            // Multiplayer threads logic
            if (gameStatus == GameStatus.MULTIPLAYER_HOST) {
                if (server == null || server.isInterrupted())
                    server = new Server(this);
            }
            if (gameStatus == GameStatus.MULTIPLAYER_JOIN) {
                if (client == null || client.isInterrupted())
                    client = new Client(this, uiManager.getMultiplayerMenu().getServerIp());
            }

            // Repaint based on the ticks passed since the last iteration
            while (delta > 0) {
                if (gameStatus == GameStatus.RUNNING) gameLoop();

                render();
                delta--;
            }
        }
    }

    /**
     * Calculates the points rewarded for hitting the final
     * flag based on the height. The higher, the better.
     * It also sums the points for the coins and the time.
     */
    private void calculateEndPoints() {
        Map map = mapManager.getMap();
        int points = map.getPoints();

        int heightPoints = (int) (6837.5 - (map.getMario().getY() * 612.5 / 48));
        points += Math.min(heightPoints, 5000);

        points += map.getCoins() * 100;
        points += Math.max(map.getTime() * 50, 1000);

        map.setPoints(points);
    }

    /**
     * Checks if mario has passed the finish flag and
     * plays the winning animation, as well as calculating
     * the total points collected at the end of the run.
     */
    private void checkEndContact() {
        Mario mario = mapManager.getMap().getMario();
        if (mario.getX() >= ((48 * 198) - 20) && mario.getX() < 10992) {
            if (!mapManager.getMap().getEndPoint().isTouched()) {
                mapManager.getMap().getEndPoint().setTouched(true);
                mario.setX((48 * 198) - 20);
                mario.setVelX(2.5);
                GameEngine.playSound("flag");

                calculateEndPoints();
                if (!mapManager.getHasCheated())
                    webServer.publishScore(mapManager.getScore());
            }
        }

        if (mario.getX() > 9792 && mario.getX() < 10992) {
            mario.setVelX(0);
            mario.setX(9792);
            mario.jump();
        }

        if (mario.getX() == 9792 && !mario.isJumping() && !mario.isFalling())
            gameStatus = GameStatus.MISSION_PASSED;
    }

    /**
     * Creates the selected map and sets
     * {@link GameStatus#RUNNING}.
     *
     * @param mapName The name of the map to be loaded.
     */
    public void createMap(String mapName, boolean isMultiplayer) {
        camera.moveCam(-camera.getX(), -camera.getY());
        mapManager.createMap(mapName, isMultiplayer);
        soundManager.restartTheme();
        setGameStatus(GameStatus.RUNNING);
    }

    /**
     * Draws the Map calling {@link Map#drawMap(Graphics2D)}.
     *
     * @param g2D The graphics engine drawing the map.
     */
    public void drawMap(Graphics2D g2D) {
        mapManager.drawMap(g2D);
    }

    /**
     * Runs the game until the game is over or the player dies.
     * Contains all the game logic and movement checks.
     */
    private void gameLoop() {
        updateCamera();
        updateCollisions();
        updateLocations();

        checkEndContact();

        Mario mario = mapManager.getMap().getMario();
        if (client != null && !client.isInterrupted()) client.sendUpdate(mario);
        if (server != null && !server.isInterrupted()) server.sendUpdate(mario);
    }

    /**
     * Pauses the theme song by calling {@link SoundManager#pauseTheme()}.
     */
    public void pauseTheme() {
        soundManager.pauseTheme();
    }

    /**
     * Plays a specific sound by the given name.
     *
     * @param soundName The name of the sound to be played.
     */
    public static void playSound(String soundName) {
        SoundManager.playSound(soundName);
    }

    /**
     * Handles all the input received by the player. It checks for the
     * game state and currently selected screen to determine which action
     * needs to be executed by the {@link GameEngine}.
     *
     * @param input The inputted key-press.
     */
    public void receiveInput(ButtonAction input) {
        if (gameStatus == GameStatus.START_SCREEN) {
            if (input == ButtonAction.SELECTION_DOWN || input == ButtonAction.SELECTION_UP) uiManager.changeSelectedAction(input);
            if (input == ButtonAction.ENTER) uiManager.confirmSelectedAction();
            if (input == ButtonAction.ESCAPE) System.exit(0);
        } else if (gameStatus == GameStatus.MULTIPLAYER_LOBBY) {
            if (input == ButtonAction.SELECTION_DOWN)
                if (uiManager.getMultiplayerMenu().validateServerIp()) gameStatus = GameStatus.MULTIPLAYER_JOIN;
            if (input == ButtonAction.SELECTION_UP) gameStatus = GameStatus.MULTIPLAYER_HOST;
            if (input == ButtonAction.ESCAPE) gameStatus = GameStatus.START_SCREEN;
        } else if (gameStatus == GameStatus.LEADERBOARDS) {
            if (input == ButtonAction.SELECTION_DOWN || input == ButtonAction.SELECTION_UP) uiManager.changeSelectedAction(input);
            if (input == ButtonAction.ENTER || input == ButtonAction.ESCAPE) gameStatus = GameStatus.START_SCREEN;
        } else if (gameStatus == GameStatus.CREDITS_SCREEN) {
            if (input == ButtonAction.ENTER || input == ButtonAction.ESCAPE) gameStatus = GameStatus.START_SCREEN;
        } else if (gameStatus == GameStatus.USERNAME_SCREEN) {
            if (input == ButtonAction.ENTER) createMap("map-01", false);
            if (input == ButtonAction.ESCAPE) gameStatus = GameStatus.START_SCREEN;
        } else if (gameStatus == GameStatus.RUNNING) {
            if (mapManager.getMap().getEndPoint().isTouched()) return;

            Mario mario = mapManager.getMap().getMario();

            if (input == ButtonAction.ACTION_COMPLETED) mario.setVelX(0);

            if (input == ButtonAction.CROUCH) {
                if (mario.getX() >= 2736 && mario.getX() <= 2784 && !mario.isFalling() && !mario.isJumping()) {
                    camera.setX(10992 + ((1920 - WIDTH) / 2));
                    mario.pipeTeleport(11616, 96);
                }
            }
            if (input == ButtonAction.JUMP) mario.jump();
            if (input == ButtonAction.M_LEFT) mario.move(false, camera);
            if (input == ButtonAction.M_RIGHT) mario.move(true, camera);

            if (input == ButtonAction.CHEAT && mario.getVelX() >= 0) {
                mario.setVelX(100);
                mapManager.setHasCheated(true);
            }
            if (input == ButtonAction.FIRE && mario.isFire()) mario.setFiring(true);
            if (input == ButtonAction.RUN) {
                if (mario.getVelX() > 0) mario.setVelX(7.5);
                if (mario.getVelX() < 0) mario.setVelX(-7.5);
            }

            if (input == ButtonAction.ESCAPE) gameStatus = GameStatus.START_SCREEN;
        } else if (gameStatus == GameStatus.GAME_OVER || gameStatus == GameStatus.MISSION_PASSED || gameStatus == GameStatus.OUT_OF_TIME) {
            if (input == ButtonAction.ENTER) createMap("map-01", false);
            if (input == ButtonAction.ESCAPE) gameStatus = GameStatus.START_SCREEN;

            if (client != null && !client.isInterrupted()) client.interrupt();
            if (server != null && !server.isInterrupted()) server.interrupt();
        }
    }

    /**
     * Handles the input on the multiplayer screen, only
     * relative to setting the server ip. The other inputs
     * are processed by {@link GameEngine#receiveInput}.
     *
     * @param character The character that has been pressed.
     */
    public void receiveIpInput(String character) {
        String serverIp = uiManager.getMultiplayerMenu().getServerIp();

        if (character.equals("\b")) {
            if (!serverIp.isEmpty() && !serverIp.equals("Start typing.")) {
                serverIp = serverIp.substring(0, serverIp.length() - 1);
                if (serverIp.isEmpty()) serverIp = "Start typing.";
            }
        }
        else if (serverIp.equals("Start typing.") || serverIp.equals("Invalid ip.")) serverIp = character;
        else serverIp += character;

        if (serverIp.length() <= 15) uiManager.getMultiplayerMenu().setServerIp(serverIp);
    }

    /**
     * Handles the input on the username screen, only
     * relative to setting the player's name. The other inputs
     * are processed by {@link GameEngine#receiveInput}.
     *
     * @param character The character that has been pressed.
     */
    public void receiveUsernameInput(String character) {
        String username = mapManager.getUsername();

        if (character.equals("\b")) {
            if (!username.isEmpty() && !username.equals("Player1")) {
                username = username.substring(0, username.length() - 1);
                if (username.isEmpty()) username = "Player1";
            }
        }
        else if (username.equals("Player1")) username = character;
        else username += character;

        if (username.length() <= 16) mapManager.setUsername(username);
    }

    /**
     * Renders the current frame by repainting the {@link JFrame}.
     */
    private void render() {
        uiManager.repaint();
    }

    /**
     * Updates the camera position based on the player's movement.
     * It also prevents the player from going back past the camera view.
     */
    private void updateCamera() {
        if (mapManager.getMap().getMario().getX() >= ((48 * 198) - 20)) return;

        Mario mario = mapManager.getMap().getMario();
        int marioVelX = (int) mario.getVelX();

        int shiftAmount = 0;
        if (marioVelX > 0 && mario.getX() - 600 > camera.getX()) shiftAmount = marioVelX;

        camera.moveCam(shiftAmount, 0);

        // Also provide a check if mario goes out of the camera
        if (camera.getX() > mario.getX() - 96) {
            mario.setVelX(0);
            mario.setX(camera.getX() + 96);
        }
    }

    /**
     * Check for all entity collisions with other entities or
     * blocks with {@link MapManager#checkCollisions(GameEngine)}.
     */
    private void updateCollisions() {
        mapManager.checkCollisions(this);
    }

    /**
     * Updates all entity/tiles locations
     * with {@link Map#updateLocations()}.
     */
    private void updateLocations() {
        Mario mario = mapManager.getMap().getMario();

        // Let the enemies move when they enter the screen
        for (Enemy enemy : mapManager.getMap().getEnemies()) {
            boolean enemyInView = enemy.getX() < camera.getX() + control.GameEngine.WIDTH;
            if (enemyInView && mario.getX() < 10992 && enemy.getVelX() == 0) {
                if (enemy instanceof Goomba) enemy.setVelX(-3);
                if (enemy instanceof Koopa) enemy.setVelX(-4);
            }
        }

        // Mario's teleportation out of the secret room
        if (mario.getX() >= 12140 && mario.getY() >= (528 - 24)) {
            camera.setX(7848 - 600);
            mario.pipeTeleport(7848, 384);
        }

        mapManager.updateLocations();
    }

    /* ---------- Getters / Setters ---------- */

    public Point getCameraPosition() {
        return new Point(camera.getX(), camera.getY());
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public WebServer getWebServer() {
        return webServer;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
