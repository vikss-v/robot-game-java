package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.RobotModel;
import model.ThemeManager;

public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;

    public GameWindow(RobotModel model, ThemeManager themeManager)
    {
        super("Игровое поле", true, true, true, true);
        getContentPane().setBackground(new Color(255, 240, 245));

        m_visualizer = new GameVisualizer(model, themeManager);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 240, 245));
        panel.add(m_visualizer, BorderLayout.CENTER);
        panel.add(createToolbar(), BorderLayout.SOUTH);
        getContentPane().add(panel);
        pack();
    }

    public GameVisualizer getVisualizer() {
        return m_visualizer;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(new Color(255, 220, 235));
        toolbar.setBorder(new EmptyBorder(5, 10, 5, 10));

        JButton drawButton = new JButton("Нарисовать маршрут робота");
        drawButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        drawButton.setBackground(new Color(255, 200, 215));
        drawButton.setForeground(new Color(180, 80, 120));
        drawButton.setFocusPainted(false);
        drawButton.setBorderPainted(false);

        drawButton.addActionListener(e -> {
            boolean nowDrawing = !m_visualizer.isDrawingMode();
            m_visualizer.setDrawingMode(nowDrawing);
            drawButton.setText(nowDrawing ? "Рисую маршрут..." : "Нарисовать маршрут");
        });

        toolbar.add(drawButton);
        return toolbar;
    }
}