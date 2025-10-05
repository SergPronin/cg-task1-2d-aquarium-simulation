package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Сундук: пузыри только когда крышка ОТКРЫТА (стоит, не движется).
 * - Нет залпов на открытии/закрытии.
 * - Эмиссия включается при устойчивом открытом состоянии и выключается при его потере.
 * - Точка эмиссии следует за «щелью».
 */
public class Chest implements Static, Dynamic {

    private final int x, y, w, chestHeight, lidHeight;

    // Кинематика крышки (градусы)
    private static final double MAX_ANGLE_DEG = 35.0;
    private double angleDeg = 0.0;
    private double angVelDeg = 0.0;
    private double targetDeg = 0.0;

    private static final double K = 200.0;   // жёсткость пружины
    private static final double C = 10.0;    // демпфирование
    private static final double ANGLE_EPS = 0.5;
    private static final double VEL_EPS = 1.5;

    // Паузы
    private double holdTimer = 0.0;
    private static final double HOLD_OPEN_SEC = 1.2;
    private static final double HOLD_CLOSED_SEC = 1.8;

    // «Открыто» = крышка стоит и раскрыта
    private static final double OPEN_ON_ANGLE = 10.0;   // порог угла, чтобы считать крышку раскрытой
    private static final double VEL_STILL_EPS = 0.25;   // скорость, близкая к нулю
    private boolean openState = true;                  // текущее «состояние открыто»

    // Геометрия крышки (общая для update() и draw())
    private final int backT = 9;                    // толщина задней полосы
    private final double perspectiveBase = 8.0;     // базовая «перспектива»
    private final int frontT;                       // толщина фронт-борта (от lidHeight)

    // Пузырьки (только «частая очередь», без залпов)
    private final Random rnd = new Random();
    private double trickleTimer = 0.0;
    private static final double TRICKLE_PERIOD_MIN = 0.05; // 0.05..0.10 с
    private static final double TRICKLE_PERIOD_MAX = 0.10;

    // Точка «щели»
    private int mouthX, mouthY;

    public Chest(int x, int y, int width, int height, int lidHeight) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.chestHeight = height;
        this.lidHeight = lidHeight;

        this.frontT = Math.max(7, lidHeight / 3);

        this.targetDeg = 0.0;
        this.trickleTimer = rand(TRICKLE_PERIOD_MIN, TRICKLE_PERIOD_MAX);

