package ru.vsu.cs.cg.pronin_s_v.task1_primitives;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

/**
 * Основная панель отрисовки аквариума.
 * Здесь рисуются:
 *  - фон комнаты (обои)
 *  - аквариум (вода, рамка, крышка)
 *  - грунт (песок)
 *  - камни
 *  - растения
 */
public class GamePanel extends JPanel {
    public static final int WINDOW_WIDTH  = 900;
    public static final int WINDOW_HEIGHT = 540;

    // Рамка аквариума
    public static final Rectangle AQUARIUM_BOUNDS = new Rectangle(140, 120, 620, 320);

    // Уровень грунта
    public static final int GROUND_LEVEL_Y = AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height - 32;

    // Параметры волнистой линии песка
    private static final double SAND_WAVE_AMPLITUDE  = 6.0;   // высота волны
    private static final double SAND_WAVE_LENGTH     = 80.0;  // длина волны
    private static final int    SAND_WAVE_STEP       = 8;     // шаг дискретизации

    // Таймер для перерисовки (60 FPS)
    private final Timer repaintTimer;
    private long lastFrameTimeNs;

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(new Color(236, 230, 220)); // фон комнаты
        setDoubleBuffered(true);

        // Таймер срабатывает каждые ~16 мс
        repaintTimer = new Timer(16, e -> onFrameUpdate());
        repaintTimer.start();
        lastFrameTimeNs = System.nanoTime();
    }

    /** Обновление кадра (здесь позже будет логика движения рыбок, пузырьков и т.д.) */
    private void onFrameUpdate() {
        long now = System.nanoTime();
        double deltaSeconds = (now - lastFrameTimeNs) / 1e9;
        lastFrameTimeNs = now;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // 1. Обои
        drawRoomBackground(g2);

        // 2. Вода
        Shape aquariumShape = new RoundRectangle2D.Double(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y,
                AQUARIUM_BOUNDS.width, AQUARIUM_BOUNDS.height,
                20, 20
        );
        drawWater(g2, aquariumShape);

        // 3. Грунт
        drawSand(g2);

        // 4. Камни
        drawRocks(g2);

        // 5. Растения
        drawPlants(g2);

        // 6. Крышка аквариума
        drawLid(g2);

        // 7. Рамка аквариума
        drawFrame(g2, aquariumShape);

        g2.dispose();
    }

    // ================= Фон =================

    /** Отрисовка фона комнаты с узором «обоев» */
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

    // ================= Аквариум =================

    /** Отрисовка воды внутри аквариума */
    private void drawWater(Graphics2D g2, Shape aquariumShape) {
        GradientPaint waterGradient = new GradientPaint(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y, new Color(70, 160, 220),
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height, new Color(20, 70, 120)
        );
        g2.setPaint(waterGradient);
        g2.fill(aquariumShape);
    }

    /** Рамка аквариума */
    private void drawFrame(Graphics2D g2, Shape aquariumShape) {
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setStroke(new BasicStroke(8f));
        GradientPaint frameGradient = new GradientPaint(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y,
                new Color(100, 110, 120),
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y + AQUARIUM_BOUNDS.height,
                new Color(60, 70, 80)
        );
        g2.setPaint(frameGradient);
        g2.draw(aquariumShape);
    }

    /** Крышка аквариума */
    private void drawLid(Graphics2D g2) {
        int sidePadding = 6;   // выступ крышки по бокам
        int lidHeight   = 14;  // высота крышки

        RoundRectangle2D lidShape = new RoundRectangle2D.Double(
                AQUARIUM_BOUNDS.x - sidePadding,
                AQUARIUM_BOUNDS.y - lidHeight - 2,
                AQUARIUM_BOUNDS.width + sidePadding * 2,
                lidHeight,
                10, 10
        );

        g2.setPaint(new GradientPaint(
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y - lidHeight,
                new Color(100, 110, 120),
                AQUARIUM_BOUNDS.x, AQUARIUM_BOUNDS.y,
                new Color(60, 70, 80)
        ));
        g2.fill(lidShape);

        // светлая линия блика
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

    // ================= Грунт =================

    /** Отрисовка грунта */
    private void drawSand(Graphics2D g2) {
        Path2D sandShape = buildSandArea();

        g2.setPaint(new GradientPaint(
                AQUARIUM_BOUNDS.x, (float) (GROUND_LEVEL_Y - 12),
                new Color(170, 130, 70),
                AQUARIUM_BOUNDS.x, (float) (GROUND_LEVEL_Y + 30),
                new Color(120, 85, 45)
        ));
        g2.fill(sandShape);
    }

    /** Волнистая линия песка (верхняя кромка) */
    private Path2D buildSandWavePath() {
        final int left  = AQUARIUM_BOUNDS.x + 2;
        final int right = AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width - 2;

        Path2D path = new Path2D.Double();
        path.moveTo(left, GROUND_LEVEL_Y - 8);
        for (int x = left; x <= right; x += SAND_WAVE_STEP) {
            double t = (x - left) / SAND_WAVE_LENGTH;
            double y = (GROUND_LEVEL_Y - 8) + Math.sin(t * 2 * Math.PI) * SAND_WAVE_AMPLITUDE;
            path.lineTo(x, y);
        }
        return path;
    }

    /** Полная область песка (волна + низ) */
    private Path2D buildSandArea() {
        final int left  = AQUARIUM_BOUNDS.x + 4;
        final int right = AQUARIUM_BOUNDS.x + AQUARIUM_BOUNDS.width - 4;

        Path2D area = (Path2D) buildSandWavePath().clone();
        area.lineTo(right, GROUND_LEVEL_Y + 30);
        area.lineTo(left,  GROUND_LEVEL_Y + 30);
        area.closePath();
        return area;
    }

    // ================= Камни =================

    /** Камни на дне */
    private void drawRocks(Graphics2D g2) {
        drawRock(g2, AQUARIUM_BOUNDS.x +  80, GROUND_LEVEL_Y -  6,  62, 28, new Color(116, 108,  98));
        drawRock(g2, AQUARIUM_BOUNDS.x + 160, GROUND_LEVEL_Y - 10,  48, 24, new Color(128, 118, 108));
        drawRock(g2, AQUARIUM_BOUNDS.x + 245, GROUND_LEVEL_Y -  4,  78, 30, new Color(112, 104,  95));
        drawRock(g2, AQUARIUM_BOUNDS.x + 360, GROUND_LEVEL_Y - 12,  54, 26, new Color(122, 112, 102));
        drawRock(g2, AQUARIUM_BOUNDS.x + 470, GROUND_LEVEL_Y -  6,  70, 30, new Color(110, 102,  94));
        drawRock(g2, AQUARIUM_BOUNDS.x + 560, GROUND_LEVEL_Y - 10,  58, 26, new Color(124, 114, 104));
    }

    /** Один камень */
    private void drawRock(Graphics2D g2, int centerX, int centerY, int width, int height, Color baseColor) {
        // Тень под камнем
        Shape shadow = new Ellipse2D.Double(centerX - width * 0.45, centerY + height * 0.15, width * 0.9, height * 0.35);
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(20, 20, 20, 180));
        g2.fill(shadow);
        g2.setComposite(oldComp);

        // Камень
        Shape rockShape = new Ellipse2D.Double(centerX - width / 2.0, centerY - height / 2.0, width, height);
        g2.setPaint(baseColor);
        g2.fill(rockShape);

        // Блик сверху
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.14f));
        g2.setPaint(new GradientPaint(
                (float) centerX, (float) (centerY - height / 2.0),
                new Color(255, 255, 255, 180),
                (float) centerX, (float) (centerY + height / 3.0),
                new Color(255, 255, 255,   0)
        ));
        g2.fill(rockShape);
        g2.setComposite(oldComp);

        // Контур
        g2.setStroke(new BasicStroke(1.1f));
        g2.setColor(new Color(0, 0, 0, 35));
        g2.draw(rockShape);
    }

    // ================= Растения =================

    /** Группа растений */
    private void drawPlants(Graphics2D g2) {
        Color stemColor = new Color(22, 140, 90);   // стебель
        Color leafColor = new Color(34, 170, 110);  // листья

        drawPlant(g2, AQUARIUM_BOUNDS.x + 110, GROUND_LEVEL_Y, 85,  0.6, stemColor, leafColor);
        drawPlant(g2, AQUARIUM_BOUNDS.x + 210, GROUND_LEVEL_Y, 120, 0.9, stemColor, leafColor);
        drawPlant(g2, AQUARIUM_BOUNDS.x + 320, GROUND_LEVEL_Y, 95,  0.7, stemColor, leafColor);
        drawPlant(g2, AQUARIUM_BOUNDS.x + 520, GROUND_LEVEL_Y, 110, 0.8, stemColor, leafColor);
    }

    /** Одна водоросль */
    private void drawPlant(Graphics2D g2, int baseX, int baseY, int height, double sway, Color stemColor, Color leafColor) {
        int ctrlX = (int) (baseX + 30 * sway);
        int ctrlY = baseY - height / 2;
        int topX  = baseX;
        int topY  = baseY - height;

        // стебель
        QuadCurve2D stemCurve = new QuadCurve2D.Double(baseX, baseY, ctrlX, ctrlY, topX, topY);
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(stemColor);
        g2.draw(stemCurve);
        g2.setStroke(oldStroke);

        // листья (эллипсы вдоль кривой)
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