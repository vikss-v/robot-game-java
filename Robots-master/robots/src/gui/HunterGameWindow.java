package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import model.HunterSnakeModel;
import model.HunterModelListener;
import model.SnakeModel;

public class HunterGameWindow extends JInternalFrame implements HunterModelListener {

    private static final int TICK_MS = 150;

    private HunterSnakeModel model;
    private final Canvas canvas;
    private Timer timer;
    private JButton restartButton;
    private JButton closeButton;

    private final String playerName;
    private final Color playerColor;
    private final int fieldWidth;
    private final int fieldHeight;

    public HunterGameWindow(HunterSnakeModel model) {
        super("Охотник - Съешь змей и убеги!", true, true, true, true);
        this.model = model;
        this.playerName = model.getPlayerSnake().name;
        this.playerColor = model.getPlayerSnake().color;
        this.fieldWidth = model.getFieldWidth() * model.getCellSize();
        this.fieldHeight = model.getFieldHeight() * model.getCellSize();

        model.addListener(this);

        canvas = new Canvas();
        canvas.setFocusable(true);
        canvas.setFocusTraversalKeysEnabled(false);

        JPanel buttonPanel = createButtonPanel();

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setMaximizable(true);
        setResizable(true);

        bindKeys();
        startTimer();
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(50, 50, 60));

