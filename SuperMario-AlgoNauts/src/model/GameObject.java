package model;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class GameObject {
    private BufferedImage style;
    private Dimension dimension;

    private double x, y;
    private double velX, velY;
    private final double gravityAcc;
    private boolean falling, jumping;

    public GameObject(double x, double y, BufferedImage style) {
        setLocation(x, y);
        setStyle(style);

        if (style != null) setDimension(style.getHeight(), style.getWidth());

        setVelX(0);
        setVelY(0);
        gravityAcc = 0.38;
        setFalling(true);
        setJumping(false);
    }

    public void drawObject(Graphics2D g2D) {
        g2D.drawImage(style, (int) x, (int) y, null);
    }

    public void updateLocation() {
        if ((isFalling() || isJumping()) && velY > -16) velY -= gravityAcc;
        y -= velY;

        if (velY < 0) {
            falling = true;
            jumping = false;
        } else if (velY > 0) {
            jumping = true;
            falling = false;
        } else {
            jumping = false;
            falling = false;
        }

        x += velX;
    }


    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(int height, int width) {
        this.dimension = new Dimension(width, height);
    }

    public BufferedImage getStyle() {
        return style;
    }

    public void setStyle(BufferedImage style) {
        this.style = style;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setLocation(double x, double y) {
        setX(x);
        setY(y);
    }

    public double getVelX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public double getVelY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    public boolean isJumping() {
        return jumping;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, dimension.width, dimension.height);
    }

    public Rectangle getHorizontalBounds() {
        double bx = x + velX;
        double by = y - 4;
        double bw = dimension.width + velX / 2;
        double bh = dimension.height;

        return new Rectangle((int) bx, (int) by, (int) bw, (int) bh);
    }

    public Rectangle getVerticalBounds() {
        double bx = x;
        double by = y - velY;
        double bw = dimension.width;
        double bh = dimension.height + velY / 3 + 2;

        return new Rectangle((int) bx, (int) by, (int) bw, (int) bh);
    }
}
