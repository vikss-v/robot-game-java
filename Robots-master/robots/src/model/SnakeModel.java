package model;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SnakeModel {

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    public enum GameState { WAITING, RUNNING, FINISHED }

    public static class Snake {
        public final String name;
        public final Color color;
        public final Deque<Point> body = new ArrayDeque<>();
        public Direction direction;
        private Direction nextDirection;
        public boolean alive = true;

        public Snake(String name, Color color, Point start, Direction direction) {
            this.name = name;
            this.color = color;
            this.direction = direction;
            this.nextDirection = direction;
            for (int i = 0; i < 5; i++) {
                int bx = start.x, by = start.y;
                switch (direction) {
                    case RIGHT -> bx = start.x - i;
                    case LEFT  -> bx = start.x + i;
                    case DOWN  -> by = start.y - i;
                    case UP    -> by = start.y + i;
                }
                body.addLast(new Point(bx, by));
            }
        }

        public Point head() {
            return body.peekFirst();
        }

        public List<Point> bodyList() {
            return new ArrayList<>(body);
        }
    }

    private int fieldWidth;
    private int fieldHeight;
    private static final int CELL = 20;

    public void setFieldSize(int widthPx, int heightPx) {
        this.fieldWidth  = Math.max(1, widthPx  / CELL);
        this.fieldHeight = Math.max(1, heightPx / CELL);
    }

    private final Snake snake1;
    private final Snake snake2;

    private Point apple;
    private final Random random = new Random();

    private GameState state = GameState.WAITING;
    private String winner = null;

    private final List<SnakeModelListener> listeners = new CopyOnWriteArrayList<>();

    public SnakeModel(int fieldWidth, int fieldHeight,
                      String name1, Color color1,
                      String name2, Color color2) {
        this.fieldWidth = fieldWidth / CELL;
        this.fieldHeight = fieldHeight / CELL;

        snake1 = new Snake(name1, color1,
                new Point(this.fieldWidth / 4, this.fieldHeight / 2),
                Direction.RIGHT);
        snake2 = new Snake(name2, color2,
                new Point(3 * this.fieldWidth / 4, this.fieldHeight / 2),
                Direction.LEFT);

        spawnApple();
    }

    public static int getCellSize() { return CELL; }

    public void addListener(SnakeModelListener l) { listeners.add(l); }
    public void removeListener(SnakeModelListener l) { listeners.remove(l); }

    private void notify_() {
        for (SnakeModelListener l : listeners) l.onSnakeModelChanged(this);
    }

    public Snake getSnake1() { return snake1; }
    public Snake getSnake2() { return snake2; }
    public Point getApple() { return apple; }
    public GameState getState() { return state; }
    public String getWinner() { return winner; }
    public int getFieldWidth() { return fieldWidth; }
    public int getFieldHeight() { return fieldHeight; }

    public void startGame() {
        if (state == GameState.WAITING) {
            state = GameState.RUNNING;
            notify_();
        }
    }

    public void setDirection1(Direction d) {
        if (!opposite(d, snake1.direction)) snake1.nextDirection = d;
    }

    public void setDirection2(Direction d) {
        if (!opposite(d, snake2.direction)) snake2.nextDirection = d;
    }

    private boolean opposite(Direction a, Direction b) {
        return (a == Direction.UP && b == Direction.DOWN)
                || (a == Direction.DOWN && b == Direction.UP)
                || (a == Direction.LEFT && b == Direction.RIGHT)
                || (a == Direction.RIGHT && b == Direction.LEFT);
    }

    public void tick() {
        if (state != GameState.RUNNING) return;

        moveSnake(snake1);
        moveSnake(snake2);

        checkCollisions();

        if (state == GameState.RUNNING) {
            checkApple(snake1);
            checkApple(snake2);
        }

        notify_();
    }

    private void moveSnake(Snake snake) {
        if (!snake.alive) return;
        snake.direction = snake.nextDirection;
        Point head = snake.head();
        Point next = step(head, snake.direction);
        snake.body.addFirst(next);
        snake.body.removeLast();
    }

    private Point step(Point p, Direction d) {
        return switch (d) {
            case UP    -> new Point(p.x, p.y - 1);
            case DOWN  -> new Point(p.x, p.y + 1);
            case LEFT  -> new Point(p.x - 1, p.y);
            case RIGHT -> new Point(p.x + 1, p.y);
        };
    }

    private void checkCollisions() {
        checkWallAndSelf(snake1, snake2);
        checkWallAndSelf(snake2, snake1);

        if (!snake1.alive && !snake2.alive) {
            state = GameState.FINISHED;
            winner = "Ничья!";
        } else if (!snake1.alive) {
            state = GameState.FINISHED;
            winner = snake2.name;
        } else if (!snake2.alive) {
            state = GameState.FINISHED;
            winner = snake1.name;
        }
    }

    private void checkWallAndSelf(Snake snake, Snake other) {
        if (!snake.alive) return;
        Point head = snake.head();

        if (head.x < 0 || head.x >= fieldWidth || head.y < 0 || head.y >= fieldHeight) {
            snake.alive = false;
            return;
        }

        List<Point> body = snake.bodyList();
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) { snake.alive = false; return; }
        }

        for (Point p : other.bodyList()) {
            if (head.equals(p)) { snake.alive = false; return; }
        }
    }

    private void checkApple(Snake snake) {
        if (!snake.alive || apple == null) return;
        if (snake.head().equals(apple)) {
            snake.body.addLast(snake.body.peekLast());
            spawnApple();
        }
    }

    private void spawnApple() {
        Point candidate;
        do {
            candidate = new Point(random.nextInt(fieldWidth), random.nextInt(fieldHeight));
        } while (isOccupied(candidate));
        apple = candidate;
    }

    private boolean isOccupied(Point p) {
        for (Point b : snake1.bodyList()) if (b.equals(p)) return true;
        for (Point b : snake2.bodyList()) if (b.equals(p)) return true;
        return false;
    }
}