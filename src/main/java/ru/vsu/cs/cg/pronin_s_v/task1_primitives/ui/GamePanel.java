package ru.vsu.cs.cg.pronin_s_v.task1_primitives.ui;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects.Plant;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects.Stone;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Главная панель сцены.
 * Содержит игровой цикл (таймер), списки объектов и базовый рендер фона+аквариума.
 * Объекты-сущности (камни, растения, рыбы, пузырьки) будем добавлять по мере готовности.
 */
public class GamePanel extends JPanel {
    // === Габариты окна ===
    public static final int WINDOW_WIDTH  = 900;
    public static final int WINDOW_HEIGHT = 540;

    // === Аквариум ===
    public static final Rectangle AQUARIUM_BOUNDS = new Rectangle(140, 120, 620, 320);
    public static final int GROUND_LEVEL_Y = AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height - 32;

    // Волна песка (параметры кромки)
    private static final double SAND_WAVE_AMPLITUDE  = 6.0;
    private static final double SAND_WAVE_LENGTH     = 80.0;
    private static final int    SAND_WAVE_STEP       = 8;

    // === Сцена ===
    private final List<Dynamic> dynamics = new ArrayList<>();
    private final List<Static> aStatics = new ArrayList<>();

    // Таймер и время кадра
    private final Timer repaintTimer;
    private long lastFrameTimeNs = System.nanoTime();

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(new Color(236, 230, 220)); // общий фон «комнаты»
        setDoubleBuffered(true);

        // Игровой цикл ~60 FPS
        repaintTimer = new Timer(16, e -> onFrame());
        repaintTimer.start();

        // === Добавляем камни ===
        addDrawable(new Stone(AQUARIUM_BOUNDS.x + 80,  GROUND_LEVEL_Y -  6, 62, 28, new Color(116,108, 98)));
        addDrawable(new Stone(AQUARIUM_BOUNDS.x + 160, GROUND_LEVEL_Y - 10, 48, 24, new Color(128,118,108)));
        addDrawable(new Stone(AQUARIUM_BOUNDS.x + 245, GROUND_LEVEL_Y -  4, 78, 30, new Color(112,104, 95)));
        addDrawable(new Stone(AQUARIUM_BOUNDS.x + 360, GROUND_LEVEL_Y - 12, 54, 26, new Color(122,112,102)));
        addDrawable(new Stone(AQUARIUM_BOUNDS.x + 470, GROUND_LEVEL_Y -  6, 70, 30, new Color(110,102, 94)));
        addDrawable(new Stone(AQUARIUM_BOUNDS.x + 560, GROUND_LEVEL_Y - 10, 58, 26, new Color(124,114,104)));

        Color stemColor = new Color(22, 140, 90);
        Color leafColor = new Color(34, 170, 110);

