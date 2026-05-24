package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import model.ThemeData;
import model.ThemeManager;

public class SettingsWindow extends JInternalFrame
{
    private final ThemeManager themeManager;
    private final GameWindow gameWindow;
    private final Random random = new Random();

    private JComboBox<String> robotCombo;
    private JComboBox<String> targetCombo;
    private JComboBox<String> backgroundCombo;

    private ThemeData.RobotTheme selectedRobotTheme;
    private ThemeData.TargetTheme selectedTargetTheme;
    private ThemeData.BackgroundTheme selectedBackgroundTheme;

    public SettingsWindow(ThemeManager themeManager, GameWindow gameWindow)
    {
        super("Настройки", true, true, true, true);
        this.themeManager = themeManager;
        this.gameWindow = gameWindow;

        getContentPane().setBackground(new Color(255, 240, 245));

        selectedRobotTheme = themeManager.getCurrentRobotTheme();
        selectedTargetTheme = themeManager.getCurrentTargetTheme();
        selectedBackgroundTheme = themeManager.getCurrentBackgroundTheme();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(255, 240, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 10, 15));
        optionsPanel.setBackground(new Color(255, 240, 245));
        optionsPanel.add(createRobotPanel());
        optionsPanel.add(createTargetPanel());
        optionsPanel.add(createBackgroundPanel());

        mainPanel.add(optionsPanel, BorderLayout.CENTER);
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        setSize(400, 350);
        setLocation(700, 100);
    }

    private JPanel createRobotPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(255, 240, 245));
        panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 200, 215)),
                "Выбор робота", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(180, 80, 120)));

        String[] robotNames = themeManager.getRobotThemes().stream()
                .map(t -> t.name)
                .toArray(String[]::new);
        robotCombo = new JComboBox<>(robotNames);
        robotCombo.setBackground(new Color(255, 245, 250));
        robotCombo.setForeground(new Color(180, 80, 120));
        robotCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        robotCombo.setSelectedItem(selectedRobotTheme.name);
        robotCombo.addActionListener(e -> {
            int index = robotCombo.getSelectedIndex();
            selectedRobotTheme = themeManager.getRobotThemes().get(index);
        });

        JLabel label = new JLabel("Тема:");
        label.setForeground(new Color(180, 80, 120));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, BorderLayout.WEST);
        panel.add(robotCombo, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTargetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(255, 240, 245));
        panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 200, 215)),
                "Выбор цели", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(180, 80, 120)));

        String[] targetNames = themeManager.getTargetThemes().stream()
                .map(t -> t.name)
                .toArray(String[]::new);
        targetCombo = new JComboBox<>(targetNames);
        targetCombo.setBackground(new Color(255, 245, 250));
        targetCombo.setForeground(new Color(180, 80, 120));
        targetCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        targetCombo.setSelectedItem(selectedTargetTheme.name);
        targetCombo.addActionListener(e -> {
            int index = targetCombo.getSelectedIndex();
            selectedTargetTheme = themeManager.getTargetThemes().get(index);
        });

        JLabel label = new JLabel("Тема:");
        label.setForeground(new Color(180, 80, 120));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, BorderLayout.WEST);
        panel.add(targetCombo, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBackgroundPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(255, 240, 245));
        panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 200, 215)),
                "Выбор фона", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(180, 80, 120)));

        String[] bgNames = themeManager.getBackgroundThemes().stream()
                .map(t -> t.name)
                .toArray(String[]::new);
        backgroundCombo = new JComboBox<>(bgNames);
        backgroundCombo.setBackground(new Color(255, 245, 250));
        backgroundCombo.setForeground(new Color(180, 80, 120));
        backgroundCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backgroundCombo.setSelectedItem(selectedBackgroundTheme.name);
        backgroundCombo.addActionListener(e -> {
            int index = backgroundCombo.getSelectedIndex();
            selectedBackgroundTheme = themeManager.getBackgroundThemes().get(index);
        });

        JLabel label = new JLabel("Тема:");
        label.setForeground(new Color(180, 80, 120));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, BorderLayout.WEST);
        panel.add(backgroundCombo, BorderLayout.CENTER);

        return panel;
    }

    private void applyRandomThemes() {
        int robotIndex = random.nextInt(themeManager.getRobotThemes().size());
        int targetIndex = random.nextInt(themeManager.getTargetThemes().size());
        int bgIndex = random.nextInt(themeManager.getBackgroundThemes().size());

        selectedRobotTheme = themeManager.getRobotThemes().get(robotIndex);
        selectedTargetTheme = themeManager.getTargetThemes().get(targetIndex);
        selectedBackgroundTheme = themeManager.getBackgroundThemes().get(bgIndex);

        robotCombo.setSelectedItem(selectedRobotTheme.name);
        targetCombo.setSelectedItem(selectedTargetTheme.name);
        backgroundCombo.setSelectedItem(selectedBackgroundTheme.name);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(255, 240, 245));

        JButton randomButton = new JButton("Случайная тема");
        randomButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        randomButton.setBackground(new Color(255, 200, 215));
        randomButton.setForeground(new Color(180, 80, 120));
        randomButton.setFocusPainted(false);
        randomButton.setBorderPainted(false);

        JButton applyButton = new JButton("Применить");
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        applyButton.setBackground(new Color(255, 180, 200));
        applyButton.setForeground(new Color(180, 80, 120));
        applyButton.setFocusPainted(false);
        applyButton.setBorderPainted(false);

        randomButton.addActionListener(e -> {
            applyRandomThemes();
        });

        applyButton.addActionListener(e -> {
            themeManager.setCurrentRobotTheme(selectedRobotTheme);
            themeManager.setCurrentTargetTheme(selectedTargetTheme);
            themeManager.setCurrentBackgroundTheme(selectedBackgroundTheme);
            themeManager.saveCurrentThemes();

            gameWindow.getVisualizer().refreshTheme();

            JOptionPane.showMessageDialog(this, "Настройки успешно применены!", "Успех", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(randomButton);
        buttonPanel.add(applyButton);

        return buttonPanel;
    }
}