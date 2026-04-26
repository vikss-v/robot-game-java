package gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class SnakeSetupDialog extends JDialog {

    public static class Config {
        public final String name1;
        public final Color color1;
        public final String name2;
        public final Color color2;
        public final String backgroundKey;

        public Config(String name1, Color color1, String name2, Color color2, String backgroundKey) {
            this.name1 = name1;
            this.color1 = color1;
            this.name2 = name2;
            this.color2 = color2;
            this.backgroundKey = backgroundKey;
        }
    }

    private static final Map<String, Color> COLORS = new LinkedHashMap<>();
    static {
        COLORS.put("Синий",      new Color(0x4A90D9));
        COLORS.put("Красный",    new Color(0xFF3232));
        COLORS.put("Зелёный",   new Color(0x3CB371));
        COLORS.put("Золотой",    new Color(0xFFD700));
        COLORS.put("Фиолетовый", new Color(0x9B59B6));
        COLORS.put("Оранжевый",  new Color(0xFF8C00));
    }

    private static final String[] BG_NAMES = {"Светлая", "Тёмная", "Аврора", "Закат"};
    private static final Color[] BG_COLORS = {
            new Color(0xFFFFFF), new Color(0x1E1E2E), new Color(0x0D1B2A), new Color(0x2D1B00)
    };

    private Config result = null;

    private final JTextField name1Field = new JTextField("Червяк 1", 12);
    private final JComboBox<String> color1Combo;
    private final JTextField name2Field = new JTextField("Червяк 2", 12);
    private final JComboBox<String> color2Combo;
    private final JComboBox<String> bgCombo;

    public SnakeSetupDialog(JFrame parent) {
        super(parent, "Настройка игры", true);

        String[] colorNames = COLORS.keySet().toArray(new String[0]);
        color1Combo = new JComboBox<>(colorNames);
        color1Combo.setSelectedIndex(0);

        color2Combo = new JComboBox<>(colorNames);
        color2Combo.setSelectedIndex(1);

        bgCombo = new JComboBox<>(BG_NAMES);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;

        addSection(panel, gc, "── Червяк 1 (WASD) ──", 0);
        addRow(panel, gc, "Имя:", name1Field, 1);
        addRow(panel, gc, "Цвет:", color1Combo, 2);

        addSection(panel, gc, "── Червяк 2 (стрелки) ──", 3);
        addRow(panel, gc, "Имя:", name2Field, 4);
        addRow(panel, gc, "Цвет:", color2Combo, 5);

        addSection(panel, gc, "── Поле ──", 6);
        addRow(panel, gc, "Фон:", bgCombo, 7);

        int option = JOptionPane.showConfirmDialog(parent, panel,
                "Настройка игры", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String n1;
            String name1Raw = name1Field.getText().trim();
            if (name1Raw.isEmpty()) {
                n1 = "Червяк 1";
            } else {
                n1 = name1Raw;
            }

            String n2;
            String name2Raw = name2Field.getText().trim();
            if (name2Raw.isEmpty()) {
                n2 = "Червяк 2";
            } else {
                n2 = name2Raw;
            }
            Color c1 = COLORS.get(colorNames[color1Combo.getSelectedIndex()]);
            Color c2 = COLORS.get(colorNames[color2Combo.getSelectedIndex()]);
            result = new Config(n1, c1, n2, c2, BG_NAMES[bgCombo.getSelectedIndex()]);
        }
    }

    private void addSection(JPanel p, GridBagConstraints gc, String title, int row) {
        gc.gridy = row; gc.gridx = 0; gc.gridwidth = 2;
        p.add(new JLabel("<html><b>" + title + "</b></html>"), gc);
        gc.gridwidth = 1;
    }

    private void addRow(JPanel p, GridBagConstraints gc, String label, java.awt.Component field, int row) {
        gc.gridy = row; gc.gridx = 0; p.add(new JLabel(label), gc);
        gc.gridx = 1; p.add(field, gc);
    }

    public Config getResult() { return result; }

    public static Color[] getBgColors() { return BG_COLORS; }
}