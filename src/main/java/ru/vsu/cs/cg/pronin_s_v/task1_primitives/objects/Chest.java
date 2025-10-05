package ru.vsu.cs.cg.pronin_s_v.task1_primitives.objects;

import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Dynamic;
import ru.vsu.cs.cg.pronin_s_v.task1_primitives.core.Static;

import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Chest implements Static, Dynamic {

    private final int x, y, w, chestHeight, lidHeight;

    // --- кинематика крышки ---
    private static final double MAX_ANGLE_DEG = 28.0; // умеренное раскрытие
    private double angleDeg = 0.0;
    private double angVelDeg = 0.0;
    private double targetDeg = 0.0;

    private static final double K = 120.0;  // жёсткость пружины
    private static final double C = 16.0;   // демпфирование
    private static final double ANGLE_EPS = 0.5;
    private static final double VEL_EPS   = 1.5;

    // паузы
    private double holdTimer = 0.0;
    private static final double HOLD_OPEN_SEC   = 0.8;
    private static final double HOLD_CLOSED_SEC = 1.0;

    // детектор открытия
    private double prevAngleDeg = 0.0;
    private boolean openingNow = false;
    private boolean wasOpeningPrev = false;

    // --- пузырьки: «залп» при начале открытия + редкая очередь пока открываемся ---
    private final Random rnd = new Random();

    // окно угла, когда уместно пускать пузыри
    private static final double EMIT_MIN_ANGLE = 5.0;
    private static final double EMIT_MAX_ANGLE = 0.95 * MAX_ANGLE_DEG;

    // очередь залпа
    private int burstLeft = 0;            // сколько пузырей осталось выпустить в текущем залпе
    private double burstCooldown = 0.0;   // время до следующего пузыря в залпе
    private static final double BURST_CD_MIN = 0.012;  // между пузырями в залпе (12..28 мс)
    private static final double BURST_CD_MAX = 0.028;

    // редкая очередь (после залпа, пока всё ещё открываемся)
    private double trickleTimer = 0.0;
    private static final double TRICKLE_PERIOD_MIN = 0.12; // 1 пузырь раз в 0.12..0.2 с
    private static final double TRICKLE_PERIOD_MAX = 0.20;

    // точка «щели»
    private int mouthX, mouthY;

    public Chest(int x, int y, int width, int height, int lidHeight) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.chestHeight = height;
        this.lidHeight = lidHeight;

        this.targetDeg = 0.0;

        this.mouthX = x + w / 2;
        this.mouthY = y - 1;

        this.trickleTimer = rand(TRICKLE_PERIOD_MIN, TRICKLE_PERIOD_MAX);
    }

    // ==== API для GamePanel ====
    public Point getBubbleOrigin() { return new Point(mouthX, mouthY); }

    /**
     * Сколько пузырей нужно выпустить на этом кадре.
     *  - На РАЗВОРОТЕ «закрыто→открывается» стартует залп (10..18 пузырей быстро).
     *  - Пока продолжается открытие — редкая очередь (по одному).
     */
    public int bubblesToEmit(double dt) {
        int count = 0;

        // если угол вне "рабочего окна", не выпускаем
        if (angleDeg < EMIT_MIN_ANGLE || angleDeg > EMIT_MAX_ANGLE) {
            burstLeft = 0;
            burstCooldown = 0;
            return 0;
        }

        // залп: пока есть «боезапас» — выдаём серию пузырей с маленьким интервалом
        if (burstLeft > 0) {
            burstCooldown -= dt;
            while (burstLeft > 0 && burstCooldown <= 0.0) {
                count++;
                burstLeft--;
                burstCooldown += rand(BURST_CD_MIN, BURST_CD_MAX);
                // чтобы не выпускать бесконечно много за один кадр
                if (count >= 6) break; // кап ограничивает ~6 пузырей за кадр
            }
        } else {
            // если не идёт залп, но мы всё ещё ОТКРЫВАЕМСЯ — редкая очередь
            if (openingNow) {
                trickleTimer -= dt;
                if (trickleTimer <= 0.0) {
                    count = 1;
                    trickleTimer = rand(TRICKLE_PERIOD_MIN, TRICKLE_PERIOD_MAX);
                }
            }
        }

        return count;
    }
    // ===========================

    @Override
    public void update(double dt) {
        prevAngleDeg = angleDeg;

        if (holdTimer > 0) {
            holdTimer -= dt;
            if (holdTimer <= 0) {
                targetDeg = (targetDeg < 1.0) ? MAX_ANGLE_DEG : 0.0;
            }
        } else {
            double diff = angleDeg - targetDeg;
            double acc  = -K * diff - C * angVelDeg;
            angVelDeg += acc * dt;
            angleDeg  += angVelDeg * dt;

            if (angleDeg < 0) angleDeg = 0;
            if (angleDeg > MAX_ANGLE_DEG) angleDeg = MAX_ANGLE_DEG;

            if (Math.abs(angleDeg - targetDeg) < ANGLE_EPS && Math.abs(angVelDeg) < VEL_EPS) {
                angleDeg = targetDeg;
                angVelDeg = 0;
                if (targetDeg <= 0.0) {
                    holdTimer = HOLD_CLOSED_SEC;
                    // полный сброс, чтобы следующий цикл снова дал залп
                    burstLeft = 0;
                    burstCooldown = 0;
                    trickleTimer = rand(TRICKLE_PERIOD_MIN, TRICKLE_PERIOD_MAX);
                } else {
                    holdTimer = HOLD_OPEN_SEC;
                }
            }
        }

        // реальное открытие = угол вырос
        openingNow = angleDeg - prevAngleDeg > 0.05;

        // детект фронта «началось открытие» (было не открытие → стало открытие)
        if (openingNow && !wasOpeningPrev) {
            // стартуем залп, только если мы уже вылезли из совсем малого угла (чтобы не «в тишине»)
            if (angleDeg >= EMIT_MIN_ANGLE) {
                burstLeft = 10 + rnd.nextInt(9); // 10..18
                burstCooldown = 0.0;             // сразу пойдёт первая порция на ближайшем кадре
            }
        }
        wasOpeningPrev = openingNow;

        // точка «щели» (центр фронта)
        mouthX = x + w / 2;
        mouthY = y - 1;
    }

    @Override
    public void draw(Graphics2D g2) {
        Object aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // корпус
        RoundRectangle2D body = new RoundRectangle2D.Double(x, y, w, chestHeight, 14, 14);
        g2.setPaint(new GradientPaint(x, y, new Color(120, 78, 40),
                x, y + chestHeight, new Color(80, 52, 28)));
        g2.fill(body);

        // рейки
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(95, 62, 33));
        for (int i = 1; i <= 3; i++) {
            int yy = y + i * (chestHeight / 4);
            g2.drawLine(x + 10, yy, x + w - 10, yy);
        }

        // мягкая тень от крышки
        if (angleDeg > 2.0) {
            int shadowH = (int) Math.min(12, 3 + angleDeg * 0.12);
            Composite oldC = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setPaint(new GradientPaint(x, y, new Color(0,0,0,60),
                    x, y + shadowH, new Color(0,0,0,0)));
            g2.fill(new Rectangle2D.Double(x + 6, y, w - 12, shadowH));
            g2.setComposite(oldC);
        }

        // крышка: фронтальная, всегда видна задняя кромка
        double rad = Math.toRadians(angleDeg);
        double depth = lidHeight + 8;

        double perspectiveBase = 6.0;
        double perspective = (1 - Math.cos(rad)) * perspectiveBase;
        double minP = Math.min(3.0, w * 0.02), maxP = Math.min(10.0, w * 0.07);
        perspective = Math.max(minP, Math.min(maxP, perspective));

        double backRise = depth * Math.sin(rad);

        Polygon lidTop = new Polygon(
                new int[]{ x,           x + w,           (int)(x + w - perspective), (int)(x + perspective) },
                new int[]{ y,           y,               (int)(y - backRise),         (int)(y - backRise)     },
                4
        );

        int frontT = Math.max(6, lidHeight / 3);
        Rectangle2D lidFront = new Rectangle2D.Double(x, y - frontT, w, frontT);

        int backT = 7;
        double backX = x + perspective;
        double backW = Math.max(10, w - 2 * perspective);
        double backY = y - backRise - backT;
        Shape lidBackStrip = new Rectangle2D.Double(backX, backY, backW, backT);

        g2.setPaint(new GradientPaint(x, (float)(y - backRise), new Color(135, 90, 48),
                x, y,                      new Color(92,  62, 34)));
        g2.fill(lidTop);

        g2.setColor(new Color(60, 40, 26, 200));
        g2.setStroke(new BasicStroke(1.6f));
        g2.draw(lidTop);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawLine(x + 12, y - frontT + 2, x + w - 12, y - frontT + 2);

        g2.setPaint(new GradientPaint(x, y - frontT, new Color(100,70,42),
                x, y,          new Color(70, 50,30)));
        g2.fill(lidFront);

        if (angleDeg > 1.0) {
            g2.setColor(new Color(80, 55, 35, 230));
            g2.fill(lidBackStrip);
            g2.setColor(new Color(255, 255, 255, 110));
            g2.draw(new Line2D.Double(backX + 1.5, backY + 1, backX + backW - 1.5, backY + 1));
            g2.setColor(new Color(40, 28, 18, 200));
            g2.draw(new Line2D.Double(backX + 1, backY + backT - 0.6, backX + backW - 1, backY + backT - 0.6));
        }

        // уголки
        g2.setColor(new Color(160, 140, 90));
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 8,     y - lidHeight + 8, x + 8,     y - 8);
        g2.drawLine(x + w - 8, y - lidHeight + 8, x + w - 8, y - 8);

        // замок
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
        g2.fill(new Rectangle2D.Double(lockX + lockW/2.0 - 2, lockY + 6, 4, 6));
        g2.fill(new Ellipse2D.Double(lockX + lockW/2.0 - 3, lockY + 12, 6, 6));
    }

    private double rand(double a, double b) { return a + rnd.nextDouble() * (b - a); }
}