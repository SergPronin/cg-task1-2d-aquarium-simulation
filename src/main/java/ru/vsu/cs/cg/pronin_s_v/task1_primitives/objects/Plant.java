package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

public class Plant implements Static {
    private final int baseX;      // X-координата основания (у грунта)
    private final int baseY;      // Y-координата основания
    private final int height;     // высота растения (от основания до верхушки)
    private final double sway;    // изгиб
    private final Color stemColor; // цвет стебля
    private final Color leafColor; // цвет листьев

    public Plant(int baseX, int baseY, int height, double sway, Color stemColor, Color leafColor) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.height = height;
        this.sway = sway;
        this.stemColor = stemColor;
        this.leafColor = leafColor;
    }

    @Override
    public void draw(Graphics2D g2) {

        int ctrlX = (int) (baseX + 30 * sway);
        int ctrlY = baseY - height / 2;

        // Верхушка растения
        int topX  = baseX;
        int topY  = baseY - height;

        // Стебель
        QuadCurve2D stemCurve = new QuadCurve2D.Double(baseX, baseY, ctrlX, ctrlY, topX, topY);

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(stemColor);
        g2.draw(stemCurve);
        g2.setStroke(oldStroke);

        // Листья
        g2.setColor(leafColor);

        for (int i = 1; i <= 6; i++) {
            double t = i / 7.0;

            double x = (1 - t) * (1 - t) * baseX
                    + 2 * (1 - t) * t * ctrlX
                    + t * t * topX;
            double y = (1 - t) * (1 - t) * baseY
                    + 2 * (1 - t) * t * ctrlY
                    + t * t * topY;

            // размеры листа
            double leafWidth = 12, leafHeight = 6;

            double offset = (i % 2 == 0) ? 7 : -7;

            // Лист
            g2.fill(new Ellipse2D.Double(
                    x - leafWidth / 2 + offset,
                    y - leafHeight / 2,
                    leafWidth,
                    leafHeight
            ));
        }
    }
}