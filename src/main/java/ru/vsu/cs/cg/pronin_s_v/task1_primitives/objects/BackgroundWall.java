package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;


public class BackgroundWall implements Static {
    private final int width, height;

    private static final Color BASE_COLOR = new Color(245, 239, 232);
    private static final Color DOT_COLOR  = new Color(228, 218, 206);
    private static final int STEP = 40;
    private static final int DOT_SIZE = 12;


    public BackgroundWall(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(BASE_COLOR);
        g2.fillRect(0, 0, width, height);

        g2.setColor(DOT_COLOR);
        for (int y = 20; y < height; y += STEP) {
            for (int x = 20; x < width; x += STEP) {
                g2.fillRoundRect(x, y, DOT_SIZE, DOT_SIZE, 3, 3);
            }
        }
    }


}