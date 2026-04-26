package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

import model.SnakeModel;
import model.SnakeModelListener;

public class SnakeGameWindow extends JInternalFrame implements SnakeModelListener {

    private static final int TICK_MS  = 350;

    private final SnakeModel model;
    private final Color bgColor;
    private final Canvas canvas;
    private final JButton startButton;
    private Timer timer;

    public SnakeGameWindow(SnakeModel model, Color bgColor) {
        super("Игровой режим", true, true, true, true);
        this.model = model;
        this.bgColor = bgColor;
        model.addListener(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
                stopTimer();
                setVisible(false);
            }
        });

        canvas = new Canvas();
        canvas.setFocusable(true);
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                model.setFieldSize(canvas.getWidth(), canvas.getHeight());
            }
        });

        startButton = new JButton("Старт");
        startButton.setFont(new Font("Dialog", Font.BOLD, 14));
        startButton.addActionListener(e -> startGame());

        JPanel root = new JPanel(new java.awt.BorderLayout(0, 6));
        root.add(canvas, java.awt.BorderLayout.CENTER);
        root.add(startButton, java.awt.BorderLayout.SOUTH);

        getContentPane().add(root);
        setSize(616, 590);

        bindKeys();
    }

    private void bindKeys() {
        InputMap im = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = canvas.getActionMap();

        bind(im, am, KeyEvent.VK_W, "w", () -> model.setDirection1(SnakeModel.Direction.UP));
        bind(im, am, KeyEvent.VK_S, "s", () -> model.setDirection1(SnakeModel.Direction.DOWN));
        bind(im, am, KeyEvent.VK_A, "a", () -> model.setDirection1(SnakeModel.Direction.LEFT));
        bind(im, am, KeyEvent.VK_D, "d", () -> model.setDirection1(SnakeModel.Direction.RIGHT));

        bind(im, am, KeyEvent.VK_UP,    "up",    () -> model.setDirection2(SnakeModel.Direction.UP));
        bind(im, am, KeyEvent.VK_DOWN,  "down",  () -> model.setDirection2(SnakeModel.Direction.DOWN));
        bind(im, am, KeyEvent.VK_LEFT,  "left",  () -> model.setDirection2(SnakeModel.Direction.LEFT));
        bind(im, am, KeyEvent.VK_RIGHT, "right", () -> model.setDirection2(SnakeModel.Direction.RIGHT));
    }

    private void bind(InputMap im, ActionMap am, int keyCode, String name, Runnable action) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private void startGame() {
        model.startGame();
        startButton.setEnabled(false);
        startButton.setText("Игра идёт...");

        timer = new Timer("snake-tick", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                model.tick();
            }
        }, TICK_MS, TICK_MS);
    }

    @Override
    public void onSnakeModelChanged(SnakeModel m) {
        SwingUtilities.invokeLater(() -> {
            canvas.repaint();
            if (m.getState() == SnakeModel.GameState.FINISHED) {
                stopTimer();
            }
        });
    }

    private void stopTimer() {
        if (timer != null) { timer.cancel(); timer = null; }
    }

    @Override
    public void dispose() {
        stopTimer();
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
            int cellW = getWidth()  / fw;
            int cellH = getHeight() / fh;
            if (cellW == 0 || cellH == 0) return;

            g2.setColor(bgColor);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(0, 0, 0, 20));
            for (int x = 0; x < fw; x++) {
                for (int y = 0; y < fh; y++) {
                    g2.drawRect(x * cellW, y * cellH, cellW, cellH);
                }
            }

            Point apple = model.getApple();
            if (apple != null) {
                int ax = apple.x * cellW + 2, ay = apple.y * cellH + 2;
                int aw = cellW - 4, ah = cellH - 4;
                g2.setColor(new Color(0xCC2200));
                g2.fillOval(ax, ay, aw, ah);
                g2.setColor(new Color(0xFF4422));
                g2.fillOval(ax + aw / 5, ay + ah / 6, aw / 3, ah / 3);
                g2.setColor(new Color(0x226600));
                g2.setStroke(new BasicStroke(Math.max(1f, cellW / 8f)));
                g2.drawLine(ax + aw / 2, ay, ax + aw / 2 + aw / 4, ay - ah / 3);
            }

            drawSnake(g2, model.getSnake1(), cellW, cellH);
            drawSnake(g2, model.getSnake2(), cellW, cellH);

            if (model.getState() == SnakeModel.GameState.WAITING) {
                drawOverlay(g2, "Нажмите «Старт» для начала игры", null);
            } else if (model.getState() == SnakeModel.GameState.FINISHED) {
                drawOverlay(g2, "Победил: " + model.getWinner(), "Закройте окно для выхода");
            }
        }

        private void drawSnake(Graphics2D g2, SnakeModel.Snake snake, int cellW, int cellH) {
            List<Point> body = snake.bodyList();
            if (body.isEmpty()) return;

            Color snakeColor;
            if (snake.alive) {
                snakeColor = snake.color;
            } else {
                snakeColor = snake.color.darker().darker();
            }

            for (int i = body.size() - 1; i >= 0; i--) {
                Point p = body.get(i);
                g2.setColor(snakeColor);

                if (i == 0) {
                    g2.fillRoundRect(p.x * cellW, p.y * cellH, cellW, cellH, 8, 8);

                    int eyeSize = Math.max(2, Math.min(cellW, cellH) / 4);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(p.x * cellW + cellW / 2, p.y * cellH + cellH / 4, eyeSize, eyeSize);
                } else {
                    g2.fillRoundRect(p.x * cellW + 1, p.y * cellH + 1, cellW - 2, cellH - 2, 6, 6);
                }
            }

            Point head = body.get(0);
            g2.setColor(snake.color);
            g2.setFont(new Font("Dialog", Font.BOLD, Math.max(8, Math.min(cellW, cellH) / 2)));
            g2.drawString(snake.name, head.x * cellW, Math.max(10, head.y * cellH - 2));
        }

        private void drawOverlay(Graphics2D g2, String line1, String line2) {
            g2.setColor(new Color(0, 0, 0, 140));
            int rectHeight;
            if (line2 != null) {
                rectHeight = 90;
            } else {
                rectHeight = 55;
            }
            g2.fillRect(0, getHeight() / 2 - 45, getWidth(), rectHeight);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Dialog", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(line1, (getWidth() - fm.stringWidth(line1)) / 2, getHeight() / 2);

            if (line2 != null) {
                g2.setFont(new Font("Dialog", Font.PLAIN, 13));
                fm = g2.getFontMetrics();
                g2.drawString(line2, (getWidth() - fm.stringWidth(line2)) / 2, getHeight() / 2 + 28);
            }
        }
    }
}