        updateMouth(); // первичная геометрия щели
    }

    /** Точка эмиссии пузырьков (следует за щелью) */
    public Point getBubbleOrigin() {
        double rad = Math.toRadians(angleDeg);
        int offsetX = (int) Math.round(Math.sin(rad) * 4.0);
        int offsetY = (int) Math.round(Math.cos(rad) * Math.min(8, lidHeight * 0.25));
        return new Point(mouthX + offsetX, mouthY - offsetY);
    }

    /** Сколько пузырей выпустить за кадр dt (только когда состояние «открыто») */
    public int bubblesToEmit(double dt) {
        int count = 0;

        if (openState) {
            trickleTimer -= dt;
            while (trickleTimer <= 0.0 && count < 2) {     // ограничим на кадр
                count += 1 + rnd.nextInt(1);                // 1..3 пузыря
                trickleTimer += rand(TRICKLE_PERIOD_MIN, TRICKLE_PERIOD_MAX);
            }
        } else {
            // когда не открыто — ничего
        }

        return count;
    }

    @Override
    public void update(double dt) {
        if (dt > 0.05) dt = 0.05; // кап на dt (если окно было в фоне и т.п.)

        // Паузы в крайних положениях
        if (holdTimer > 0) {
            holdTimer -= dt;
            if (holdTimer <= 0) {
                targetDeg = (targetDeg < 1.0) ? MAX_ANGLE_DEG : 0.0;
            }
        } else {
            double diff = targetDeg - angleDeg;
            double acc = K * diff - C * angVelDeg;
            angVelDeg += acc * dt;
            angleDeg += angVelDeg * dt;

            // Ограничители и лёгкий bounce
            if (angleDeg < 0) {
                angleDeg = 0;
                angVelDeg *= -0.3;
            }
            if (angleDeg > MAX_ANGLE_DEG) {
                angleDeg = MAX_ANGLE_DEG;
                angVelDeg *= -0.3;
            }

            // Захват в цель и установка паузы
            if (Math.abs(angleDeg - targetDeg) < ANGLE_EPS && Math.abs(angVelDeg) < VEL_EPS) {
                angleDeg = targetDeg;
                angVelDeg = 0;
                holdTimer = (targetDeg <= 0.0) ? HOLD_CLOSED_SEC : HOLD_OPEN_SEC;
            }
        }

        // Обновляем «состояние открыто»:
        // 1) угол достаточно большой
        // 2) скорость мала (стоит)
        // 3) целевая позиция — открыто (или идёт удержание в открытом)
        boolean targetIsOpen = targetDeg >= MAX_ANGLE_DEG - 0.1;
        boolean angleOpenEnough = angleDeg >= OPEN_ON_ANGLE;
        boolean almostStill = Math.abs(angVelDeg) < VEL_STILL_EPS;

        openState = angleOpenEnough && almostStill && (targetIsOpen || (holdTimer > 0 && targetDeg >= MAX_ANGLE_DEG - 0.1));

        // Когда только что вошли в openState — перезапустим таймер «очереди»,
        // чтобы пузырьки не слипались между циклами.
        if (openState && trickleTimer > TRICKLE_PERIOD_MAX) {
            trickleTimer = rand(TRICKLE_PERIOD_MIN, TRICKLE_PERIOD_MAX);
        }

        // Пересчитать точку «щели»
        updateMouth();
    }

    /** Пересчёт координат «щели» с учётом текущего угла и перспективы */
    private void updateMouth() {
        double rad = Math.toRadians(angleDeg);

        double perspective = (1 - Math.cos(rad)) * perspectiveBase;
        double minP = Math.min(4.0, w * 0.02);
        double maxP = Math.min(14.0, w * 0.09);
        perspective = Math.max(minP, Math.min(maxP, perspective));

        double depth = lidHeight + 12;
        double backRise = depth * Math.sin(rad);

        // середина верхней плоскости между передним и задним ребром
        double topYFront = y - frontT;
        double topYBack = y - backRise - backT;

        this.mouthX = (int) Math.round(x + w / 2.0);
        this.mouthY = (int) Math.round((topYFront + topYBack) * 0.5);
    }

    @Override
    public void draw(Graphics2D g2) {
        Object aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Корпус
        RoundRectangle2D body = new RoundRectangle2D.Double(x, y, w, chestHeight, 14, 14);
        g2.setPaint(new GradientPaint(x, y, new Color(120, 78, 40),
                x, y + chestHeight, new Color(80, 52, 28)));
        g2.fill(body);

        // Рейки
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(95, 62, 33));
        for (int i = 1; i <= 3; i++) {
            int yy = y + i * (chestHeight / 4);
            g2.drawLine(x + 10, yy, x + w - 10, yy);
        }

        // Тень от крышки
        if (angleDeg > 2.0) {
            int shadowH = (int) Math.min(15, 5 + angleDeg * 0.2);
            Composite oldC = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2.setPaint(new GradientPaint(x, y, new Color(0, 0, 0, 80),
                    x, y + shadowH, new Color(0, 0, 0, 0)));
            g2.fill(new Rectangle2D.Double(x + 6, y, w - 12, shadowH));
            g2.setComposite(oldC);
        }

        // Крышка
        double rad = Math.toRadians(angleDeg);
        double depth = lidHeight + 12;

        double perspective = (1 - Math.cos(rad)) * perspectiveBase;
        double minP = Math.min(4.0, w * 0.02);
        double maxP = Math.min(14.0, w * 0.09);
        perspective = Math.max(minP, Math.min(maxP, perspective));

        double backRise = depth * Math.sin(rad);

        Polygon lidTop = new Polygon(
                new int[]{x, x + w, (int) (x + w - perspective), (int) (x + perspective)},
                new int[]{y, y, (int) (y - backRise), (int) (y - backRise)},
                4
        );

        Rectangle2D lidFront = new Rectangle2D.Double(x, y - frontT, w, frontT);

        double backX = x + perspective;
        double backW = Math.max(10, w - 2 * perspective);
        double backY = y - backRise - backT;
        Shape lidBackStrip = new Rectangle2D.Double(backX, backY, backW, backT);

        g2.setPaint(new GradientPaint(x, (float) (y - backRise), new Color(135, 90, 48),
                x, y, new Color(92, 62, 34)));
        g2.fill(lidTop);

        g2.setColor(new Color(60, 40, 26, 200));
        g2.setStroke(new BasicStroke(1.8f));
        g2.draw(lidTop);

        g2.setStroke(new BasicStroke(2.2f));
        g2.setColor(new Color(255, 255, 255, 140));
        g2.drawLine(x + 12, y - frontT + 2, x + w - 12, y - frontT + 2);

        g2.setPaint(new GradientPaint(x, y - frontT, new Color(100, 70, 42),
                x, y, new Color(70, 50, 30)));
        g2.fill(lidFront);

        if (angleDeg > 1.0) {
            g2.setColor(new Color(80, 55, 35, 230));
            g2.fill(lidBackStrip);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.draw(new Line2D.Double(backX + 1.5, backY + 1, backX + backW - 1.5, backY + 1));
            g2.setColor(new Color(40, 28, 18, 200));
            g2.draw(new Line2D.Double(backX + 1, backY + backT - 0.6, backX + backW - 1, backY + backT - 0.6));
        }

        // Уголки
        g2.setColor(new Color(160, 140, 90));
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 8, y - lidHeight + 8, x + 8, y - 8);
        g2.drawLine(x + w - 8, y - lidHeight + 8, x + w - 8, y - 8);

        // Замок
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

    private double rand(double a, double b) {
        return a + rnd.nextDouble() * (b - a);
    }
}