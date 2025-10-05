package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/** Простой пузырёк: поднимается, слегка «шатает» по синусу, постепенно растворяется. */
public class Bubble implements Dynamic, Static {
    private double x, y;
    private double r;
    private double vy;      // скорость подъёма (px/s)
    private double t;       // время жизни (s)
    private double ttl;     // максимальная длительность (s)
    private double wobble;  // амплитуда покачивания
    private double freq;    // частота покачивания

    private boolean dead = false;

    public Bubble(double x, double y) {
        this.x = x;
        this.y = y;
        this.r = 5 + Math.random() * 3;            // радиус 5..8
        this.vy = -40 - Math.random() * 25;        // поднимается 40..65 px/s
        this.ttl = 2.5 + Math.random() * 1.2;      // живёт 2.5..3.7 s
        this.wobble = 6 + Math.random() * 6;       // качание 6..12
        this.freq = 4 + Math.random() * 2;         // частота 4..6 Гц
    }

    @Override
    public void update(double dt) {
        if (dead) return;
        t += dt;
        if (t >= ttl) {
            dead = true;
            return;
        }
        // подъём
        y += vy * dt;
        // лёгкое уменьшение радиуса
        r = Math.max(1.5, r - 0.25 * dt * 10);
    }

    @Override
    public void draw(Graphics2D g2) {
        if (dead) return;

        double dx = Math.sin(t * 2 * Math.PI * freq) * wobble * 0.15;
        double cx = x + dx;
        double cy = y;

        // прозрачность к концу жизни
        float alpha = (float) Math.max(0.0, 1.0 - t / ttl);
        alpha = Math.min(1f, alpha);

        Composite oldC = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // контур + блик
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(200, 230, 255, 180));
        g2.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));

        g2.setColor(new Color(220, 240, 255, 120));
        g2.fill(new Ellipse2D.Double(cx - r * 0.5, cy - r * 0.8, r * 0.8, r * 0.7));

        g2.setComposite(oldC);
    }

    public boolean isDead() { return dead; }
    public double getY()    { return y;   }
}