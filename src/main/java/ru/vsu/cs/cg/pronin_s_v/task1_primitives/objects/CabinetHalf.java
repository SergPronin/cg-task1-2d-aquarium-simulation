package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CabinetHalf implements Static {
    private final Rectangle aquariumBounds;

    public CabinetHalf(Rectangle aquariumBounds) {
        this.aquariumBounds = new Rectangle(aquariumBounds);
    }

    @Override
    public void draw(Graphics2D g2) {
        int topPad = 22;  // отступы по бокам от аквариума
        int topH = 20;    // высота верхней столешницы
        int topX = aquariumBounds.x - topPad;                 // X начала столешницы
        int topY = aquariumBounds.y + aquariumBounds.height;  // Y — сразу под аквариумом
        int topW = aquariumBounds.width + topPad * 2;         // ширина (шире аквариума на паддинги)

        // Столешница
        RoundRectangle2D tabletop = new RoundRectangle2D.Double(topX, topY, topW, topH, 12, 12);
        g2.setPaint(new GradientPaint(
                topX, topY, new Color(95, 75, 55),
                topX, topY + topH, new Color(70, 54, 40)
        ));
        g2.fill(tabletop);

        // Комод
        int bodyX = topX + 8;
        int bodyY = topY + topH - 2;
        int bodyW = topW - 16;
        int bodyH = 260;
        g2.setPaint(new GradientPaint(
                bodyX, bodyY, new Color(120, 96, 74),
                bodyX, bodyY + bodyH, new Color(83, 64, 48)
        ));
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 14, 14);

        // Ящики комода
        int gap = 10;                      // отступы между ящиками
        int drawerW = bodyW - 2 * gap;     // ширина ящика
        int drawerH = 36;                  // высота ящика
        int drawerX = bodyX + gap;         // X первого ящика

        for (int i = 0; i < 5; i++) { // рисуем 5 ящиков
            int drawerY = bodyY + gap + i * (drawerH + gap);

            // Ящик
            g2.setColor(new Color(110, 88, 68));
            g2.fillRoundRect(drawerX, drawerY, drawerW, drawerH, 10, 10);

            // Ручка
            g2.setColor(new Color(230, 220, 160));
            g2.fillOval(
                    drawerX + drawerW / 2 - 5,
                    drawerY + drawerH / 2 - 5,
                    10, 10
            );
        }

        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRect(bodyX, topY + topH - 2, bodyW, 4);
    }
}