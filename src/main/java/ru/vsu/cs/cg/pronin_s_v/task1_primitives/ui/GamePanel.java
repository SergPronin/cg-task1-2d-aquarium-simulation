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

    public static final Rectangle AQUARIUM_BOUNDS = new Rectangle(140, 120, 620, 320);
    public static final int GROUND_LEVEL_Y = AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height - 32;

    private final List<Static>  statics  = new ArrayList<>();
    private final List<Dynamic> dynamics = new ArrayList<>();

    private Aquarium aquarium;
    private Chest chest;

    // ссылки на эмиттеры/растения/звезду (как в твоей предыдущей версии)
    private Plant plant1, plant2, plant3, plant4;
    private Starfish star;

    // таймеры фоновых пузырей (как раньше)
    private double sandEmitT = 0,   sandEmitPeriodMin = 1.2, sandEmitPeriodMax = 2.2;
    private double plantsEmitT = 0, plantsEmitPeriodMin = 1.8, plantsEmitPeriodMax = 3.2;
    private double stonesEmitT = 0, stonesEmitPeriodMin = 3.0, stonesEmitPeriodMax = 5.0;
    private double starEmitT   = 0, starEmitPeriodMin   = 3.0, starEmitPeriodMax   = 5.0;
    private double columnEmitT = 0, columnEmitPeriodMin = 2.5, columnEmitPeriodMax = 4.5;

    private final Timer timer;
    private long lastNs = System.nanoTime();

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(new Color(236, 230, 220));
        setDoubleBuffered(true);

        statics.add(new BackgroundWall(WINDOW_WIDTH, WINDOW_HEIGHT));

        aquarium = new Aquarium(AQUARIUM_BOUNDS, GROUND_LEVEL_Y, 6.0, 80.0, 8);
        statics.add(aquarium);

        statics.add(new Stone(AQUARIUM_BOUNDS.x +  80, GROUND_LEVEL_Y -  6, 62, 28, new Color(116,108, 98)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 160, GROUND_LEVEL_Y - 10, 48, 24, new Color(128,118,108)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 245, GROUND_LEVEL_Y -  4, 78, 30, new Color(112,104, 95)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 360, GROUND_LEVEL_Y - 12, 54, 26, new Color(122,112,102)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 470, GROUND_LEVEL_Y -  6, 70, 30, new Color(110,102, 94)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 560, GROUND_LEVEL_Y - 10, 58, 26, new Color(124,114,104)));

        Color stem = new Color(22, 140, 90);
        Color leaf = new Color(34, 170, 110);
        plant1 = new Plant(AQUARIUM_BOUNDS.x + 110, GROUND_LEVEL_Y,  85, 0.6, stem, leaf);
        plant2 = new Plant(AQUARIUM_BOUNDS.x + 210, GROUND_LEVEL_Y, 120, 0.9, stem, leaf);
        plant3 = new Plant(AQUARIUM_BOUNDS.x + 320, GROUND_LEVEL_Y,  95, 0.7, stem, leaf);
        plant4 = new Plant(AQUARIUM_BOUNDS.x + 520, GROUND_LEVEL_Y, 110, 0.8, stem, leaf);
        dynamics.add(plant1); dynamics.add(plant2); dynamics.add(plant3); dynamics.add(plant4);

        int chestW = 140, chestH = 70, chestLid = 18;
        int chestX = AQUARIUM_BOUNDS.x + 340;
        int chestY = GROUND_LEVEL_Y - chestH + 6;
        chest = new Chest(chestX, chestY, chestW, chestH, chestLid);
        statics.add(chest);
        dynamics.add(chest);

        // звезда в правом верхнем углу
        int starX = AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width - 50;
        int starY = AQUARIUM_BOUNDS.y + 40;
        star = new Starfish(starX, starY, 36, new Color(255,150,100), new Color(210,90,60));
        dynamics.add(star);

        // рыбки (если есть класс Fish)
        dynamics.add(new Fish(AQUARIUM_BOUNDS.x + 220, GROUND_LEVEL_Y - 120, 70, 38,
                new Color(255, 125, 90), new Color(255, 200, 170)));
        dynamics.add(new Fish(AQUARIUM_BOUNDS.x + 430, GROUND_LEVEL_Y - 160, 60, 32,
                new Color(120, 200, 255), new Color(200, 240, 255)));
        dynamics.add(new Fish(AQUARIUM_BOUNDS.x + 520, GROUND_LEVEL_Y -  90, 46, 26,
                new Color(250, 220, 120), new Color(255, 245, 180)));

        statics.add(new CabinetHalf(AQUARIUM_BOUNDS));

        sandEmitT   = randRange(0.4, 1.0);
        plantsEmitT = randRange(0.2, 1.2);
        stonesEmitT = randRange(0.6, 1.4);
        starEmitT   = randRange(0.8, 1.6);
        columnEmitT = randRange(0.5, 1.5);

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

            // --- МАССОВЫЙ ЗАЛП из сундука при открытии ---
            int toEmit = chest.bubblesToEmit(dt);
            if (toEmit > 0) {
                Point p = chest.getBubbleOrigin();
                for (int i = 0; i < toEmit; i++) {
                    int ox = (int) (p.x + (Math.random() * 12 - 6)); // чуть шире веер
                    int oy = (int) (p.y + (Math.random() * 8  - 3));
                    dynamics.add(new Bubble(ox, oy));
                }
            }

            // --- фоновые пузыри по всей сцене ---
            spawnAmbientBubbles(dt);

            // очистка умерших пузырей
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

    private void spawnAmbientBubbles(double dt) {
        final int waterLeft   = AQUARIUM_BOUNDS.x + 8;
        final int waterRight  = AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width - 8;
        final int waterTop    = AQUARIUM_BOUNDS.y + 8;
        final int sandY       = GROUND_LEVEL_Y - 2;

        sandEmitT -= dt;
        if (sandEmitT <= 0) {
            sandEmitT = randRange(sandEmitPeriodMin, sandEmitPeriodMax);
            int x = (int) randRange(waterLeft + 10, waterRight - 10);
            int y = sandY;
            dynamics.add(new Bubble(x, y));
        }

        plantsEmitT -= dt;
        if (plantsEmitT <= 0) {
            plantsEmitT = randRange(plantsEmitPeriodMin, plantsEmitPeriodMax);
            int idx = Math.min(3, Math.max(0, (int) randRange(0, 4)));
            int baseX = switch (idx) {
                case 0 -> AQUARIUM_BOUNDS.x + 110;
                case 1 -> AQUARIUM_BOUNDS.x + 210;
                case 2 -> AQUARIUM_BOUNDS.x + 320;
                default -> AQUARIUM_BOUNDS.x + 520;
            };
            int x = (int) (baseX + randRange(-8, 8));
            int y = (int) (GROUND_LEVEL_Y - randRange(30, 70));
            dynamics.add(new Bubble(x, y));
        }

        stonesEmitT -= dt;
        if (stonesEmitT <= 0) {
            stonesEmitT = randRange(stonesEmitPeriodMin, stonesEmitPeriodMax);
            int x = AQUARIUM_BOUNDS.x + (int) randRange(80, 560);
            int y = sandY - (int) randRange(4, 14);
            dynamics.add(new Bubble(x, y));
        }

        starEmitT -= dt;
        if (starEmitT <= 0) {
            starEmitT = randRange(starEmitPeriodMin, starEmitPeriodMax);
            int x = AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width - 50 + (int) randRange(-6, 6);
            int y = AQUARIUM_BOUNDS.y + 40 + (int) randRange(-6, 6);
            dynamics.add(new Bubble(x, y));
        }

        columnEmitT -= dt;
        if (columnEmitT <= 0) {
            columnEmitT = randRange(columnEmitPeriodMin, columnEmitPeriodMax);
            int x = (int) randRange(waterLeft + 20, waterRight - 20);
            int y = (int) randRange(waterTop + 40, GROUND_LEVEL_Y - 40);
            dynamics.add(new Bubble(x, y));
        }
    }

    private double randRange(double a, double b) { return a + Math.random() * (b - a); }

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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
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