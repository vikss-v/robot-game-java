package model;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefenseModel {

    public enum GamePhase {
        WAITING,
        WAVE_ACTIVE,
        BETWEEN_WAVES,
        VICTORY,
        DEFEAT
    }

    public static class Robot {
        public double x;
        public double y;
        public double vx = 0;
        public double vy = 0;
        public double angle = 0;
        public final Color color;
        public final double radius = 18;
        public final double speed = 160;

        public Robot(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    public static class Worm {
        public double x;
        public double y;
        public boolean alive = true;
        public final Color color;
        public final double radius = 10;
        public final double speed;

        public Worm(double x, double y, Color color, double speed) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.speed = speed;
        }
    }

    public static final int TOTAL_WAVES      = 5;
    public static final int APPLE_MAX_HP     = 10;
    public static final double APPLE_RADIUS  = 22;
    public static final int BETWEEN_WAVES_MS = 3000;

    private static final int[] WORMS_PER_WAVE = {5, 8, 11, 15, 15};
    private static final double[] WORM_SPEED  = {55, 70, 90, 90, 120};

    private final int fieldWidth;
    private final int fieldHeight;

    private final Robot robot1;
    private final Robot robot2;

    private final Point apple;
    private int appleHp = APPLE_MAX_HP;

    private final List<Worm> worms = new ArrayList<>();
    private int currentWave = 0;
    private int wormsSpawnedThisWave = 0;
    private long lastSpawnTime = 0;
    private long waveEndTime = 0;

    private GamePhase phase = GamePhase.WAITING;

    private final Random random = new Random();
    private final List<DefenseModelListener> listeners = new CopyOnWriteArrayList<>();

    public DefenseModel(int fieldWidth, int fieldHeight) {
        this.fieldWidth  = fieldWidth;
        this.fieldHeight = fieldHeight;

        apple = new Point(fieldWidth / 2, fieldHeight / 2);

        robot1 = new Robot(apple.x - 80, apple.y, new Color(0x4A90D9));
        robot2 = new Robot(apple.x + 80, apple.y, new Color(0xFF6B35));
    }

    public void addListener(DefenseModelListener l)    { listeners.add(l); }
    public void removeListener(DefenseModelListener l) { listeners.remove(l); }

    private void notifyListeners() {
        for (DefenseModelListener l : listeners) l.onModelChanged(this);
    }

    public void setRobot1Direction(double vx, double vy) {
        robot1.vx = vx;
        robot1.vy = vy;
    }

    public void setRobot2Direction(double vx, double vy) {
        robot2.vx = vx;
        robot2.vy = vy;
    }

    public void startGame() {
        if (phase != GamePhase.WAITING) return;
        currentWave = 0;
        startNextWave();
    }

    private void startNextWave() {
        wormsSpawnedThisWave = 0;
        lastSpawnTime = System.currentTimeMillis();
        worms.clear();
        phase = GamePhase.WAVE_ACTIVE;
        notifyListeners();
    }

    public void tick(double dtSeconds) {
        if (phase == GamePhase.WAITING || phase == GamePhase.VICTORY || phase == GamePhase.DEFEAT) {
            return;
        }

        if (phase == GamePhase.BETWEEN_WAVES) {
            if (System.currentTimeMillis() >= waveEndTime) {
                startNextWave();
            }
            notifyListeners();
            return;
        }

        spawnWorms();
        moveRobots(dtSeconds);
        moveWorms(dtSeconds);
        checkCollisions();
        checkWaveComplete();

        notifyListeners();
    }

    private void spawnWorms() {
        int total = WORMS_PER_WAVE[currentWave];
        if (wormsSpawnedThisWave >= total) return;

        long now = System.currentTimeMillis();
        if (now - lastSpawnTime < 600) return;
        lastSpawnTime = now;

        double spawnX, spawnY;
        int side = random.nextInt(4);
        switch (side) {
            case 0:  spawnX = random.nextInt(fieldWidth);  spawnY = 0; break;
            case 1:  spawnX = random.nextInt(fieldWidth);  spawnY = fieldHeight; break;
            case 2:  spawnX = 0;           spawnY = random.nextInt(fieldHeight); break;
            default: spawnX = fieldWidth;  spawnY = random.nextInt(fieldHeight); break;
        }

        Color wormColor = new Color(
                100 + random.nextInt(100),
                150 + random.nextInt(80),
                50  + random.nextInt(80)
        );

        worms.add(new Worm(spawnX, spawnY, wormColor, WORM_SPEED[currentWave]));
        wormsSpawnedThisWave++;
    }

    private void moveRobots(double dt) {
        moveRobot(robot1, dt);
        moveRobot(robot2, dt);
    }

    private void moveRobot(Robot r, double dt) {
        double nx = r.x + r.vx * r.speed * dt;
        double ny = r.y + r.vy * r.speed * dt;

        nx = Math.max(r.radius, Math.min(fieldWidth  - r.radius, nx));
        ny = Math.max(r.radius, Math.min(fieldHeight - r.radius, ny));

        r.x = nx;
        r.y = ny;

        if (r.vx != 0 || r.vy != 0) {
            r.angle = Math.atan2(r.vy, r.vx);
        }
    }

    private void moveWorms(double dt) {
        for (Worm w : worms) {
            if (!w.alive) continue;

            double dx = apple.x - w.x;
            double dy = apple.y - w.y;
            double dist = Math.hypot(dx, dy);
            if (dist < 1) continue;

            w.x += (dx / dist) * w.speed * dt;
            w.y += (dy / dist) * w.speed * dt;
        }
    }

    private void checkCollisions() {
        for (Worm w : worms) {
            if (!w.alive) continue;

            if (circles(w.x, w.y, w.radius, robot1.x, robot1.y, robot1.radius) || circles(w.x, w.y, w.radius, robot2.x, robot2.y, robot2.radius)) {
                w.alive = false;
                continue;
            }

            if (circles(w.x, w.y, w.radius, apple.x, apple.y, APPLE_RADIUS)) {
                w.alive = false;
                appleHp--;
                if (appleHp <= 0) {
                    appleHp = 0;
                    phase = GamePhase.DEFEAT;
                    return;
                }
            }
        }
    }

    private boolean circles(double x1, double y1, double r1, double x2, double y2, double r2) {
        return Math.hypot(x1 - x2, y1 - y2) < r1 + r2;
    }

    private void checkWaveComplete() {
        int total = WORMS_PER_WAVE[currentWave];
        if (wormsSpawnedThisWave < total) return;

        boolean anyAlive = false;
        for (Worm w : worms) {
            if (w.alive) { anyAlive = true; break; }
        }
        if (anyAlive) return;

        currentWave++;
        if (currentWave >= TOTAL_WAVES) {
            phase = GamePhase.VICTORY;
        } else {
            phase = GamePhase.BETWEEN_WAVES;
            waveEndTime = System.currentTimeMillis() + BETWEEN_WAVES_MS;
        }
    }

    public Robot getRobot1()     { return robot1; }
    public Robot getRobot2()     { return robot2; }
    public Point getApple()      { return apple; }
    public int getAppleHp()      { return appleHp; }
    public List<Worm> getWorms() { return worms; }
    public int getCurrentWave()  { return currentWave; }
    public GamePhase getPhase()  { return phase; }
    public int getFieldWidth()   { return fieldWidth; }
    public int getFieldHeight()  { return fieldHeight; }

    public long getRemainingPauseMs() {
        if (phase != GamePhase.BETWEEN_WAVES) return 0;
        return Math.max(0, waveEndTime - System.currentTimeMillis());
    }
}