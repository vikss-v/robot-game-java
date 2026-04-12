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

public class GameVisualizer extends JPanel implements RobotModelListener
{
    private final RobotModel model;
    private final Timer m_timer = initTimer();

    private static Timer initTimer()
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private final List<double[]> drawDraftPath = new ArrayList<>();
    private boolean drawingMode = false;

    public GameVisualizer(RobotModel model)
    {
        this.model = model;
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
                if (!drawingMode) return;
                drawDraftPath.clear();
                drawDraftPath.add(toListModelCoords(e.getX(), e.getY()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!drawingMode) return;
                drawDraftPath.add(toListModelCoords(e.getX(), e.getY()));
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!drawingMode || drawDraftPath.isEmpty()) return;
                model.setPath(new ArrayList<>(drawDraftPath));
                drawDraftPath.clear();
                repaint();
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        setDoubleBuffered(true);
    }

    public void setDrawingMode(boolean enabled) {
        drawingMode = enabled;
        if (!enabled) {
            drawDraftPath.clear();
            repaint();
        }
    }

    public boolean isDrawingMode() {
        return drawingMode;
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

        drawPath(g2d, model.getPath(), new Color(110, 100, 255), false);
        drawPath(g2d, drawDraftPath, new Color(109, 255, 60), true);

        drawRobot(g2d, round(model.getX()), round(model.getY()), model.getDirection());
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
        int robotCenterX = x;
        int robotCenterY = y;
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX + 10, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX + 10, robotCenterY, 5, 5);
        g.setTransform(new AffineTransform());
    }
}