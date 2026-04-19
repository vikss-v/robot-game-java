package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import model.RobotModel;
import model.RobotModelListener;
import model.ThemeData;
import model.ThemeManager;

public class GameVisualizer extends JPanel implements RobotModelListener
{
    private final RobotModel model;
    private final ThemeManager themeManager;
    private final Timer m_timer = initTimer();

    private static Timer initTimer()
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private final List<double[]> drawDraftPath = new ArrayList<>();
    private boolean drawingMode = false;
    private double[] singleClickTarget = null;

    public GameVisualizer(RobotModel model, ThemeManager themeManager)
    {
        this.model = model;
        this.themeManager = themeManager;
        this.model.addListener(this);

        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                model.updateModel(10);
            }
        }, 0, 10);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (drawingMode) {
                    drawDraftPath.clear();
                    drawDraftPath.add(toListModelCoords(e.getX(), e.getY()));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawingMode) {
                    drawDraftPath.add(toListModelCoords(e.getX(), e.getY()));
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drawingMode) {
                    if (drawDraftPath.size() >= 2) {
                        model.setPath(new ArrayList<>(drawDraftPath));
                        singleClickTarget = null;
                    }
                    drawDraftPath.clear();
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!drawingMode) {
                    double[] target = toListModelCoords(e.getX(), e.getY());
                    List<double[]> singlePointPath = new ArrayList<>();
                    singlePointPath.add(target);
                    model.setPath(singlePointPath);
                    singleClickTarget = target;
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        setDoubleBuffered(true);

        applyTheme();
    }

    public void setDrawingMode(boolean enabled) {
        drawingMode = enabled;
        if (enabled) {
            model.setPath(new ArrayList<>());
            singleClickTarget = null;
            drawDraftPath.clear();
        } else {
            drawDraftPath.clear();
        }
        repaint();
    }

    public boolean isDrawingMode() {
        return drawingMode;
    }

    public void applyTheme() {
        ThemeData.BackgroundTheme bgTheme = themeManager.getCurrentBackgroundTheme();
        setBackground(bgTheme.getBackgroundColor());
        repaint();
    }

    public void refreshTheme() {
        applyTheme();
    }

    private double[] toListModelCoords(int screenX, int screenY) {
        return new double[]{screenX, screenY};
    }

    @Override
    public void onModelChanged(RobotModel model) {
        EventQueue.invokeLater(this::repaint);
    }

    private static int round(double value)
    {
        return (int) (value + 0.5);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ThemeData.BackgroundTheme bgTheme = themeManager.getCurrentBackgroundTheme();

        drawPath(g2d, model.getPath(), bgTheme.getPathColor(), false);
        drawPath(g2d, drawDraftPath, bgTheme.getDraftPathColor(), true);

        if (singleClickTarget != null && model.getPath().size() == 1) {
            drawTargetPoint(g2d, singleClickTarget);
        }

        drawRobot(g2d, round(model.getX()), round(model.getY()), model.getDirection());
    }

    private void drawTargetPoint(Graphics2D g, double[] target) {
        ThemeData.TargetTheme targetTheme = themeManager.getCurrentTargetTheme();

        int x = round(target[0]);
        int y = round(target[1]);

        AffineTransform oldTransform = g.getTransform();
        g.setTransform(new AffineTransform());
        g.setColor(targetTheme.getColor());

        int size = targetTheme.size;

        switch (targetTheme.shape) {
            case "square":
                g.fillRect(x - size/2, y - size/2, size, size);
                break;
            case "cross":
                g.setStroke(new BasicStroke(2f));
                g.drawLine(x - size, y - size, x + size, y + size);
                g.drawLine(x + size, y - size, x - size, y + size);
                break;
            case "circle":
            default:
                g.fillOval(x - size/2, y - size/2, size, size);
                break;
        }

        g.setTransform(oldTransform);
    }

    private void drawPath(Graphics2D g, List<double[]> path, Color color, boolean isDraft) {
        if (path.size() < 2) return;

        g.setTransform(new AffineTransform());
        g.setColor(color);

        if (isDraft) {
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{6f, 4f}, 0f));
        } else {
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        for (int i = 1; i < path.size(); i++) {
            double[] prev = path.get(i - 1);
            double[] curr = path.get(i);
            g.drawLine(round(prev[0]), round(prev[1]), round(curr[0]), round(curr[1]));
        }

        if (!path.isEmpty()) {
            double[] first = path.get(0);
            g.setColor(color.darker());
            g.setStroke(new BasicStroke(1f));
            fillOval(g, round(first[0]), round(first[1]), 8, 8);
        }
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        ThemeData.RobotTheme robotTheme = themeManager.getCurrentRobotTheme();

        int robotCenterX = x;
        int robotCenterY = y;
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY);
        g.setTransform(t);

        g.setColor(robotTheme.getBodyColor());
        fillOval(g, robotCenterX, robotCenterY, robotTheme.bodyWidth, robotTheme.bodyHeight);
        g.setColor(robotTheme.getBorderColor());
        drawOval(g, robotCenterX, robotCenterY, robotTheme.bodyWidth, robotTheme.bodyHeight);

        g.setColor(robotTheme.getEyeColor());
        fillOval(g, robotCenterX + robotTheme.eyeOffsetX, robotCenterY, robotTheme.eyeSize, robotTheme.eyeSize);
        g.setColor(robotTheme.getEyeBorderColor());
        drawOval(g, robotCenterX + robotTheme.eyeOffsetX, robotCenterY, robotTheme.eyeSize, robotTheme.eyeSize);

        g.setTransform(new AffineTransform());
    }
}