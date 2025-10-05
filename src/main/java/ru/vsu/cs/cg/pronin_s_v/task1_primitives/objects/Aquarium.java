package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class Aquarium implements Static {
    private final Rectangle bounds;
    private final int groundY;

    private final double waveAmplitude;
    private final double waveLen;
    private final int waveStep;

    private final RoundRectangle2D glassShape;

    public Aquarium(Rectangle bounds, int groundY,
                    double sandAmplitude, double sandWavelength, int waveStep) {
        this.bounds = new Rectangle(bounds);
        this.groundY = groundY;
        this.waveAmplitude = sandAmplitude;
        this.waveLen = sandWavelength;
        this.waveStep = waveStep;
        this.glassShape = new RoundRectangle2D.Double(
                this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height, 20, 20
        );
    }

    public Shape getWaterClipShape() {
        // прямоугольник воды до уровня песка (чтобы рыбы не плавали в песке и над крышкой)
        return new Rectangle(
                bounds.x + 4,
                bounds.y + 4,
                bounds.width - 8,
                Math.max(0, groundY - (bounds.y + 4))
        );
    }

    public Rectangle getInnerRect() {
        // чистый прямоугольник для коллизий динамики
        return new Rectangle(
                bounds.x + 8,
                bounds.y + 8,
                bounds.width - 16,
                Math.max(0, groundY - (bounds.y + 8))
        );
    }

    @Override
    public void draw(Graphics2D g2) {
        // Вода
        Paint old = g2.getPaint();
        g2.setPaint(new GradientPaint(
                bounds.x, bounds.y, new Color(70, 160, 220),                 // верх
                bounds.x, bounds.y + bounds.height, new Color(20, 70, 120)   // низ
        ));
        g2.fill(glassShape);
        g2.setPaint(old);

        // Песок
        Path2D sandArea = buildSandArea();
        g2.setPaint(new GradientPaint(
                bounds.x, (float) (groundY - 12), new Color(170, 130, 70),
                bounds.x, (float) (groundY + 30), new Color(120, 85, 45)
        ));
        g2.fill(sandArea); // заливаем песок
    }


    public void drawGlassOverlay(Graphics2D g2) {
        drawLid(g2);        // крышка
        drawGlassFrame(g2); // рамка
    }

    // --- Вспомогательные методы ---

    // Построение фигуры песка с волнистой верхней линией
    private Path2D buildSandArea() {
        final int left  = bounds.x + 4;                 // немного отступаем от рамки
        final int right = bounds.x + bounds.width - 4;  // справа тоже отступ

        Path2D wave = new Path2D.Double();
        wave.moveTo(left, groundY - 8); // стартуем чуть выше уровня песка
        for (int x = left; x <= right; x += waveStep) {
            double t = (x - left) / waveLen;                       // нормированная координата
            double y = (groundY - 8) + Math.sin(t * 2 * Math.PI) * waveAmplitude; // синусоида
            wave.lineTo(x, y); // добавляем точку волны
        }
        // Замыкаем фигуру вниз, чтобы получилась область (многоугольник)
        wave.lineTo(right, groundY + 30);
        wave.lineTo(left,  groundY + 30);
        wave.closePath();
        return wave;
    }

    // Рисует рамку аквариума (обводку стекла)
    private void drawGlassFrame(Graphics2D g2) {
        Stroke oldSt = g2.getStroke();
        Paint  oldPt = g2.getPaint();

        g2.setStroke(new BasicStroke(8f)); // толщина рамки = 8 пикселей
        g2.setPaint(new GradientPaint(
                bounds.x, bounds.y, new Color(100,110,120),               // верх рамки
                bounds.x, bounds.y + bounds.height, new Color(60,70,80)   // низ рамки
        ));
        g2.draw(glassShape); // обводим контур аквариума

        // восстанавливаем настройки
        g2.setPaint(oldPt);
        g2.setStroke(oldSt);
    }

    // Рисует крышку аквариума
    private void drawLid(Graphics2D g2) {
        Paint old = g2.getPaint();

        int sidePadding = 6; // крышка шире аквариума на 6 px с каждой стороны
        int lidHeight   = 14; // высота крышки

        // Форма крышки — скруглённый прямоугольник
        RoundRectangle2D lidShape = new RoundRectangle2D.Double(
                bounds.x - sidePadding,             // смещаем влево
                bounds.y - lidHeight - 2,           // поднимаем вверх над аквариумом
                bounds.width + sidePadding * 2,     // шире аквариума
                lidHeight, 10, 10                   // высота, скругления 10 px
        );

        // Градиент для крышки (светлее сверху, темнее снизу)
        g2.setPaint(new GradientPaint(
                bounds.x, bounds.y - lidHeight, new Color(100,110,120),
                bounds.x, bounds.y,              new Color(60,70,80)
        ));
        g2.fill(lidShape);

        // Блик на крыше
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(230, 240, 250, 140)); // полупрозрачный белый
        g2.drawLine(
                bounds.x - sidePadding + 8,
                bounds.y - lidHeight + 2,
                bounds.x + bounds.width + sidePadding - 8,
                bounds.y - lidHeight + 2
        );

        // Обводим крышку тёмной линией
        g2.setColor(new Color(60, 70, 80));
        g2.draw(lidShape);

        g2.setPaint(old);
    }
}