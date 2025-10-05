package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * CrystalBubble — спокойный пузырёк с кристальным обводом и бликовым серпом.
 * Особенности:
 *  - Очень мягкий дрейф по X с экспоненциальным сглаживанием (без дрожи).
 *  - Незаметная эллиптичность (как от течения): форма чуть «дышит».
 *  - Двойной кант (хроматический): холодный голубой + тёплый розовато-оранжевый.
 *  - Внутренний "серп" блика и слабая аура вокруг.
 */
public class Bubble implements Dynamic, Static {

    // Положение/жизненный цикл
    private double x, y;
    private double r;        // базовый радиус
    private double vy;       // скорость подъёма
    private double t;        // возраст
    private double ttl;      // время жизни
    private boolean dead = false;

    // Дрейф
    private double wobbleAmp;    // амплитуда дрейфа
    private double f1, f2;       // две очень низкие частоты
    private double phi1, phi2;   // фазы
    private double dxSmoothed;   // сглаженный dx
    private double smoothHz;     // скорость сглаживания

    // Лёгкая эллиптичность формы
    private double shapePulseFreq;
    private double shapePulseAmp;

    public Bubble(double x, double y) {
        this.x = x;
        this.y = y;

        // Жизнь/размеры — спокойные
        this.r   = 4.8 + Math.random() * 3.8;      // 4.8..8.6
        this.vy  = -34  - Math.random() * 24;      // -34..-58
        this.ttl = 3.4  + Math.random() * 2.2;     // 3.4..5.6

        // Дрейф: минимальный, низкочастотный
        this.wobbleAmp = 2.4 + Math.random() * 1.4; // 2.4..3.8
        this.f1  = 0.22 + Math.random() * 0.14;     // 0.22..0.36 Гц
        this.f2  = 0.10 + Math.random() * 0.10;     // 0.10..0.20 Гц
        this.phi1 = Math.random() * Math.PI * 2;
        this.phi2 = Math.random() * Math.PI * 2;
        this.smoothHz = 3.3;                        // сглаживание дрейфа
        this.dxSmoothed = 0.0;

        // Эллиптичность (деликатная)
        this.shapePulseFreq = 1.4 + Math.random() * 0.8; // 1.4..2.2 Гц
        this.shapePulseAmp  = 0.05 + Math.random() * 0.03; // 5..8% «дыхания» по оси
    }

    @Override
    public void update(double dt) {
        if (dead) return;

        t += dt;
        if (t >= ttl) { dead = true; return; }

        // Подъём
        y += vy * dt;

        // Целевая dx (комбинация низких частот), чуть уменьшается к концу жизни
        double lifeK = Math.max(0, 1.0 - t / ttl); // 1..0
        double amp   = wobbleAmp * (0.6 + 0.4 * lifeK);

        double targetDx =
                Math.sin((t * f1 + phi1) * 2 * Math.PI) * amp * 0.7 +
                        Math.sin((t * f2 + phi2) * 2 * Math.PI) * amp * 0.3;

        // Эксп. сглаживание (one-pole low-pass)
        double alpha = 1.0 - Math.exp(-2 * Math.PI * smoothHz * dt);
        dxSmoothed += (targetDx - dxSmoothed) * alpha;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (dead) return;

        double cx = x + dxSmoothed;
        double cy = y;

        // Прозрачность: мягкий спад, ближе к концу — быстрее
        float a = (float) Math.max(0.0, 1.0 - t / ttl);
        a = (float) Math.pow(a, 0.9);
        Composite oldC = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));

        // Лёгкое «дыхание» формы (эллипс)
        double pulse = 1.0 + shapePulseAmp * Math.sin(t * shapePulseFreq * 2 * Math.PI);
        double rx = r * (1.0 + 0.02 * pulse); // по X практически круг
        double ry = r * (1.0 - 0.06 * pulse); // по Y немного ужимается/расширяется

        // ===== МЯГКАЯ АУРА =====
        RadialGradientPaint aura = new RadialGradientPaint(
                new Point2D.Double(cx, cy),
                (float) (Math.max(rx, ry) * 1.65),
                new float[]{0f, 0.6f, 1f},
                new Color[]{
                        new Color(210, 235, 255, 38),
                        new Color(190, 215, 240, 16),
                        new Color(190, 215, 240, 0)
                }
        );
        g2.setPaint(aura);
        g2.fill(new Ellipse2D.Double(cx - rx * 1.65, cy - ry * 1.65, rx * 3.3, ry * 3.3));

        // ===== ТЕЛО ПУЗЫРЯ (радиальный градиент) =====
        Point2D center = new Point2D.Double(cx, cy);
        float radius = (float) Math.max(rx, ry);
        Color core = new Color(240, 250, 255, 160);
        Color mid  = new Color(205, 230, 248, 85);
        Color edge = new Color(165, 205, 235, 55);

        RadialGradientPaint body = new RadialGradientPaint(
                center, radius,
                new float[]{0f, 0.55f, 1f},
                new Color[]{core, mid, edge}
        );
        g2.setPaint(body);
        g2.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));

        // ===== ВНУТРЕННИЙ СЕРП БЛИКА (сверху-слева) =====
        double sr = Math.min(rx, ry);
        double hlx = cx - sr * 0.45;
        double hly = cy - sr * 0.48;
        double hlw = sr * 0.9;
        double hlh = sr * 0.55;
        g2.setColor(new Color(255, 255, 255, 105));
        g2.fill(new Ellipse2D.Double(hlx, hly, hlw, hlh));

        // ===== ДВОЙНОЙ ХРОМАТИЧЕСКИЙ КАНТ =====
        // холодный голубой (внешний тонкий)
        g2.setStroke(new BasicStroke(1.05f));
        g2.setColor(new Color(110, 160, 210, 95));
        g2.draw(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));
        // тёплый розовато-оранжевый (внутренний очень тонкий)
        g2.setStroke(new BasicStroke(0.6f));
        g2.setColor(new Color(245, 180, 170, 70));
        double shrink = 0.85;
        g2.draw(new Ellipse2D.Double(cx - rx * shrink, cy - ry * shrink, rx * 2 * shrink, ry * 2 * shrink));

        g2.setComposite(oldC);
    }

    public boolean isDead() { return dead; }
    public double getY()    { return y;   }
}