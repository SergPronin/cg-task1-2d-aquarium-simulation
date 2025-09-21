package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Камень.
 */
public class Stone implements Static {
    private final int centerX;
    private final int centerY;
    private final int width;
    private final int height;
    private final Color baseColor;

    public Stone(int centerX, int centerY, int width, int height, Color baseColor) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width   = width;
        this.height  = height;
        this.baseColor = baseColor;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Тень под камнем
        Shape shadow = new Ellipse2D.Double(
                centerX - width * 0.45,
                centerY + height * 0.15,
                width * 0.9,
                height * 0.35
        );
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(20, 20, 20, 180));
        g2.fill(shadow);
        g2.setComposite(oldComp);

        // Основное тело камня
        Shape stoneShape = new Ellipse2D.Double(centerX - width / 2.0, centerY - height / 2.0, width, height);
        g2.setPaint(baseColor);
        g2.fill(stoneShape);

        // Блик сверху
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.14f));
        g2.setPaint(new GradientPaint(
                (float) centerX, (float) (centerY - height / 2.0),
                new Color(255, 255, 255, 180),
                (float) centerX, (float) (centerY + height / 3.0),
                new Color(255, 255, 255,   0)
        ));
        g2.fill(stoneShape);
        g2.setComposite(oldComp);

        // Лёгкая обводка
        g2.setStroke(new BasicStroke(1.1f));
        g2.setColor(new Color(0, 0, 0, 35));
        g2.draw(stoneShape);
    }
}