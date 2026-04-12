package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import model.RobotModel;

public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;

    public GameWindow(RobotModel model)
    {
        super("Игровое поле", true, true, true, true);
        m_visualizer = new GameVisualizer(model);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        panel.add(createToolbar(), BorderLayout.SOUTH);
        getContentPane().add(panel);
        pack();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        JButton drawButton = new JButton("Нарисовать маршрут робота");
        drawButton.setToolTipText("Зажмите кнопку мыши на поле для рисования маршрута");

        drawButton.addActionListener(e -> {
            boolean nowDrawing = !m_visualizer.isDrawingMode();
            m_visualizer.setDrawingMode(nowDrawing);
            drawButton.setText(nowDrawing ? "Рисую маршрут..." : "Нарисовать маршрут");
        });

        toolbar.add(drawButton);
        return toolbar;
    }
}