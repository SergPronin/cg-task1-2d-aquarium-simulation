package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Морская звезда, прилипшая к стеклу.
 * Мягко "дышит" (пульсирует размером) и чуть покачивается.
 */
public class Starfish implements Static, Dynamic {
    private final int centerX;
    private final int centerY;
    private final int size;
    private final Color mainColor;
    private final Color edgeColor;

    private double t = 0.0;
    private final double pulseSpeed;
    private final double wobbleSpeed;

    public Starfish(int centerX, int centerY, int size, Color mainColor, Color edgeColor) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
        this.mainColor = mainColor;
        this.edgeColor = edgeColor;

        this.pulseSpeed = 1.5 + Math.random() * 0.7;
        this.wobbleSpeed = 0.8 + Math.random() * 0.4;
    }

    @Override
    public void update(double dt) {
        t += dt;
    }

    @Override
    public void draw(Graphics2D g2) {
        double pulse = 1.0 + 0.08 * Math.sin(t * pulseSpeed);
        double wobble = 0.05 * Math.sin(t * wobbleSpeed);

        int points = 5;
        double innerRadius = size * 0.4;
        double outerRadius = size * 0.9 * pulse;

        Path2D path = new Path2D.Double();
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI / points * i + wobble;
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = centerX + Math.cos(angle) * r;
            double y = centerY + Math.sin(angle) * r;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();

        g2.setColor(mainColor);
        g2.fill(path);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(edgeColor);
        g2.draw(path);

        // маленькие светлые точки
        g2.setColor(new Color(255, 255, 255, 80));
        for (int i = 0; i < 5; i++) {
            double angle = (2 * Math.PI / 5) * i;
            int dotX = (int) (centerX + Math.cos(angle) * (innerRadius * 0.7));
            int dotY = (int) (centerY + Math.sin(angle) * (innerRadius * 0.7));
            g2.fillOval(dotX - 3, dotY - 3, 6, 6);
        }
    }
}