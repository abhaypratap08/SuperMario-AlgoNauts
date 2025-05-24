package model;

import model.boost.Boost;
import model.boost.BoostType;
import model.brick.Brick;
import model.enemy.Enemy;
import model.hero.Fireball;
import model.hero.Mario;
import utils.ImageImporter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Map {
    private final BufferedImage backgroundImage;

    private int coins, lives, points, time;

    private Mario mario;
    private Mario netMario;
    private EndFlag endPoint;

    private final ArrayList<Boost> boosts = new ArrayList<>();
    private final ArrayList<Brick> bricks = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Fireball> fireballs = new ArrayList<>();

    public Map() {
        backgroundImage = ImageImporter.loadImage("background");

        coins = 0;
        lives = 3;
        points = 0;
        time = 120;
    }

    public void drawMap(Graphics2D g2D) {
        drawBackground(g2D);

        mario.drawObject(g2D);
        if (netMario != null) netMario.drawObject(g2D);
        endPoint.drawObject(g2D);

        drawBoosts(g2D);
        drawBricks(g2D);
        drawEnemies(g2D);
        drawFireballs(g2D);
    }

    public void drawBackground(Graphics2D g2D) {
        g2D.drawImage(backgroundImage, 0, 0, null);
    }


    private void drawBoosts(Graphics2D g2D) {
        for (int i = 0; i < boosts.size(); i++) {
            Boost boost = boosts.get(i);
            if (boost.getBoostType() == BoostType.COIN && boost.getVelY() == 0) boosts.remove(boost);
            else boost.drawObject(g2D);
        }
    }

    private void drawBricks(Graphics2D g2D) {
        for (Brick brick : bricks) g2D.drawImage(brick.getStyle(), (int) brick.getX(), (int) brick.getY(), null);
    }

    private void drawEnemies(Graphics2D g2D) {
        for (Enemy enemy : enemies) enemy.drawObject(g2D);
    }


    private void drawFireballs(Graphics2D g2D) {
        for (Fireball fireball : fireballs) fireball.drawObject(g2D);
    }


    public int getBrickIndex(int x, int y) {
        for (int i = 0; i < bricks.size(); i++)
            if (bricks.get(i).getX() == x && bricks.get(i).getY() == y) return i;
        return -1;
    }

    public void updateLocations() {
        mario.updateLocation();
        if (netMario != null) netMario.updateLocation();

        for (Boost boost : boosts) boost.updateLocation();
        for (Enemy enemy : enemies) enemy.updateLocation();
        for (Fireball fireball : fireballs) fireball.updateLocation();

        endPoint.updateLocation();
    }



    public ArrayList<Boost> getBoosts() {
        return boosts;
    }

    public ArrayList<Brick> getBricks() {
        return bricks;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public EndFlag getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(EndFlag endPoint) {
        this.endPoint = endPoint;
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public ArrayList<Fireball> getFireballs() {
        return fireballs;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public Mario getMario() {
        return mario;
    }

    public void setMario(Mario mario) {
        this.mario = mario;
    }

    public Mario getNetMario() {
        return netMario;
    }

    public void setNetMario(Mario netMario) {
        this.netMario = netMario;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
