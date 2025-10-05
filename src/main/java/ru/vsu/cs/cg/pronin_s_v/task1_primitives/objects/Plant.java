package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

/**
 * Растение: мягко качается (стебель и листья).
 * Конструктор не менялся: sway трактуем как амплитуду (0..1.2 ориентировочно).
 */
public class Plant implements Static, Dynamic {
    private final int baseX;       // X-координата основания (у грунта)
    private final int baseY;       // Y-координата основания
    private final int height;      // высота растения (от основания до верхушки)
    private final double swayAmp;  // амплитуда изгиба (из исходного sway)
    private final Color stemColor; // цвет стебля
    private final Color leafColor; // цвет листьев

    // Анимация
    private double t = 0.0;             // время
    private final double speed;         // скорость колебаний (рад/с)
    private final double phaseOffset;   // сдвиг фазы, чтобы разные растения качались не синхронно

    public Plant(int baseX, int baseY, int height, double sway, Color stemColor, Color leafColor) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.height = height;
        this.swayAmp = sway;
        this.stemColor = stemColor;
        this.leafColor = leafColor;

        // Псевдослучайные, но детерминированные параметры от базовой позиции:
        int seed = baseX * 73856093 ^ baseY * 19349663 ^ height * 83492791;
        // переводим seed в [0..1]
        double r = ((seed & 0x7fffffff) / (double) Integer.MAX_VALUE);

        this.speed = 1.1 + 0.8 * (r);            // 1.1 .. 1.9 рад/с
        this.phaseOffset = (r * Math.PI * 2.0);  // индивидуальная фаза
    }

    @Override
    public void update(double dt) {
        t += dt;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Фазовый коэффициент качания: [-1..1]
        double swing = Math.sin(t * speed + phaseOffset);

        // Контрольная точка изгиба: чем выше амплитуда и swing, тем сильнее изгиб вправо/влево
        int ctrlX = (int) (baseX + (30 + height * 0.06) * swayAmp * swing);
        int ctrlY = baseY - height / 2;

        // Верхушка
        int topX = (int) (baseX + 6 * swayAmp * swing); // небольшое смещение верхушки
        int topY = baseY - height;

        // Стебель
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(stemColor);

        QuadCurve2D stemCurve = new QuadCurve2D.Double(baseX, baseY, ctrlX, ctrlY, topX, topY);
        g2.draw(stemCurve);

        g2.setStroke(oldStroke);

        // Листья: распределим вдоль кривой, добавим лёгкий собственный "вздох" листьев
        g2.setColor(leafColor);
        int leaves = 6;
        for (int i = 1; i <= leaves; i++) {
            double tt = i / (leaves + 1.0); // параметр вдоль стебля [0..1]
            // точка на квадратичной кривой Безье
            double x = (1 - tt) * (1 - tt) * baseX + 2 * (1 - tt) * tt * ctrlX + tt * tt * topX;
            double y = (1 - tt) * (1 - tt) * baseY + 2 * (1 - tt) * tt * ctrlY + tt * tt * topY;

            // Небольшая динамика размера листа (пульс) + чередование стороны
            double side = (i % 2 == 0 ? 1 : -1);
            double leafPulse = 1.0 + 0.06 * Math.sin(t * (speed * 1.5) + i * 0.9);
            double leafWidth = 12 * leafPulse;
            double leafHeight = 6 * leafPulse;

            // Вращение листа чуть «сдувает» его по направлению качания
            double offset = side * (7 + 4 * swayAmp * swing);
            Shape leaf = new Ellipse2D.Double(
                    x - leafWidth / 2 + offset,
                    y - leafHeight / 2,
                    leafWidth,
                    leafHeight
            );
            g2.fill(leaf);
        }
    }
}