package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

/**
 * Водоросль в аквариуме.
 * Представляет собой кривую (стебель) с листьями.
 */
public class Plant implements Static {
    private final int baseX;      // точка у грунта (основание)
    private final int baseY;
    private final int height;     // высота растения
    private final double sway;    // изгиб (отклонение)
    private final Color stemColor;
    private final Color leafColor;

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
        int ctrlX = (int) (baseX + 30 * sway); // управляющая точка для изгиба
        int ctrlY = baseY - height / 2;
        int topX  = baseX;
        int topY  = baseY - height;

        // Стебель (кривая Безье второго порядка)
        QuadCurve2D stemCurve = new QuadCurve2D.Double(baseX, baseY, ctrlX, ctrlY, topX, topY);
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(stemColor);
        g2.draw(stemCurve);
        g2.setStroke(oldStroke);

        // Листья вдоль стебля
        g2.setColor(leafColor);
        for (int i = 1; i <= 6; i++) {
            double t = i / 7.0; // параметр вдоль кривой
            double x = (1 - t) * (1 - t) * baseX + 2 * (1 - t) * t * ctrlX + t * t * topX;
            double y = (1 - t) * (1 - t) * baseY + 2 * (1 - t) * t * ctrlY + t * t * topY;

            double leafWidth = 12, leafHeight = 6;
            double offset = (i % 2 == 0) ? 7 : -7;
            g2.fill(new Ellipse2D.Double(x - leafWidth / 2 + offset, y - leafHeight / 2, leafWidth, leafHeight));
        }
    }
}