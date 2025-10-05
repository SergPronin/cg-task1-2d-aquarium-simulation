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
    private final int lidHeight;

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

        RoundRectangle2D body = new RoundRectangle2D.Double(x, y, w, chestHeight, 14, 14);

        GradientPaint wood = new GradientPaint(
                x,     y,     new Color(120, 78, 40),
                x,     y + chestHeight, new Color(80,  52, 28)
        );
        g2.setPaint(wood);
        g2.fill(body);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(95, 62, 33));
        int rails = 3;
        for (int i = 1; i <= rails; i++) {
            int yy = y + i * (chestHeight / (rails + 1));
            g2.drawLine(x + 10, yy, x + w - 10, yy);
        }


        RoundRectangle2D lid = new RoundRectangle2D.Double(x, y - lidHeight, w, lidHeight, 14, 14);
        GradientPaint lidPaint = new GradientPaint(
                x,     y - lidHeight, new Color(135, 90, 48),
                x,     y,        new Color(92,  62, 34)
        );
        g2.setPaint(lidPaint);
        g2.fill(lid);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawLine(x + 12, y - lidHeight + 4, x + w - 12, y - lidHeight + 4);


        g2.setColor(new Color(200, 185, 120));
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.drawLine(x + 8,     y + 8,      x + 8,     y + chestHeight - 8);
        g2.drawLine(x + w - 8, y + 8,      x + w - 8, y + chestHeight - 8);

        g2.drawLine(x + 8,     y - lidHeight + 8, x + 8,     y - 8);
        g2.drawLine(x + w - 8, y - lidHeight + 8, x + w - 8, y - 8);

        g2.setColor(new Color(240, 230, 170));
        for (int i = 0; i < 3; i++) {
            int rx = x + 8;
            int ry = y + 12 + i * (chestHeight / 3);
            g2.fill(new Ellipse2D.Double(rx - 2,        ry - 2,        4, 4));
            g2.fill(new Ellipse2D.Double(x + w - 10,    ry - 2,        4, 4)); // правый уголок
        }


        int lockW = 16, lockH = 20;
        int lockX = x + w / 2 - lockW / 2;
        int lockY = y + chestHeight / 2 - lockH / 2;

        g2.setColor(new Color(210, 195, 130));
        RoundRectangle2D lock = new RoundRectangle2D.Double(lockX, lockY, lockW, lockH, 6, 6);
        g2.fill(lock);

        g2.setColor(new Color(160, 140, 90));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(lock);

        g2.setColor(new Color(80, 60, 30));
        g2.fill(new Rectangle2D.Double(lockX + lockW / 2.0 - 2, lockY + 6, 4, 6));
        g2.fill(new Ellipse2D.Double(lockX + lockW / 2.0 - 3, lockY + 12, 6, 6));


        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }
}