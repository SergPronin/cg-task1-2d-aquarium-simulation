package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Простой и красивый 2D-пузырёк:
 * - Форма: слегка эллиптический круг с мягким "дыханием".
 * - Заливка: полупрозрачный голубой с лёгким градиентом и бликом.
 * - Движение: плавный подъём с небольшим дрейфом по X.
 */
public class Bubble implements Dynamic, Static {

    // Положение и жизненный цикл
    private double x, y;
    private double r;        // базовый радиус
    private double vy;       // скорость подъёма
    private double t;        // возраст
    private double ttl;      // время жизни
    private boolean dead = false;

    // Дрейф по X
    private double wobbleAmp;    // амплитуда дрейфа
    private double wobbleFreq;   // частота дрейфа
    private double wobblePhase;  // фаза
    private double dxSmoothed;   // сглаженный дрейф
    private double smoothHz;     // скорость сглаживания

    // Лёгкая эллиптичность формы
    private double shapePulseFreq;
    private double shapePulseAmp;

    public Bubble(double x, double y) {
        this.x = x;
        this.y = y;

        // Жизнь и размеры
        this.r = 3.5 + Math.random() * 2.5;    // 3.5..6.0 пикс
        this.vy = -40 - Math.random() * 20;    // -40..-60 пикс/с
        this.ttl = 3.0 + Math.random() * 2.0;  // 3.0..5.0 с

        // Дрейф: мягкий и естественный
        this.wobbleAmp = 2.0 + Math.random() * 1.5;   // 2.0..3.5 пикс
        this.wobbleFreq = 0.2 + Math.random() * 0.2;  // 0.2..0.4 Гц
        this.wobblePhase = Math.random() * Math.PI * 2;
        this.smoothHz = 3.0;                          // сглаживание дрейфа
        this.dxSmoothed = 0.0;

        // Эллиптичность
        this.shapePulseFreq = 1.2 + Math.random() * 0.6; // 1.2..1.8 Гц
        this.shapePulseAmp = 0.04 + Math.random() * 0.02; // 4..6% "дыхания"
    }

    @Override
    public void update(double dt) {
        if (dead) return;

        t += dt;
        if (t >= ttl) {
            dead = true;
            return;
        }

        // Подъём
        y += vy * dt;

        // Дрейф по X
        double lifeK = Math.max(0, 1.0 - t / ttl); // 1..0
        double amp = wobbleAmp * (0.7 + 0.3 * lifeK);
        double targetDx = Math.sin((t * wobbleFreq + wobblePhase) * 2 * Math.PI) * amp;

        // Экспоненциальное сглаживание
        double alpha = 1.0 - Math.exp(-2 * Math.PI * smoothHz * dt);
        dxSmoothed += (targetDx - dxSmoothed) * alpha;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (dead) return;

        double cx = x + dxSmoothed;
        double cy = y;

        // Прозрачность: плавный спад
        float a = (float) Math.max(0.0, 1.0 - t / ttl);
        a = (float) Math.pow(a, 0.85);
        Composite oldC = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));

        // Лёгкое "дыхание" формы
        double pulse = Math.sin(t * shapePulseFreq * 2 * Math.PI) * shapePulseAmp;
        double rx = r * (1.0 + pulse); // слабое растяжение по X
        double ry = r * (1.0 - pulse); // слабое сжатие по Y

        // Тело пузырька: лёгкий градиент
        GradientPaint body = new GradientPaint(
                (float)(cx - rx * 0.3), (float)(cy - ry * 0.3),
                new Color(200, 230, 255, 140),
                (float)(cx + rx * 0.3), (float)(cy + ry * 0.3),
                new Color(150, 200, 240, 60)
        );
        g2.setPaint(body);
        g2.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));

        // Блик: маленький полукруг сверху-слева
        double br = r * 0.6;
        double bx = cx - r * 0.3;
        double by = cy - r * 0.3;
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fill(new Ellipse2D.Double(bx, by, br * 2, br * 2));

        g2.setComposite(oldC);
    }

    public boolean isDead() { return dead; }
    public double getY() { return y; }
}