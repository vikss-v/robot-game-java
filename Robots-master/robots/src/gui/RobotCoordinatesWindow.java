package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import model.RobotModel;
import model.RobotModelListener;

public class RobotCoordinatesWindow extends JInternalFrame implements RobotModelListener {

    private final JLabel xLabel;
    private final JLabel yLabel;
    private final JLabel directionLabel;
    private final JLabel targetXLabel;
    private final JLabel targetYLabel;
    private final DecimalFormat df = new DecimalFormat("#0.00");

    public RobotCoordinatesWindow(RobotModel model) {
        super("Координаты робота", true, true, true, true);
        getContentPane().setBackground(new Color(255, 240, 245));

        model.addListener(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 240, 245));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 5, 10));
        infoPanel.setBackground(new Color(255, 240, 245));
        infoPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 200, 215)),
                "Параметры робота", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(180, 80, 120)));

        JLabel xTitle = new JLabel("Позиция X:", SwingConstants.RIGHT);
        xTitle.setForeground(new Color(180, 80, 120));
        xTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(xTitle);

        xLabel = new JLabel("0.00");
        xLabel.setForeground(new Color(150, 60, 100));
        xLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoPanel.add(xLabel);

        JLabel yTitle = new JLabel("Позиция Y:", SwingConstants.RIGHT);
        yTitle.setForeground(new Color(180, 80, 120));
        yTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(yTitle);

        yLabel = new JLabel("0.00");
        yLabel.setForeground(new Color(150, 60, 100));
        yLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoPanel.add(yLabel);

        JLabel dirTitle = new JLabel("Направление (градусы):", SwingConstants.RIGHT);
        dirTitle.setForeground(new Color(180, 80, 120));
        dirTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(dirTitle);

        directionLabel = new JLabel("0.00");
        directionLabel.setForeground(new Color(150, 60, 100));
        directionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoPanel.add(directionLabel);

        JLabel targetXTitle = new JLabel("Цель X:", SwingConstants.RIGHT);
        targetXTitle.setForeground(new Color(180, 80, 120));
        targetXTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(targetXTitle);

        targetXLabel = new JLabel("-");
        targetXLabel.setForeground(new Color(150, 60, 100));
        targetXLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoPanel.add(targetXLabel);

        JLabel targetYTitle = new JLabel("Цель Y:", SwingConstants.RIGHT);
        targetYTitle.setForeground(new Color(180, 80, 120));
        targetYTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(targetYTitle);

        targetYLabel = new JLabel("-");
        targetYLabel.setForeground(new Color(150, 60, 100));
        targetYLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoPanel.add(targetYLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        getContentPane().add(panel);
        setSize(250, 220);
        setLocation(320, 10);

        updateCoordinates(model);
    }

    private void updateCoordinates(RobotModel model)
    {
        SwingUtilities.invokeLater(() -> {
            xLabel.setText(df.format(model.getX()));
            yLabel.setText(df.format(model.getY()));
            directionLabel.setText(df.format(Math.toDegrees(model.getDirection())));

            double[] target = model.getCurrentTarget();
            if (target != null) {
                targetXLabel.setText(df.format(target[0]));
                targetYLabel.setText(df.format(target[1]));
            } else {
                targetXLabel.setText("-");
                targetYLabel.setText("-");
            }
        });
    }

    @Override
    public void onModelChanged(RobotModel model) {
        updateCoordinates(model);
    }

    private static class BorderFactory {
        public static javax.swing.border.Border createLineBorder(Color color) {
            return javax.swing.BorderFactory.createLineBorder(color);
        }
    }
}