        restartButton = new JButton("Новая игра");
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.setBackground(new Color(70, 70, 90));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> restartGame());

        closeButton = new JButton("Закрыть");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(70, 70, 90));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> dispose());

        panel.add(restartButton);
        panel.add(closeButton);

        return panel;
    }

    private void restartGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        HunterSnakeModel newModel = new HunterSnakeModel(fieldWidth, fieldHeight, playerName, playerColor);

        model.removeListener(this);

        this.model = newModel;
        this.model.addListener(this);

        canvas.repaint();

        startTimer();

        canvas.requestFocusInWindow();
    }

    private void bindKeys() {
        InputMap im = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = canvas.getActionMap();

        bind(im, am, KeyEvent.VK_W, "w", () -> model.setPlayerDirection(SnakeModel.Direction.UP));
        bind(im, am, KeyEvent.VK_S, "s", () -> model.setPlayerDirection(SnakeModel.Direction.DOWN));
        bind(im, am, KeyEvent.VK_A, "a", () -> model.setPlayerDirection(SnakeModel.Direction.LEFT));
        bind(im, am, KeyEvent.VK_D, "d", () -> model.setPlayerDirection(SnakeModel.Direction.RIGHT));
        bind(im, am, KeyEvent.VK_UP, "up", () -> model.setPlayerDirection(SnakeModel.Direction.UP));
        bind(im, am, KeyEvent.VK_DOWN, "down", () -> model.setPlayerDirection(SnakeModel.Direction.DOWN));
        bind(im, am, KeyEvent.VK_LEFT, "left", () -> model.setPlayerDirection(SnakeModel.Direction.LEFT));
        bind(im, am, KeyEvent.VK_RIGHT, "right", () -> model.setPlayerDirection(SnakeModel.Direction.RIGHT));

        bind(im, am, KeyEvent.VK_R, "restart", () -> restartGame());
    }

    private void bind(InputMap im, ActionMap am, int keyCode, String name, Runnable action) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void startTimer() {
        timer = new Timer("hunter-tick", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                model.tick();
            }
        }, TICK_MS, TICK_MS);
    }

    @Override
    public void onModelChanged(HunterSnakeModel m) {
        SwingUtilities.invokeLater(() -> {
            canvas.repaint();

            if (m.getPhase() == HunterSnakeModel.GamePhase.GAME_OVER ||
                    m.getPhase() == HunterSnakeModel.GamePhase.VICTORY) {
                restartButton.setText("Играть снова");
                restartButton.setBackground(new Color(100, 150, 100));
            } else {
                restartButton.setText("Новая игра");
                restartButton.setBackground(new Color(70, 70, 90));
            }
        });
    }

    @Override
    public void dispose() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        model.removeListener(this);
        super.dispose();
    }

    private class Canvas extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int fw = model.getFieldWidth();
            int fh = model.getFieldHeight();
            if (fw == 0 || fh == 0) return;

            int cellW = getWidth() / fw;
            int cellH = getHeight() / fh;
            if (cellW == 0 || cellH == 0) return;

            if (model.getPhase() == HunterSnakeModel.GamePhase.ESCAPE) {
                g2.setColor(new Color(80, 30, 30));
            } else {
                g2.setColor(new Color(30, 30, 40));
            }
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(60, 60, 70));
            g2.setStroke(new java.awt.BasicStroke(0.5f));
            for (int x = 0; x <= fw; x++) {
                g2.drawLine(x * cellW, 0, x * cellW, getHeight());
            }
            for (int y = 0; y <= fh; y++) {
                g2.drawLine(0, y * cellH, getWidth(), y * cellH);
            }

            Point apple = model.getApple();
            if (apple != null) {
                g2.setColor(new Color(0xCC2200));
                int ax = apple.x * cellW + 2;
                int ay = apple.y * cellH + 2;
                int aw = cellW - 4;
                int ah = cellH - 4;
                g2.fillOval(ax, ay, aw, ah);
                g2.setColor(new Color(0xFF6644));
                g2.fillOval(ax + aw/3, ay + ah/3, aw/3, ah/3);
            }

            for (HunterSnakeModel.BotSnake bot : model.getBotSnakes()) {
                if (!bot.isAlive || !bot.snake.alive) continue;
                drawSnake(g2, bot.snake, cellW, cellH, true);
            }

            if (model.getPlayerSnake() != null && model.getPlayerSnake().alive) {
                drawSnake(g2, model.getPlayerSnake(), cellW, cellH, false);
            }

            drawUI(g2, cellW);

            if (model.getPhase() == HunterSnakeModel.GamePhase.GAME_OVER) {
                drawMessage(g2, "ИГРА ОКОНЧЕНА! Счёт: " + model.getScore(),
                        "Нажмите R или кнопку 'Новая игра'");
            } else if (model.getPhase() == HunterSnakeModel.GamePhase.VICTORY) {
                drawMessage(g2, "ПОБЕДА! Вы выжили! Счёт: " + model.getScore(),
                        "Нажмите R или кнопку 'Новая игра'");
            }
        }

        private void drawSnake(Graphics2D g2, SnakeModel.Snake snake, int cellW, int cellH, boolean isBot) {
            List<Point> body = snake.bodyList();
            if (body.isEmpty()) return;

            Color snakeColor = snake.alive ? snake.color : snake.color.darker();

            for (int i = body.size() - 1; i >= 0; i--) {
                Point p = body.get(i);

                if (i == 0) {
                    g2.setColor(snakeColor);
                    g2.fillRoundRect(p.x * cellW, p.y * cellH, cellW, cellH, 8, 8);

                    if (!isBot) {
                        int eyeSize = Math.max(2, Math.min(cellW, cellH) / 6);
                        g2.setColor(Color.WHITE);
                        g2.fillOval(p.x * cellW + cellW / 4 - eyeSize/2, p.y * cellH + cellH / 4, eyeSize, eyeSize);
                        g2.fillOval(p.x * cellW + 3 * cellW / 4 - eyeSize/2, p.y * cellH + cellH / 4, eyeSize, eyeSize);
                        g2.setColor(Color.BLACK);
                        g2.fillOval(p.x * cellW + cellW / 4 - eyeSize/4, p.y * cellH + cellH / 4 + eyeSize/4, eyeSize/2, eyeSize/2);
                        g2.fillOval(p.x * cellW + 3 * cellW / 4 - eyeSize/4, p.y * cellH + cellH / 4 + eyeSize/4, eyeSize/2, eyeSize/2);
                    }
                } else {
                    g2.setColor(snakeColor);
                    g2.fillRoundRect(p.x * cellW + 1, p.y * cellH + 1, cellW - 2, cellH - 2, 4, 4);
                }
            }
        }

        private void drawUI(Graphics2D g2, int cellW) {
            int topY = 25;
            int leftX = 10;
            int rightX = getWidth() - 200;

            g2.setFont(new Font("Arial", Font.BOLD, 16));

            g2.setColor(Color.WHITE);
            g2.drawString("Счёт: " + model.getScore(), leftX, topY);

            g2.drawString("Ботов: " + model.getAliveBotsCount(), leftX, topY + 25);

            if (model.getPhase() == HunterSnakeModel.GamePhase.HUNTING) {
                g2.setColor(new Color(100, 255, 100));
                long remaining = model.getRemainingHuntingTime();
                g2.drawString("ОХОТА: " + remaining + "с", rightX, topY);
                g2.drawString("Ешь ботов!", rightX, topY + 25);
            } else if (model.getPhase() == HunterSnakeModel.GamePhase.ESCAPE) {
                g2.setColor(new Color(255, 100, 100));
                long remaining = model.getRemainingEscapeTime();
                g2.drawString("ПОБЕГ: " + remaining + "с", rightX, topY);
                g2.drawString("Убегай!", rightX, topY + 25);
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(200, 200, 200));
            g2.drawString("WASD / Стрелки | R - Новая игра", leftX, getHeight() - 10);
        }

        private void drawMessage(Graphics2D g2, String message, String subMessage) {
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, getHeight() / 2 - 70, getWidth(), 140);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(message, (getWidth() - fm.stringWidth(message)) / 2, getHeight() / 2 - 10);

            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            fm = g2.getFontMetrics();
            g2.drawString(subMessage, (getWidth() - fm.stringWidth(subMessage)) / 2, getHeight() / 2 + 35);
        }
    }
}