package ru.vsu.cs.cg.pronin_s_v.task1_primitives;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GamePanel extends JPanel {
    public static final int W = 900;
    public static final int H = 540;

    // Прямоугольник, рамка аквариума
    public static final Rectangle AQUARIUM_RECT = new Rectangle(140, 120, 620, 320);

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

        // Фон — светлый бежевый
        g2.setColor(new Color(245, 239, 232));
        g2.fillRect(0, 0, W, H);

        // Узор «обоев»
        g2.setColor(new Color(228, 218, 206));
        for (int y = 20; y < H; y += 40) {
            for (int x = 20; x < W; x += 40) {
                g2.fillRoundRect(x, y, 12, 12, 3, 3);
            }
        }

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(160, 170, 175));
        Shape aquariumShape = new RoundRectangle2D.Double(
                AQUARIUM_RECT.x, AQUARIUM_RECT.y,
                AQUARIUM_RECT.width, AQUARIUM_RECT.height,
                20, 20
        );
        g2.draw(aquariumShape);

        g2.dispose();
    }
}