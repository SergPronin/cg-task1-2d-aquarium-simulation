package ru.vsu.cs.cg.pronin_s_v.task1_primitives.ui;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel {
    public static final int WINDOW_WIDTH  = 1100;
    public static final int WINDOW_HEIGHT = 700;

    // Аквариум
    public static final Rectangle AQUARIUM_BOUNDS = new Rectangle(140, 120, 620, 320);
    public static final int GROUND_LEVEL_Y = AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height - 32;

    private final List<Static>  statics  = new ArrayList<>();
    private final List<Dynamic> dynamics = new ArrayList<>();

    private Aquarium aquarium;
    private Chest chest;
    private final Timer timer;
    private long lastNs = System.nanoTime();

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(new Color(236, 230, 220));
        setDoubleBuffered(true);

        // --- фон ---
        statics.add(new BackgroundWall(WINDOW_WIDTH, WINDOW_HEIGHT));

        // --- аквариум ---
        aquarium = new Aquarium(
                AQUARIUM_BOUNDS,
                GROUND_LEVEL_Y,
                6.0, 80.0, 8
        );
        statics.add(aquarium);

        // --- камни ---
        statics.add(new Stone(AQUARIUM_BOUNDS.x +  80, GROUND_LEVEL_Y -  6, 62, 28, new Color(116,108, 98)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 160, GROUND_LEVEL_Y - 10, 48, 24, new Color(128,118,108)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 245, GROUND_LEVEL_Y -  4, 78, 30, new Color(112,104, 95)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 360, GROUND_LEVEL_Y - 12, 54, 26, new Color(122,112,102)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 470, GROUND_LEVEL_Y -  6, 70, 30, new Color(110,102, 94)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 560, GROUND_LEVEL_Y - 10, 58, 26, new Color(124,114,104)));

        // --- растения ---
        Color stem = new Color(22, 140, 90);
        Color leaf = new Color(34, 170, 110);
        dynamics.add(new Plant(AQUARIUM_BOUNDS.x + 110, GROUND_LEVEL_Y,  85, 0.6, stem, leaf));
        dynamics.add(new Plant(AQUARIUM_BOUNDS.x + 210, GROUND_LEVEL_Y, 120, 0.9, stem, leaf));
        dynamics.add(new Plant(AQUARIUM_BOUNDS.x + 320, GROUND_LEVEL_Y,  95, 0.7, stem, leaf));
        dynamics.add(new Plant(AQUARIUM_BOUNDS.x + 520, GROUND_LEVEL_Y, 110, 0.8, stem, leaf));

        // --- сундук ---
        int chestW = 140, chestH = 70, chestLid = 18;
        int chestX = AQUARIUM_BOUNDS.x + 340;
        int chestY = GROUND_LEVEL_Y - chestH + 6;
        chest = new Chest(chestX, chestY, chestW, chestH, chestLid);
        statics.add(chest);
        dynamics.add(chest);

        // --- рыбки ---
        dynamics.add(new Fish(AQUARIUM_BOUNDS.x + 220, GROUND_LEVEL_Y - 120, 70, 38,
                new Color(255, 125, 90), new Color(255, 200, 170)));
        dynamics.add(new Fish(AQUARIUM_BOUNDS.x + 430, GROUND_LEVEL_Y - 160, 60, 32,
                new Color(120, 200, 255), new Color(200, 240, 255)));
        dynamics.add(new Fish(AQUARIUM_BOUNDS.x + 520, GROUND_LEVEL_Y -  90, 46, 26,
                new Color(250, 220, 120), new Color(255, 245, 180)));

        // 🌟 морская звезда — теперь в правом верхнем углу
        int starX = AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width - 50;
        int starY = AQUARIUM_BOUNDS.y + 40;
        dynamics.add(new Starfish(starX, starY, 36,
                new Color(255, 150, 100),
                new Color(210, 90, 60)));

        // --- тумба ---
        statics.add(new CabinetHalf(AQUARIUM_BOUNDS));

        // --- таймер ---
        timer = new Timer(1000 / 60, e -> {
            long now = System.nanoTime();
            double dt = (now - lastNs) / 1_000_000_000.0;
            lastNs = now;

            Rectangle inner = aquarium.getInnerRect();

            for (Dynamic d : dynamics) {
                if (d instanceof Fish f) {
                    f.update(dt, inner);
                } else {
                    d.update(dt);
                }
            }

            if (chest.shouldEmitBubbleThisFrame(dt)) {
                Point p = chest.getBubbleOrigin();
                dynamics.add(new Bubble(p.x, p.y));
            }

            // чистим пузыри
            Iterator<Dynamic> it = dynamics.iterator();
            int waterTop = aquarium.getInnerRect().y;
            while (it.hasNext()) {
                Dynamic d = it.next();
                if (d instanceof Bubble b) {
                    if (b.isDead() || b.getY() < waterTop - 10) it.remove();
                }
            }

            repaint();
        });
        timer.setCoalesce(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        lastNs = System.nanoTime();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        for (Static s : statics) s.draw(g2);

        Shape oldClip = g2.getClip();
        Shape waterClip = aquarium.getWaterClipShape();
        g2.setClip(waterClip);
        for (Dynamic d : dynamics) {
            if (d instanceof Static s) s.draw(g2);
        }
        g2.setClip(oldClip);

        aquarium.drawGlassOverlay(g2);
        g2.dispose();
    }
}