package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.*;

/**
 * Очень простая 2D-рыба:
 * - Тело: вытянутый овал (плоская заливка).
 * - Хвост: треугольный, с лёгким колебанием.
 * - Плавники: верхний и нижний, маленькие треугольники, с минимальным "дыханием".
 * - Глаз: маленький круг с бликом, без рта.
 * - Движение: плавное, с разворотами у стенок аквариума.
 *
 * Конструктор: new Fish(x, y, w, h, bodyColor, finColor)
 */
public class Fish implements Static, Dynamic {

    private double x, y;            // центр рыбы
    private final int w, h;         // логический размер
    private final Color bodyColor;  // цвет тела
    private final Color finColor;   // цвет плавников и хвоста

    // движение
    private double vx;
    private double baseY;
    private double bobAmp;
    private double bobFreq;
    private boolean faceRight;
    private Rectangle lastBounds;

    // анимация
    private double t = 0;
    private final double tailFreq = 1.8;       // Гц, мягкое колебание хвоста
    private final double tailAmpDeg = 10.0;   // амплитуда размаха хвоста (градусы)
    private final double finBreathFreq = 0.7; // Гц, лёгкое "дыхание" плавников
    private final double finBreathAmp = 3.0;  // пикс, минимальная амплитуда

    public Fish(int x, int y, int w, int h, Color bodyColor, Color finColor) {
        this.x = x;
        this.y = y;
        this.w = Math.max(30, w);  // минимальный размер для читаемости
        this.h = Math.max(16, h);
        this.bodyColor = bodyColor;
        this.finColor = finColor;

        this.vx = (Math.random() < 0.5 ? -1 : 1) * (45 + Math.random() * 35); // 45..80 пикс/с
        this.faceRight = vx >= 0;

        this.baseY = y;
        this.bobAmp = 5 + Math.random() * 7;       // 5..12 пикс
        this.bobFreq = 0.2 + Math.random() * 0.25; // 0.2..0.45 Гц
    }

    @Override
    public void update(double dt) {
        if (lastBounds != null) {
            update(dt, lastBounds);
            return;
        }
        t += dt;
        y = baseY + Math.sin(t * bobFreq * Math.PI * 2) * bobAmp;
        x += vx * dt;
    }

    public void update(double dt, Rectangle waterBounds) {
        lastBounds = waterBounds;

        t += dt;

        // вертикальный дрейф
        y = baseY + Math.sin(t * bobFreq * Math.PI * 2) * bobAmp;

        // движение по X
        x += vx * dt;

        // развороты
        int pad = Math.max(10, (int)(w * 0.25));
        int left = waterBounds.x + pad;
        int right = waterBounds.x + waterBounds.width - pad;

        if (x < left) {
            x = left;
            vx = Math.abs(vx);
            faceRight = true;
        }
        if (x > right) {
            x = right;
            vx = -Math.abs(vx);
            faceRight = false;
        }

        int top = waterBounds.y + Math.max(10, (int)(h * 0.5));
        int bottom = waterBounds.y + waterBounds.height - Math.max(10, (int)(h * 0.5));
        baseY = clamp(baseY, top, bottom);
    }

    @Override
    public void draw(Graphics2D g2) {
        Object aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        if (!faceRight) g2.scale(-1, 1);

        // нормированный канвас 100x50
        double sx = w / 100.0, sy = h / 50.0;
        g2.scale(sx, sy);

        // параметры формы
        final double BW = 100, BH = 50;
        final double half = BW * 0.5;

        // ===== тело: вытянутый овал =====
        Shape body = new Ellipse2D.Double(-half * 0.9, -BH * 0.4, BW * 0.9, BH * 0.8);

        // ===== плавники: маленькие треугольники, с лёгким "дыханием" =====
        double breath = Math.sin(t * finBreathFreq * 2 * Math.PI) * finBreathAmp;

        Path2D topFin = new Path2D.Double();
        topFin.moveTo(0, -BH * 0.15);
        topFin.lineTo(BW * 0.2, -BH * 0.35 - breath * 0.02);
        topFin.lineTo(BW * 0.1, -BH * 0.15);
        topFin.closePath();

        Path2D botFin = new Path2D.Double();
        botFin.moveTo(0, BH * 0.15);
        botFin.lineTo(BW * 0.2, BH * 0.35 + breath * 0.02);
        botFin.lineTo(BW * 0.1, BH * 0.15);
        botFin.closePath();

        // ===== хвост: треугольный, с лёгким колебанием =====
        double tailDeg = Math.sin(t * tailFreq * 2 * Math.PI) * tailAmpDeg;
        AffineTransform tailTx = AffineTransform.getRotateInstance(
                Math.toRadians(tailDeg), -half * 0.9, 0);

        Path2D tail = new Path2D.Double();
        tail.moveTo(-half * 0.9, 0);
        tail.lineTo(-half * 1.15, -BH * 0.25);
        tail.lineTo(-half * 1.15, BH * 0.25);
        tail.closePath();
        Shape tailShape = tailTx.createTransformedShape(tail);

        // ===== глаз + блик =====
        double eyeR = 3.5;
        double eyeCx = half * 0.6;
        double eyeCy = -BH * 0.05;
        Shape eye = new Ellipse2D.Double(eyeCx - eyeR, eyeCy - eyeR, eyeR * 2, eyeR * 2);
        Shape eyeHighlight = new Ellipse2D.Double(eyeCx - eyeR * 0.3, eyeCy - eyeR * 0.4, eyeR * 0.6, eyeR * 0.4);

        // ===== рисуем плоскими заливками =====
        g2.setColor(finColor);
        g2.fill(topFin);
        g2.fill(botFin);
        g2.fill(tailShape);

        g2.setColor(bodyColor);
        g2.fill(body);

        g2.setColor(new Color(20, 25, 30)); // тёмный глаз
        g2.fill(eye);
        g2.setColor(new Color(255, 255, 255, 160)); // блик глаза
        g2.fill(eyeHighlight);

        // restore
        g2.setTransform(old);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    private static double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }
}