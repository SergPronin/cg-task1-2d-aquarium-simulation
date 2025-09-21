package ru.vsu.cs.cg.pronin_s_v.task1_primitives;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D;

public class GamePanel extends JPanel {
    public static final int W = 900;
    public static final int H = 540;

    // Прямоугольник аквариума
    public static final Rectangle AQUARIUM_RECT = new Rectangle(140, 120, 620, 320);
    // Уровень грунта (немного выше нижней кромки аквариума)
    public static final int GROUND_Y = AQUARIUM_RECT.y + AQUARIUM_RECT.height - 28;

    private final Timer timer;
    private long lastNs;

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(236, 230, 220)); // фон комнаты
        setDoubleBuffered(true);

        // Таймер с интервалом ~16 мс (60 FPS)
        timer = new Timer(16, e -> onTick());
        timer.start();
        lastNs = System.nanoTime();
    }

    private void onTick() {
        long now = System.nanoTime();
        double dt = (now - lastNs) / 1e9;
        lastNs = now;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setColor(new Color(245, 239, 232));
        g2.fillRect(0, 0, W, H);

        // Узор обоев
        g2.setColor(new Color(228, 218, 206));
        for (int y = 20; y < H; y += 40) {
            for (int x = 20; x < W; x += 40) {
                g2.fillRoundRect(x, y, 12, 12, 3, 3);
            }
        }

        Shape aquariumShape = new RoundRectangle2D.Double(
                AQUARIUM_RECT.x, AQUARIUM_RECT.y,
                AQUARIUM_RECT.width, AQUARIUM_RECT.height,
                20, 20
        );

        GradientPaint water = new GradientPaint(
                AQUARIUM_RECT.x, AQUARIUM_RECT.y, new Color(70, 160, 220),
                AQUARIUM_RECT.x, AQUARIUM_RECT.y + AQUARIUM_RECT.height, new Color(20, 70, 120)
        );
        g2.setPaint(water);
        g2.fill(aquariumShape);

        drawGround(g2);

        drawLid(g2);

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setStroke(new BasicStroke(4f));
        GradientPaint frameGradient = new GradientPaint(
                AQUARIUM_RECT.x, AQUARIUM_RECT.y,
                new Color(100, 110, 120),
                AQUARIUM_RECT.x, AQUARIUM_RECT.y + AQUARIUM_RECT.height,
                new Color(60, 70, 80)
        );
        g2.setPaint(frameGradient);
        g2.draw(aquariumShape);

        g2.dispose();
    }

    private void drawGround(Graphics2D g2) {
        final int left  = AQUARIUM_RECT.x + 4;
        final int right = AQUARIUM_RECT.x + AQUARIUM_RECT.width - 4;

        final double amplitude  = 6.0;   // высота волны
        final double wavelength = 80.0;  // длина волны
        final int step = 8;              // шаг построения

        Path2D.Double path = new Path2D.Double();
        path.moveTo(left, GROUND_Y - 8);
        for (int x = left; x <= right; x += step) {
            double t = (x - left) / wavelength;
            double y = (GROUND_Y - 8) + Math.sin(t * 2 * Math.PI) * amplitude;
            path.lineTo(x, y);
        }

        path.lineTo(right, GROUND_Y + 30);
        path.lineTo(left,  GROUND_Y + 30);
        path.closePath();

        g2.setPaint(new GradientPaint(
                left, (float) (GROUND_Y - 12),
                new Color(170, 130, 70),
                left, (float) (GROUND_Y + 30),
                new Color(120, 85, 45)
        ));
        g2.fill(path);

    }

    private void drawLid(Graphics2D g2) {
        int pad = 6;   // выступ крышки по бокам
        int lidH = 14; // высота крышки

        RoundRectangle2D lid = new RoundRectangle2D.Double(
                AQUARIUM_RECT.x - pad,
                AQUARIUM_RECT.y - lidH - 2,
                AQUARIUM_RECT.width + pad * 2,
                lidH,
                10, 10
        );

        g2.setPaint(new GradientPaint(
                AQUARIUM_RECT.x, AQUARIUM_RECT.y - lidH,
                new Color(100, 110, 120),
                AQUARIUM_RECT.x, AQUARIUM_RECT.y,
                new Color(60, 70, 80)
        ));
        g2.fill(lid);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(230, 240, 250, 140));
        g2.drawLine(
                AQUARIUM_RECT.x - pad + 8,
                AQUARIUM_RECT.y - lidH + 2,
                AQUARIUM_RECT.x + AQUARIUM_RECT.width + pad - 8,
                AQUARIUM_RECT.y - lidH + 2
        );

        g2.setColor(new Color(60, 70, 80));
        g2.draw(lid);
    }
}