package model.enemy;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Koopa extends Enemy {
    private BufferedImage rightImage;

    public Koopa(double x, double y, BufferedImage style) {
        super(x, y, style);
        setVelX(0);
    }

    @Override
    public void drawObject(Graphics2D g2D) {
        if (getVelX() > 0) g2D.drawImage(rightImage, (int) getX(), (int) getY(), null);
        else super.drawObject(g2D);
    }


    public void setRightImage(BufferedImage rightImage) {
        this.rightImage = rightImage;
    }
}
