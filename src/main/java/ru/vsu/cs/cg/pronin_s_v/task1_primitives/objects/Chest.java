package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;

public class Chest implements Static {
    private final int x;
    private final int y;
    private final int w;
    private final int chestHeight;
    private final int lidHeight;   // высота крышки (отдельный прямоугольник над корпусом)

    public Chest(int x, int y, int width, int height, int lidHeight) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.chestHeight = height;
        this.lidHeight = lidHeight;
    }

    @Override
    public void draw(Graphics2D g2) {
        Object aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ===================== КОРПУС СУНДУКА =====================
        // Скруглённый прямоугольник: «деревянная коробка»
        RoundRectangle2D body = new RoundRectangle2D.Double(x, y, w, chestHeight, 14, 14);

        // Вертикальный градиент имитирует объём дерева: сверху немного светлее, снизу темнее
        GradientPaint wood = new GradientPaint(
                x,     y,     new Color(120, 78, 40),  // верх корпуса (теплее/светлее)
                x,     y + chestHeight, new Color(80,  52, 28)   // низ корпуса (темнее/холоднее)
        );
        g2.setPaint(wood);
        g2.fill(body);

        // Деревянные рейки: 3 горизонтальные линии по корпусу, для детализации
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(95, 62, 33));
        int rails = 3;
        for (int i = 1; i <= rails; i++) {
            int yy = y + i * (chestHeight / (rails + 1));        // равномерно по высоте корпуса
            g2.drawLine(x + 10, yy, x + w - 10, yy);   // коротим на 10px с краёв, чтобы не «ездила» по радиусу
        }

        // ===================== КРЫШКА =====================
        // Крышка — отдельный скруглённый прямоугольник, сидит над корпусом на высоту lidH
        RoundRectangle2D lid = new RoundRectangle2D.Double(x, y - lidHeight, w, lidHeight, 14, 14);
        // Отдельный градиент крышки — немного иной оттенок дерева
        GradientPaint lidPaint = new GradientPaint(
                x,     y - lidHeight, new Color(135, 90, 48),  // верх крышки
                x,     y,        new Color(92,  62, 34)   // низ крышки (стык с корпусом)
        );
        g2.setPaint(lidPaint);
        g2.fill(lid);

        // Блик на крышке — тонкая светлая линия почти вдоль всей ширины
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawLine(x + 12, y - lidHeight + 4, x + w - 12, y - lidHeight + 4);

        // ===================== МЕТАЛЛИЧЕСКИЕ УГОЛКИ/ОКАНТОВКА =====================
        // Толстые линии по краям — создают ощущение металлических накладок (золочёных)
        g2.setColor(new Color(200, 185, 120)); // «латунный» цвет
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Вертикальные уголки корпуса
        g2.drawLine(x + 8,     y + 8,      x + 8,     y + chestHeight - 8);
        g2.drawLine(x + w - 8, y + 8,      x + w - 8, y + chestHeight - 8);

        // Окантовки на крышке (вверх/вниз) — продолжают «металл» на крышке
        g2.drawLine(x + 8,     y - lidHeight + 8, x + 8,     y - 8);
        g2.drawLine(x + w - 8, y - lidHeight + 8, x + w - 8, y - 8);

        // Заклёпки на уголках (маленькие светлые точки)
        g2.setColor(new Color(240, 230, 170));
        for (int i = 0; i < 3; i++) {
            int rx = x + 8;                         // левый уголок
            int ry = y + 12 + i * (chestHeight / 3);          // три заклёпки по высоте
            g2.fill(new Ellipse2D.Double(rx - 2,        ry - 2,        4, 4));
            g2.fill(new Ellipse2D.Double(x + w - 10,    ry - 2,        4, 4)); // правый уголок
        }

        // ===================== ЗАМОК =====================
        // Небольшой прямоугольник со скруглением по центру корпуса
        int lockW = 16, lockH = 20;
        int lockX = x + w / 2 - lockW / 2;
        int lockY = y + chestHeight / 2 - lockH / 2;

        // Корпус замка: заливка
        g2.setColor(new Color(210, 195, 130)); // чуть светлее металла уголков, чтобы выделялся
        RoundRectangle2D lock = new RoundRectangle2D.Double(lockX, lockY, lockW, lockH, 6, 6);
        g2.fill(lock);

        // Контур замка
        g2.setColor(new Color(160, 140, 90));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(lock);

        // «Скважина»: прямоугольная прорезь + круглая часть
        g2.setColor(new Color(80, 60, 30));
        g2.fill(new Rectangle2D.Double(lockX + lockW / 2.0 - 2, lockY + 6, 4, 6));
        g2.fill(new Ellipse2D.Double(lockX + lockW / 2.0 - 3, lockY + 12, 6, 6));

        // ===================== ЩЕЛЬ С ЗОЛОТОМ =====================
        // Узкая прямоугольная полоска под крышкой — имитация «света от золота» внутри
        GradientPaint gold = new GradientPaint(
                x, y + 4,   new Color(255, 215, 120, 200),  // сверху ярче/теплее
                x, y + 12,  new Color(255, 200,  80,  80)   // ниже — слабее/прозрачнее
        );
        g2.setPaint(gold);
        g2.fill(new Rectangle2D.Double(x + 12, y - 2, w - 24, 6));

        // --- возвращаем исходный режим сглаживания ---
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }
}