package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Простая рыбка: плавает в пределах клипа, отражается от границ,
 * лёгкое покачивание хвоста (sin).
 */
public class Fish implements Static, Dynamic {
    private double x, y;
    private double vx, vy;
    private final double speedMin = 60;
    private final double speedMax = 140; // px/s
    private final int bodyW, bodyH;
    private double t; // время для анимации хвоста
    private final Color bodyColor;
    private final Color finColor;

    public Fish(double x, double y, int bodyW, int bodyH, Color bodyColor, Color finColor) {
        this.x = x;
        this.y = y;
        this.bodyW = bodyW;
        this.bodyH = bodyH;
        this.bodyColor = bodyColor;
        this.finColor = finColor;

        double sp = ThreadLocalRandom.current().nextDouble(speedMin, speedMax);
        double ang = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        this.vx = sp * Math.cos(ang);
        this.vy = sp * Math.sin(ang);
    }

    @Override
    public void update(double dt) {
        t += dt;

        x += vx * dt;
        y += vy * dt;

        // границы берём из текущего Graphics clip в GamePanel (там делаем clipRect),
        // здесь просто отразимся по грубому AABB, чтобы рыбка не «улетала».
        // Эти границы прокидываются при апдейте через setBounds (см. ниже перегрузку).
    }

    public void update(double dt, Rectangle bounds) {
        update(dt);
        // учтём размер тела, чтобы не «вылазить»
        int pad = 6;
        int w2 = bodyW / 2 + pad;
        int h2 = bodyH / 2 + pad;

        if (x - w2 < bounds.x) { x = bounds.x + w2; vx = Math.abs(vx); }
        if (x + w2 > bounds.x + bounds.width) { x = bounds.x + bounds.width - w2; vx = -Math.abs(vx); }

        if (y - h2 < bounds.y) { y = bounds.y + h2; vy = Math.abs(vy); }
        if (y + h2 > bounds.y + bounds.height) { y = bounds.y + bounds.height - h2; vy = -Math.abs(vy); }
    }

    @Override
    public void draw(Graphics2D g2) {
        // направление по скорости
        double angle = Math.atan2(vy, vx);

        AffineTransform oldTx = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle);

        // тело
        g2.setColor(bodyColor);
        g2.fillOval(-bodyW/2, -bodyH/2, bodyW, bodyH);

        // хвост (треугольник), лёгкое махание по синусу
        int tailW = bodyW/3;
        int tailH = bodyH/2;
        int sway = (int) (Math.sin(t * 6) * (bodyH * 0.15)); // покачивание
        Polygon tail = new Polygon(
                new int[]{-bodyW/2, -bodyW/2 - tailW, -bodyW/2},
                new int[]{-tailH/2 + sway, 0 + sway, tailH/2 + sway},
                3
        );
        g2.setColor(finColor);
        g2.fill(tail);

        // глаз
        g2.setColor(Color.white);
        g2.fillOval(bodyW/6, -bodyH/4, bodyH/5, bodyH/5);
        g2.setColor(Color.black);
        g2.fillOval(bodyW/6 + bodyH/10, -bodyH/4 + bodyH/10, bodyH/10, bodyH/10);

        g2.setTransform(oldTx);
    }
}