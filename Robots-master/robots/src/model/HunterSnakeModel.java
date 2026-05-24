package model;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import log.Logger;

public class HunterSnakeModel {

    public enum GamePhase {
        HUNTING,
        ESCAPE,
        GAME_OVER,
        VICTORY
    }

    public static class BotSnake {
        public SnakeModel.Snake snake;
        public boolean isAlive = true;

        public BotSnake(String name, Color color, Point start, SnakeModel.Direction dir) {
            Logger.logFunction("BotSnake constructor");
            this.snake = new SnakeModel.Snake(name, color, start, dir);
            while (this.snake.body.size() > 3) {
                this.snake.body.removeLast();
            }
        }
    }

    private static final int CELL_SIZE = 40;
    private static final int HUNTING_DURATION_MS = 30000;
    private static final int ESCAPE_DURATION_MS = 20000;

    private static final int PLAYER_TICK_MS = 145;
    private static final int BOT_TICK_MS = 150;

    private int fieldWidth;
    private int fieldHeight;

    private SnakeModel.Snake playerSnake;
    private final List<BotSnake> botSnakes = new ArrayList<>();

    private int score = 0;
    private int botsEaten = 0;
    private GamePhase phase = GamePhase.HUNTING;
    private long phaseStartTime;
    private long lastPlayerMove = 0;
    private long lastBotMove = 0;

    private Point apple;
    private final Random random = new Random();

    private final List<HunterModelListener> listeners = new CopyOnWriteArrayList<>();

    public HunterSnakeModel(int fieldWidthPx, int fieldHeightPx, String playerName, Color playerColor) {
        Logger.logFunction("HunterSnakeModel constructor");
        this.fieldWidth = Math.max(15, fieldWidthPx / CELL_SIZE);
        this.fieldHeight = Math.max(15, fieldHeightPx / CELL_SIZE);

        playerSnake = new SnakeModel.Snake(
                playerName, playerColor,
                new Point(this.fieldWidth / 2, this.fieldHeight / 2),
                SnakeModel.Direction.RIGHT
        );

        while (playerSnake.body.size() > 3) {
            playerSnake.body.removeLast();
        }

        createBotSnakes();
        spawnApple();
        phaseStartTime = System.currentTimeMillis();
        lastPlayerMove = System.currentTimeMillis();
        lastBotMove = System.currentTimeMillis();
    }

