package ru.vsu.cs.cg.pronin_s_v.task1_primitives.ui;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    public static final int WINDOW_WIDTH  = 1100;
    public static final int WINDOW_HEIGHT = 700;

    // Аквариум
    public static final Rectangle AQUARIUM_BOUNDS = new Rectangle(140, 120, 620, 320);
    public static final int GROUND_LEVEL_Y = AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height - 32;

    // Параметры песка
    private static final double SAND_WAVE_AMPLITUDE  = 6.0;
    private static final double SAND_WAVE_LENGTH     = 80.0;
    private static final int    SAND_WAVE_STEP       = 8;

    private final List<Static> statics = new ArrayList<>();
    private Aquarium aquarium;

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(new Color(236, 230, 220));
        setDoubleBuffered(true);

        statics.add(new BackgroundWall(WINDOW_WIDTH, WINDOW_HEIGHT));

        aquarium = new Aquarium(
                AQUARIUM_BOUNDS,
                GROUND_LEVEL_Y,
                SAND_WAVE_AMPLITUDE,
                SAND_WAVE_LENGTH,
                SAND_WAVE_STEP
        );
        statics.add(aquarium);

        // Камни
        statics.add(new Stone(AQUARIUM_BOUNDS.x +  80, GROUND_LEVEL_Y -  6, 62, 28, new Color(116,108, 98)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 160, GROUND_LEVEL_Y - 10, 48, 24, new Color(128,118,108)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 245, GROUND_LEVEL_Y -  4, 78, 30, new Color(112,104, 95)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 360, GROUND_LEVEL_Y - 12, 54, 26, new Color(122,112,102)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 470, GROUND_LEVEL_Y -  6, 70, 30, new Color(110,102, 94)));
        statics.add(new Stone(AQUARIUM_BOUNDS.x + 560, GROUND_LEVEL_Y - 10, 58, 26, new Color(124,114,104)));

        // Растения
        Color stem = new Color(22, 140, 90);
        Color leaf = new Color(34, 170, 110);
        statics.add(new Plant(AQUARIUM_BOUNDS.x + 110, GROUND_LEVEL_Y,  85, 0.6, stem, leaf));
        statics.add(new Plant(AQUARIUM_BOUNDS.x + 210, GROUND_LEVEL_Y, 120, 0.9, stem, leaf));
        statics.add(new Plant(AQUARIUM_BOUNDS.x + 320, GROUND_LEVEL_Y,  95, 0.7, stem, leaf));
        statics.add(new Plant(AQUARIUM_BOUNDS.x + 520, GROUND_LEVEL_Y, 110, 0.8, stem, leaf));

        // Сундук
        int chestW = 140, chestH = 70, chestLid = 18;
        int chestX = AQUARIUM_BOUNDS.x + 340;
        int chestY = GROUND_LEVEL_Y - chestH + 6;
        statics.add(new Chest(chestX, chestY, chestW, chestH, chestLid));

        statics.add(new CabinetHalf(AQUARIUM_BOUNDS));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        for (Static s : statics) s.draw(g2);
        aquarium.drawGlassOverlay(g2);

        g2.dispose();
    }
}