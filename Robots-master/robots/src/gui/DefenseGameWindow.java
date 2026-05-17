package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
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

import model.DefenseModel;
import model.DefenseModelListener;

public class DefenseGameWindow extends JInternalFrame implements DefenseModelListener {

    private static final int TICK_MS = 30;

    private DefenseModel model;
    private final Canvas canvas;
    private Timer timer;
    private JButton startButton;
    private JButton restartButton;

    private final Set<Integer> pressedKeys = new HashSet<>();

    public DefenseGameWindow(DefenseModel model) {
        super("Защита яблока — кооперативная игра", true, true, true, true);
        this.model = model;
        model.addListener(this);

        canvas = new Canvas();
        canvas.setFocusable(true);

        JPanel buttonPanel = createButtonPanel();

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(900, 650);
        setResizable(true);
        setMaximizable(true);

        bindKeys();
        startTimer();
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 20, 30));

        startButton = new JButton("Начать игру");
        styleButton(startButton);
        startButton.addActionListener(e -> {
            model.startGame();
            startButton.setEnabled(false);
            canvas.requestFocusInWindow();
        });

        restartButton = new JButton("Новая игра");
        styleButton(restartButton);
        restartButton.addActionListener(e -> restartGame());

        JButton closeButton = new JButton("Закрыть");
        styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        panel.add(startButton);
        panel.add(restartButton);
        panel.add(closeButton);
        return panel;
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBackground(new Color(50, 55, 75));
        b.setForeground(Color.WHITE);
        b.setFocusable(false);
    }

    private void restartGame() {
        stopTimer();
        model.removeListener(this);

        model = new DefenseModel(model.getFieldWidth(), model.getFieldHeight());
        model.addListener(this);

        startButton.setEnabled(true);
        pressedKeys.clear();
        canvas.repaint();
        startTimer();
        canvas.requestFocusInWindow();
    }

    private void startTimer() {
        timer = new Timer("defense-tick", true);
        long[] lastTick = {System.currentTimeMillis()};

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                double dt = (now - lastTick[0]) / 1000.0;
                lastTick[0] = now;
                updateDirectionsFromKeys();
                model.tick(dt);
            }
        }, TICK_MS, TICK_MS);
    }

    private void stopTimer() {
        if (timer != null) { timer.cancel(); timer = null; }
    }

    private void updateDirectionsFromKeys() {
        double vx1 = 0, vy1 = 0;
        if (pressedKeys.contains(KeyEvent.VK_W)) vy1 -= 1;
        if (pressedKeys.contains(KeyEvent.VK_S)) vy1 += 1;
        if (pressedKeys.contains(KeyEvent.VK_A)) vx1 -= 1;
        if (pressedKeys.contains(KeyEvent.VK_D)) vx1 += 1;
        double len1 = Math.hypot(vx1, vy1);
        if (len1 > 0) { vx1 /= len1; vy1 /= len1; }
        model.setRobot1Direction(vx1, vy1);

        double vx2 = 0, vy2 = 0;
        if (pressedKeys.contains(KeyEvent.VK_UP))    vy2 -= 1;
        if (pressedKeys.contains(KeyEvent.VK_DOWN))  vy2 += 1;
        if (pressedKeys.contains(KeyEvent.VK_LEFT))  vx2 -= 1;
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) vx2 += 1;
        double len2 = Math.hypot(vx2, vy2);
        if (len2 > 0) { vx2 /= len2; vy2 /= len2; }
        model.setRobot2Direction(vx2, vy2);
    }

    private void bindKeys() {
        InputMap im = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = canvas.getActionMap();

        bindPress(im, am, KeyEvent.VK_W,     "w_press");
        bindPress(im, am, KeyEvent.VK_S,     "s_press");
        bindPress(im, am, KeyEvent.VK_A,     "a_press");
        bindPress(im, am, KeyEvent.VK_D,     "d_press");
        bindPress(im, am, KeyEvent.VK_UP,    "up_press");
        bindPress(im, am, KeyEvent.VK_DOWN,  "down_press");
        bindPress(im, am, KeyEvent.VK_LEFT,  "left_press");
        bindPress(im, am, KeyEvent.VK_RIGHT, "right_press");

        bindRelease(im, am, KeyEvent.VK_W,     "w_rel");
        bindRelease(im, am, KeyEvent.VK_S,     "s_rel");
        bindRelease(im, am, KeyEvent.VK_A,     "a_rel");
        bindRelease(im, am, KeyEvent.VK_D,     "d_rel");
        bindRelease(im, am, KeyEvent.VK_UP,    "up_rel");
        bindRelease(im, am, KeyEvent.VK_DOWN,  "down_rel");
        bindRelease(im, am, KeyEvent.VK_LEFT,  "left_rel");
        bindRelease(im, am, KeyEvent.VK_RIGHT, "right_rel");
    }

    private void bindPress(InputMap im, ActionMap am, int key, String name) {
        im.put(KeyStroke.getKeyStroke(key, 0, false), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { pressedKeys.add(key); }
        });
    }

    private void bindRelease(InputMap im, ActionMap am, int key, String name) {
        im.put(KeyStroke.getKeyStroke(key, 0, true), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { pressedKeys.remove(key); }
        });
    }

    @Override
    public void onModelChanged(DefenseModel m) {
        SwingUtilities.invokeLater(() -> {
            canvas.repaint();
            DefenseModel.GamePhase phase = m.getPhase();
            if (phase == DefenseModel.GamePhase.VICTORY || phase == DefenseModel.GamePhase.DEFEAT) {
                restartButton.setBackground(new Color(70, 110, 70));
            }
        });
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

            drawBackground(g2);
            drawApple(g2);
            drawWorms(g2);
            drawRobot(g2, model.getRobot1(), "Игрок 1\n(WASD)");
            drawRobot(g2, model.getRobot2(), "Игрок 2\n(Стрелки)");
            drawHUD(g2);
            drawOverlay(g2);
        }

        private void drawBackground(Graphics2D g2) {
            g2.setColor(new Color(15, 17, 26));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(30, 34, 50));
            g2.setStroke(new BasicStroke(0.5f));
            int step = 40;
            for (int x = 0; x < getWidth(); x += step)
                g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += step)
                g2.drawLine(0, y, getWidth(), y);
        }

        private void drawApple(Graphics2D g2) {
            int ax = model.getApple().x;
            int ay = model.getApple().y;
            int r  = (int) DefenseModel.APPLE_RADIUS;

            float hp = (float) model.getAppleHp() / DefenseModel.APPLE_MAX_HP;
            g2.setColor(new Color((int)(255 * (1 - hp)), (int)(200 * hp), 0, 40));
            g2.fillOval(ax - r - 12, ay - r - 12, (r + 12) * 2, (r + 12) * 2);

            g2.setColor(new Color(0xCC2200));
            g2.fillOval(ax - r, ay - r, r * 2, r * 2);
            g2.setColor(new Color(0xFF5533));
            g2.fillOval(ax - r / 2, ay - r / 2, r / 2, r / 2);

            g2.setColor(new Color(0x33AA44));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(ax, ay - r, ax + 6, ay - r - 7);

            int barW = 60;
            int barH = 8;
            int barX = ax - barW / 2;
            int barY = ay - r - 20;
            g2.setColor(new Color(50, 50, 50));
            g2.fillRoundRect(barX, barY, barW, barH, 4, 4);
            g2.setColor(hpColor(model.getAppleHp()));
            int filled = (int)(barW * model.getAppleHp() / (double) DefenseModel.APPLE_MAX_HP);
            g2.fillRoundRect(barX, barY, filled, barH, 4, 4);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString(model.getAppleHp() + " HP", barX + barW + 4, barY + barH - 1);
        }

        private Color hpColor(int hp) {
            if (hp >= 7) return new Color(0x44DD66);
            if (hp >= 4) return new Color(0xFFCC00);
            return new Color(0xFF3322);
        }

        private void drawWorms(Graphics2D g2) {
            java.awt.Point apple = model.getApple();

            for (DefenseModel.Worm w : model.getWorms()) {
                if (!w.alive) continue;

                double dx = apple.x - w.x;
                double dy = apple.y - w.y;
                double dist = Math.hypot(dx, dy);
                if (dist < 1) dist = 1;
                double nx = dx / dist;
                double ny = dy / dist;
                double px = -ny;
                double py =  nx;

                int headR   = (int) w.radius;
                int segR    = headR - 2;
                int segCount = 4;
                int segGap  = segR * 2 - 1;

                for (int i = segCount; i >= 1; i--) {
                    double cx = w.x - nx * segGap * i;
                    double cy = w.y - ny * segGap * i;
                    g2.setColor(w.color);
                    g2.fillOval((int)(cx - segR), (int)(cy - segR), segR * 2, segR * 2);
                }

                int hx = (int) w.x;
                int hy = (int) w.y;
                g2.setColor(w.color);
                g2.fillOval(hx - headR, hy - headR, headR * 2, headR * 2);

                int eyeR = 2;
                int e1x = (int)(hx + px * 3 + nx * 4);
                int e1y = (int)(hy + py * 3 + ny * 4);
                int e2x = (int)(hx - px * 3 + nx * 4);
                int e2y = (int)(hy - py * 3 + ny * 4);
                g2.setColor(Color.WHITE);
                g2.fillOval(e1x - eyeR, e1y - eyeR, eyeR * 2, eyeR * 2);
                g2.fillOval(e2x - eyeR, e2y - eyeR, eyeR * 2, eyeR * 2);
                g2.setColor(Color.BLACK);
                g2.fillOval(e1x - 1, e1y - 1, 2, 2);
                g2.fillOval(e2x - 1, e2y - 1, 2, 2);
            }
        }

        private void drawRobot(Graphics2D g2, DefenseModel.Robot robot, String label) {
            int rx = (int) robot.x;
            int ry = (int) robot.y;
            int r  = (int) robot.radius;

            java.awt.geom.AffineTransform saved = g2.getTransform();
            g2.rotate(robot.angle, rx, ry);

            int wheelW = 6;
            int wheelH = 14;
            g2.setColor(new Color(50, 50, 50));
            g2.fillRoundRect(rx - r - wheelW + 1, ry - wheelH / 2, wheelW, wheelH, 3, 3);
            g2.fillRoundRect(rx + r - 1,           ry - wheelH / 2, wheelW, wheelH, 3, 3);

            int bw = r * 2 - 2;
            int bh = r * 2 - 4;
            g2.setColor(robot.color);
            g2.fillRoundRect(rx - bw / 2, ry - bh / 2, bw, bh, 8, 8);

            g2.setColor(new Color(20, 20, 20));
            g2.fillOval(rx - 6, ry - 4, 12, 8);
            g2.setColor(new Color(80, 200, 255));
            g2.fillOval(rx - 4, ry - 2, 8, 5);

            g2.setColor(new Color(210, 210, 210));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(rx + bw / 2, ry - 4, rx + bw / 2 + 10, ry - 8);
            g2.fillOval(rx + bw / 2 + 8, ry - 10, 4, 4);
            g2.drawLine(rx + bw / 2, ry + 4, rx + bw / 2 + 10, ry + 8);
            g2.fillOval(rx + bw / 2 + 8, ry + 6, 4, 4);

            g2.setStroke(new BasicStroke(1f));
            g2.setTransform(saved);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            String firstLine = label.split("\n")[0];
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(firstLine, rx - fm.stringWidth(firstLine) / 2, ry + r + 16);

            if (label.contains("\n")) {
                String secondLine = label.split("\n")[1];
                g2.setFont(new Font("Arial", Font.PLAIN, 9));
                g2.setColor(new Color(180, 180, 200));
                fm = g2.getFontMetrics();
                g2.drawString(secondLine, rx - fm.stringWidth(secondLine) / 2, ry + r + 27);
            }
        }

        private void drawHUD(Graphics2D g2) {
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.setColor(Color.WHITE);

            int wave = model.getCurrentWave() + 1;
            if (model.getPhase() == DefenseModel.GamePhase.WAITING) wave = 1;
            if (model.getPhase() == DefenseModel.GamePhase.VICTORY || model.getPhase() == DefenseModel.GamePhase.DEFEAT)  wave = DefenseModel.TOTAL_WAVES;

            g2.drawString("Волна: " + Math.min(wave, DefenseModel.TOTAL_WAVES) + " / " + DefenseModel.TOTAL_WAVES, 12, 24);

            long alive = model.getWorms().stream().filter(w -> w.alive).count();
            g2.drawString("Червяков: " + alive, 12, 46);

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(180, 180, 200));
            g2.drawString("Игрок 1: WASD", 12, getHeight() - 22);
            g2.drawString("Игрок 2: Стрелки", 12, getHeight() - 8);

            if (model.getPhase() == DefenseModel.GamePhase.BETWEEN_WAVES) {
                long sec = (model.getRemainingPauseMs() + 999) / 1000;
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.setColor(new Color(255, 220, 80));
                String msg = "Следующая волна через " + sec + " с...";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, 34);
            }
        }

        private void drawOverlay(Graphics2D g2) {
            DefenseModel.GamePhase phase = model.getPhase();

            String title    = null;
            String subtitle = null;
            Color  titleColor = Color.WHITE;

            switch (phase) {
                case WAITING:
                    title     = "ЗАЩИТА ЯБЛОКА";
                    subtitle  = "Нажмите «Начать игру»";
                    titleColor = new Color(255, 220, 60);
                    break;
                case VICTORY:
                    title     = "ПОБЕДА!";
                    subtitle  = "Яблоко выжило! HP: " + model.getAppleHp();
                    titleColor = new Color(80, 230, 120);
                    break;
                case DEFEAT:
                    title     = "ПОРАЖЕНИЕ";
                    subtitle  = "Червяки добрались до яблока :(";
                    titleColor = new Color(230, 80, 80);
                    break;
                default:
                    return;
            }

            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRect(0, getHeight() / 2 - 60, getWidth(), 110);

            g2.setFont(new Font("Arial", Font.BOLD, 32));
            g2.setColor(titleColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, getHeight() / 2 - 10);

            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            g2.setColor(Color.WHITE);
            fm = g2.getFontMetrics();
            g2.drawString(subtitle, (getWidth() - fm.stringWidth(subtitle)) / 2, getHeight() / 2 + 25);
        }
    }
}