    private void createBotSnakes() {
        Logger.logFunction("HunterSnakeModel.createBotSnakes");
        String[] botNames = {
                "Красный", "Зелёный", "Синий", "Жёлтый",
                "Фиолетовый", "Оранжевый", "Розовый", "Голубой"
        };

        Color[] botColors = {
                new Color(0xFF6666), new Color(0x66FF66), new Color(0x6666FF), new Color(0xFFDD66),
                new Color(0xFF66FF), new Color(0xFF8844), new Color(0xFF99CC), new Color(0x44CCFF)
        };

        List<Point> usedPositions = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            Point startPos;
            int attempts = 0;
            boolean found = false;

            do {
                int zone = i % 4;
                int x, y;

                switch (zone) {
                    case 0:
                        x = 5 + random.nextInt(fieldWidth / 4);
                        y = 5 + random.nextInt(fieldHeight / 4);
                        break;
                    case 1:
                        x = fieldWidth - 5 - random.nextInt(fieldWidth / 4);
                        y = 5 + random.nextInt(fieldHeight / 4);
                        break;
                    case 2:
                        x = 5 + random.nextInt(fieldWidth / 4);
                        y = fieldHeight - 5 - random.nextInt(fieldHeight / 4);
                        break;
                    default:
                        x = fieldWidth - 5 - random.nextInt(fieldWidth / 4);
                        y = fieldHeight - 5 - random.nextInt(fieldHeight / 4);
                        break;
                }

                startPos = new Point(x, y);
                found = true;

                for (Point used : usedPositions) {
                    if (Math.hypot(startPos.x - used.x, startPos.y - used.y) < 8) {
                        found = false;
                        break;
                    }
                }

                attempts++;
                if (attempts > 50) break;

            } while (!found);

            usedPositions.add(startPos);

            SnakeModel.Direction dir;
            if (startPos.x < fieldWidth / 2) {
                dir = SnakeModel.Direction.RIGHT;
            } else {
                dir = SnakeModel.Direction.LEFT;
            }

            BotSnake bot = new BotSnake(botNames[i], botColors[i], startPos, dir);
            botSnakes.add(bot);
        }
    }

    public void addListener(HunterModelListener listener) {
        listeners.add(listener);
        Logger.logFunction("HunterSnakeModel.addListener");
    }

    public void removeListener(HunterModelListener listener) {
        listeners.remove(listener);
        Logger.logFunction("HunterSnakeModel.removeListener");
    }

    private void notifyListeners() {
        Logger.logFunction("HunterSnakeModel.notifyListeners");
        for (HunterModelListener l : listeners) {
            l.onModelChanged(this);
        }
    }

    public void setPlayerDirection(SnakeModel.Direction dir) {
        Logger.logFunction("HunterSnakeModel.setPlayerDirection");
        if (playerSnake != null && playerSnake.alive && !opposite(dir, playerSnake.direction)) {
            playerSnake.nextDirection = dir;
        }
    }

    private boolean opposite(SnakeModel.Direction a, SnakeModel.Direction b) {
        return (a == SnakeModel.Direction.UP && b == SnakeModel.Direction.DOWN) ||
                (a == SnakeModel.Direction.DOWN && b == SnakeModel.Direction.UP) ||
                (a == SnakeModel.Direction.LEFT && b == SnakeModel.Direction.RIGHT) ||
                (a == SnakeModel.Direction.RIGHT && b == SnakeModel.Direction.LEFT);
    }

    public void tick() {
        Logger.logFunction("HunterSnakeModel.tick");
        long currentTime = System.currentTimeMillis();

        if (phase == GamePhase.GAME_OVER || phase == GamePhase.VICTORY) {
            return;
        }

        updatePhase();

        if (currentTime - lastPlayerMove >= PLAYER_TICK_MS) {
            movePlayer();
            lastPlayerMove = currentTime;

            if (playerSnake == null || !playerSnake.alive) {
                phase = GamePhase.GAME_OVER;
                notifyListeners();
                return;
            }

            checkApple();
        }

        if (currentTime - lastBotMove >= BOT_TICK_MS) {
            for (BotSnake bot : botSnakes) {
                if (bot.isAlive && bot.snake.alive) {
                    moveBot(bot);
                }
            }
            lastBotMove = currentTime;

            checkCollisions();
        }

        if (phase == GamePhase.ESCAPE && isEscapePhaseComplete()) {
            phase = GamePhase.VICTORY;
        }

        notifyListeners();
    }

    private void movePlayer() {
        Logger.logFunction("HunterSnakeModel.movePlayer");
        if (!playerSnake.alive) return;

        playerSnake.direction = playerSnake.nextDirection;
        Point head = playerSnake.head();
        Point next = step(head, playerSnake.direction);
        playerSnake.body.addFirst(next);
        playerSnake.body.removeLast();

        if (next.x < 0 || next.x >= fieldWidth || next.y < 0 || next.y >= fieldHeight) {
            playerSnake.alive = false;
            return;
        }

        Point newHead = playerSnake.head();
        List<Point> body = playerSnake.bodyList();
        for (int i = 1; i < body.size(); i++) {
            if (newHead.equals(body.get(i))) {
                playerSnake.alive = false;
                return;
            }
        }
    }

    private void moveBot(BotSnake bot) {
        Logger.logFunction("HunterSnakeModel.moveBot");
        SnakeModel.Snake s = bot.snake;
        if (!s.alive) return;

        SnakeModel.Direction bestDir = getBestDirection(bot);

        if (bestDir != null && !opposite(bestDir, s.direction)) {
            s.nextDirection = bestDir;
        }

        s.direction = s.nextDirection;
        Point head = s.head();
        Point next = step(head, s.direction);
        s.body.addFirst(next);
        s.body.removeLast();

        if (next.x < 0 || next.x >= fieldWidth || next.y < 0 || next.y >= fieldHeight) {
            s.alive = false;
            bot.isAlive = false;
            return;
        }

        Point newHead = s.head();
        List<Point> body = s.bodyList();
        for (int i = 1; i < body.size(); i++) {
            if (newHead.equals(body.get(i))) {
                s.alive = false;
                bot.isAlive = false;
                return;
            }
        }
    }

    private SnakeModel.Direction getBestDirection(BotSnake bot) {
        Logger.logFunction("HunterSnakeModel.getBestDirection");
        SnakeModel.Snake s = bot.snake;
        Point head = s.head();

        SnakeModel.Direction[] dirs = {
                SnakeModel.Direction.UP, SnakeModel.Direction.DOWN,
                SnakeModel.Direction.LEFT, SnakeModel.Direction.RIGHT
        };

        double bestScore = Double.NEGATIVE_INFINITY;
        SnakeModel.Direction bestDir = s.direction;

        for (SnakeModel.Direction dir : dirs) {
            if (opposite(dir, s.direction)) continue;

            Point next = step(head, dir);
            double score = 0;

            if (next.x <= 0 || next.x >= fieldWidth - 1 || next.y <= 0 || next.y >= fieldHeight - 1) {
                score -= 10000;
                continue;
            }

            boolean selfCollision = false;
            for (Point segment : s.bodyList()) {
                if (next.equals(segment)) {
                    selfCollision = true;
                    break;
                }
            }
            if (selfCollision) {
                score -= 8000;
            }

            for (BotSnake other : botSnakes) {
                if (other == bot || !other.isAlive || !other.snake.alive) continue;

                for (Point segment : other.snake.bodyList()) {
                    if (next.equals(segment)) {
                        score -= 10000;
                        break;
                    }
                }

                if (next.equals(other.snake.head())) {
                    score -= 8000;
                }

                Point otherHead = other.snake.head();
                for (SnakeModel.Direction otherDir : SnakeModel.Direction.values()) {
                    if (opposite(otherDir, other.snake.direction)) continue;

                    Point otherNext = step(otherHead, otherDir);

                    if (next.equals(otherNext)) {
                        score -= 5000;
                    }

                    if (otherNext.equals(head) && !phase.equals(GamePhase.ESCAPE)) {
                        score -= 3000;
                    }
                }

                double distToOther = Math.hypot(next.x - otherHead.x, next.y - otherHead.y);
                if (distToOther < 3) {
                    score -= (2000 * (4 - (int)distToOther));
                }
            }

            Point playerHead = playerSnake.head();
            double dx = next.x - playerHead.x;
            double dy = next.y - playerHead.y;
            double distanceToPlayer = Math.hypot(dx, dy);

            if (phase == GamePhase.HUNTING) {
                score += (4000 - distanceToPlayer * 4);

                Point botHead = s.head();
                double awayDx = botHead.x - playerHead.x;
                double awayDy = botHead.y - playerHead.y;
                double awayLen = Math.hypot(awayDx, awayDy);
                if (awayLen > 0.1) {
                    awayDx = awayDx / awayLen;
                    awayDy = awayDy / awayLen;
                }

                double moveDx = next.x - botHead.x;
                double moveDy = next.y - botHead.y;
                double moveLen = Math.hypot(moveDx, moveDy);
                if (moveLen > 0.1) {
                    moveDx = moveDx / moveLen;
                    moveDy = moveDy / moveLen;
                }

                double awayAlignment = (moveDx * awayDx + moveDy * awayDy);
                score += awayAlignment * 2500;

            } else {
                score += (4000 - distanceToPlayer * 3);

                double toPlayerDx = playerHead.x - next.x;
                double toPlayerDy = playerHead.y - next.y;
                double toPlayerLen = Math.hypot(toPlayerDx, toPlayerDy);
                if (toPlayerLen > 0.1) {
                    toPlayerDx = toPlayerDx / toPlayerLen;
                    toPlayerDy = toPlayerDy / toPlayerLen;
                }

                double moveDx = next.x - head.x;
                double moveDy = next.y - head.y;
                double moveLen = Math.hypot(moveDx, moveDy);
                if (moveLen > 0.1) {
                    moveDx = moveDx / moveLen;
                    moveDy = moveDy / moveLen;
                }

                double pursuitAlignment = (moveDx * toPlayerDx + moveDy * toPlayerDy);
                score += pursuitAlignment * 3500;

                if (next.equals(playerHead)) {
                    score += 10000;
                }
            }

            boolean isCorner = (next.x <= 2 && next.y <= 2) ||
                    (next.x <= 2 && next.y >= fieldHeight - 3) ||
                    (next.x >= fieldWidth - 3 && next.y <= 2) ||
                    (next.x >= fieldWidth - 3 && next.y >= fieldHeight - 3);
            if (isCorner) {
                score -= 2000;
            }

            double centerX = fieldWidth / 2.0;
            double centerY = fieldHeight / 2.0;
            double distToCenter = Math.hypot(next.x - centerX, next.y - centerY);
            score += (300 - distToCenter * 2);

            score += random.nextDouble() * 150;

            if (score > bestScore) {
                bestScore = score;
                bestDir = dir;
            }
        }

        return bestDir;
    }

    private void checkCollisions() {
        Logger.logFunction("HunterSnakeModel.checkCollisions");
        if (playerSnake == null || !playerSnake.alive) return;

        Point playerHead = playerSnake.head();

        Iterator<BotSnake> botIterator = botSnakes.iterator();
        while (botIterator.hasNext()) {
            BotSnake bot = botIterator.next();
            if (!bot.isAlive || !bot.snake.alive) continue;

            boolean collision = false;
            for (Point botSegment : bot.snake.bodyList()) {
                if (playerHead.equals(botSegment)) {
                    collision = true;
                    break;
                }
            }

            if (collision) {
                if (phase == GamePhase.HUNTING) {
                    eatBot(bot);
                } else {
                    playerSnake.alive = false;
                    return;
                }
            }
        }

        List<BotSnake> aliveBots = new ArrayList<>();
        for (BotSnake bot : botSnakes) {
            if (bot.isAlive && bot.snake.alive) {
                aliveBots.add(bot);
            }
        }

        for (int i = 0; i < aliveBots.size(); i++) {
            BotSnake bot1 = aliveBots.get(i);
            Point bot1Head = bot1.snake.head();

            for (int j = 0; j < aliveBots.size(); j++) {
                if (i == j) continue;
                BotSnake bot2 = aliveBots.get(j);

                for (Point segment : bot2.snake.bodyList()) {
                    if (bot1Head.equals(segment)) {
                        bot1.snake.alive = false;
                        bot1.isAlive = false;
                        break;
                    }
                }
            }
        }
    }

    private void eatBot(BotSnake bot) {
        Logger.logFunction("HunterSnakeModel.eatBot");
        score += 100;
        botsEaten++;

        for (int i = 0; i < 2; i++) {
            Point last = playerSnake.body.getLast();
            playerSnake.body.addLast(new Point(last.x, last.y));
        }

        bot.snake.alive = false;
        bot.isAlive = false;

        notifyListeners();
    }

    private Point step(Point p, SnakeModel.Direction d) {
        return switch (d) {
            case UP -> new Point(p.x, p.y - 1);
            case DOWN -> new Point(p.x, p.y + 1);
            case LEFT -> new Point(p.x - 1, p.y);
            case RIGHT -> new Point(p.x + 1, p.y);
        };
    }

    private void updatePhase() {
        Logger.logFunction("HunterSnakeModel.updatePhase");
        long elapsed = System.currentTimeMillis() - phaseStartTime;

        if (phase == GamePhase.HUNTING && elapsed >= HUNTING_DURATION_MS) {
            phase = GamePhase.ESCAPE;
            phaseStartTime = System.currentTimeMillis();
            notifyListeners();
        }
    }

    private void checkApple() {
        Logger.logFunction("HunterSnakeModel.checkApple");
        if (!playerSnake.alive || apple == null) return;
        if (playerSnake.head().equals(apple)) {
            Point last = playerSnake.body.getLast();
            playerSnake.body.addLast(new Point(last.x, last.y));
            score += 10;
            spawnApple();
        }
    }

    private void spawnApple() {
        Logger.logFunction("HunterSnakeModel.spawnApple");
        Point candidate;
        int attempts = 0;
        do {
            candidate = new Point(random.nextInt(fieldWidth), random.nextInt(fieldHeight));
            attempts++;
            if (attempts > 100) break;
        } while (isPositionOccupied(candidate));
        apple = candidate;
    }

    private boolean isPositionOccupied(Point p) {
        if (playerSnake != null && playerSnake.alive) {
            for (Point seg : playerSnake.bodyList()) {
                if (seg.equals(p)) return true;
            }
        }

        for (BotSnake bot : botSnakes) {
            if (bot.isAlive && bot.snake.alive) {
                for (Point seg : bot.snake.bodyList()) {
                    if (seg.equals(p)) return true;
                }
            }
        }
        return false;
    }

    private boolean isEscapePhaseComplete() {
        long elapsed = System.currentTimeMillis() - phaseStartTime;
        return elapsed >= ESCAPE_DURATION_MS;
    }

    public SnakeModel.Snake getPlayerSnake() { Logger.logFunction("HunterSnakeModel.getPlayerSnake"); return playerSnake; }
    public List<BotSnake> getBotSnakes() { Logger.logFunction("HunterSnakeModel.getBotSnakes"); return new ArrayList<>(botSnakes); }
    public int getScore() { Logger.logFunction("HunterSnakeModel.getScore"); return score; }
    public int getBotsEaten() { Logger.logFunction("HunterSnakeModel.getBotsEaten"); return botsEaten; }
    public int getAliveBotsCount() {
        Logger.logFunction("HunterSnakeModel.getAliveBotsCount");
        int count = 0;
        for (BotSnake bot : botSnakes) {
            if (bot.isAlive && bot.snake.alive) count++;
        }
        return count;
    }
    public GamePhase getPhase() { Logger.logFunction("HunterSnakeModel.getPhase"); return phase; }
    public Point getApple() { Logger.logFunction("HunterSnakeModel.getApple"); return apple; }
    public int getFieldWidth() { Logger.logFunction("HunterSnakeModel.getFieldWidth"); return fieldWidth; }
    public int getFieldHeight() { Logger.logFunction("HunterSnakeModel.getFieldHeight"); return fieldHeight; }
    public static int getCellSize() { Logger.logFunction("HunterSnakeModel.getCellSize"); return CELL_SIZE; }

    public long getRemainingHuntingTime() {
        Logger.logFunction("HunterSnakeModel.getRemainingHuntingTime");
        if (phase != GamePhase.HUNTING) return 0;
        long elapsed = System.currentTimeMillis() - phaseStartTime;
        return Math.max(0, (HUNTING_DURATION_MS - elapsed) / 1000);
    }

    public long getRemainingEscapeTime() {
        Logger.logFunction("HunterSnakeModel.getRemainingEscapeTime");
        if (phase != GamePhase.ESCAPE) return 0;
        long elapsed = System.currentTimeMillis() - phaseStartTime;
        return Math.max(0, (ESCAPE_DURATION_MS - elapsed) / 1000);
    }
}