        // === Добавляем водросли ===
        addDrawable(new Plant(AQUARIUM_BOUNDS.x + 110, GROUND_LEVEL_Y,  85, 0.6, stemColor, leafColor));
        addDrawable(new Plant(AQUARIUM_BOUNDS.x + 210, GROUND_LEVEL_Y, 120, 0.9, stemColor, leafColor));
        addDrawable(new Plant(AQUARIUM_BOUNDS.x + 320, GROUND_LEVEL_Y,  95, 0.7, stemColor, leafColor));
        addDrawable(new Plant(AQUARIUM_BOUNDS.x + 520, GROUND_LEVEL_Y, 110, 0.8, stemColor, leafColor));
    }

    private void onFrame() {
        long now = System.nanoTime();
        double dt = (now - lastFrameTimeNs) / 1e9;
        lastFrameTimeNs = now;

        // Обновление всех динамических объектов (рыбы, пузырьки и т.п.)
        for (Dynamic u : dynamics) {
            u.update(dt);
        }
        repaint();
    }

    public void addUpdatable(Dynamic u) { dynamics.add(u); }

    public void addDrawable(Static d) { aStatics.add(d); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Обои
        drawRoomBackground(g2);

        // 2) Вода
        drawWater(g2);

        // 3) Песок
        drawSand(g2);

        // 4) Объекты сцены (камни, растения, рыбы, пузырьки и т.п.)
        for (Static d : aStatics) {
            d.draw(g2);
        }

        // 5) Крышка + рамка аквариума
        drawLid(g2);
        drawFrame(g2);

        g2.dispose();
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ РЕНДЕРЫ (фон+аквариум) ==================

    /** Фон комнаты с простым узором «обоев» */
    private void drawRoomBackground(Graphics2D g2) {
        g2.setColor(new Color(245, 239, 232));
        g2.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        g2.setColor(new Color(228, 218, 206));
        for (int y = 20; y < WINDOW_HEIGHT; y += 40) {
            for (int x = 20; x < WINDOW_WIDTH; x += 40) {
                g2.fillRoundRect(x, y, 12, 12, 3, 3);
            }
        }
    }

    /** Вода внутри аквариума (градиент) */
    private void drawWater(Graphics2D g2) {
        Shape aquariumShape = new RoundRectangle2D.Double(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y,
                AQUARIUM_BOUNDS.width, AQUARIUM_BOUNDS.height,
                20, 20
        );
        GradientPaint water = new GradientPaint(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y, new Color(70, 160, 220),
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height, new Color(20, 70, 120)
        );
        g2.setPaint(water);
        g2.fill(aquariumShape);
    }

    /** Песок (волнистая верхняя кромка + заливка) */
    private void drawSand(Graphics2D g2) {
        Path2D sandArea = buildSandArea();
        g2.setPaint(new GradientPaint(
                AQUARIUM_BOUNDS.x, (float) (GROUND_LEVEL_Y - 12),
                new Color(170, 130, 70),
                AQUARIUM_BOUNDS.x, (float) (GROUND_LEVEL_Y + 30),
                new Color(120, 85, 45)
        ));
        g2.fill(sandArea);
    }

    /** Крышка аквариума */
    private void drawLid(Graphics2D g2) {
        int sidePadding = 6;
        int lidHeight   = 14;

        RoundRectangle2D lidShape = new RoundRectangle2D.Double(
                AQUARIUM_BOUNDS.x - sidePadding,
                AQUARIUM_BOUNDS.y - lidHeight - 2,
                AQUARIUM_BOUNDS.width + sidePadding * 2,
                lidHeight,
                10, 10
        );

        g2.setPaint(new GradientPaint(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y - lidHeight, new Color(100,110,120),
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y,              new Color( 60, 70, 80)
        ));
        g2.fill(lidShape);

        // верхний светлый блик
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(230, 240, 250, 140));
        g2.drawLine(
                AQUARIUM_BOUNDS.x - sidePadding + 8,
                AQUARIUM_BOUNDS.y - lidHeight + 2,
                AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width + sidePadding - 8,
                AQUARIUM_BOUNDS.y - lidHeight + 2
        );

        g2.setColor(new Color(60, 70, 80));
        g2.draw(lidShape);
    }

    /** Рамка аквариума (градиент, как у крышки) */
    private void drawFrame(Graphics2D g2) {
        Shape aquariumShape = new RoundRectangle2D.Double(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y,
                AQUARIUM_BOUNDS.width, AQUARIUM_BOUNDS.height,
                20, 20
        );
        g2.setStroke(new BasicStroke(8f));
        GradientPaint frameGradient = new GradientPaint(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y, new Color(100,110,120),
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height, new Color(60,70,80)
        );
        g2.setPaint(frameGradient);
        g2.draw(aquariumShape);
    }

    // Построение области песка (волнистая верхняя граница + низ)
    private Path2D buildSandArea() {
        final int left  = AQUARIUM_BOUNDS.x + 4;
        final int right = AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width - 4;

        Path2D wave = new Path2D.Double();
        wave.moveTo(left, GROUND_LEVEL_Y - 8);
        for (int x = left; x <= right; x += SAND_WAVE_STEP) {
            double t = (x - left) / SAND_WAVE_LENGTH;
            double y = (GROUND_LEVEL_Y - 8) + Math.sin(t * 2 * Math.PI) * SAND_WAVE_AMPLITUDE;
            wave.lineTo(x, y);
        }
        wave.lineTo(right, GROUND_LEVEL_Y + 30);
        wave.lineTo(left,  GROUND_LEVEL_Y + 30);
        wave.closePath();
        return wave;
    }
}