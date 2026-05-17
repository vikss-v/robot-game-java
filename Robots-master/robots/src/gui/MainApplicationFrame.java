package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.beans.PropertyVetoException;

import log.Logger;
import model.HunterSnakeModel;
import model.RobotModel;
import model.ThemeManager;
import java.awt.Color;
import model.SnakeModel;
import model.DefenseModel;

public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final ThemeManager themeManager = new ThemeManager();

    private static final String CONFIG_DIR = System.getProperty("user.home");
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "robots_config.properties";

    private final RobotModel robotModel = new RobotModel();

    public MainApplicationFrame() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);

        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow(robotModel, themeManager);
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        RobotCoordinatesWindow coordinatesWindow = new RobotCoordinatesWindow(robotModel);
        addWindow(coordinatesWindow);

        SettingsWindow settingsWindow = new SettingsWindow(themeManager, gameWindow);
        addWindow(settingsWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void saveWindowStates()
    {
        Properties props = new Properties();
        JInternalFrame[] frames = desktopPane.getAllFrames();

        for (JInternalFrame frame : frames)
        {
            String title = frame.getTitle();
            boolean iconified = frame.isIcon();

            props.setProperty(title + ".iconified", String.valueOf(iconified));

            if (iconified)
            {
                int iconX = frame.getDesktopIcon().getX();
                int iconY = frame.getDesktopIcon().getY();
                props.setProperty(title + ".iconX", String.valueOf(iconX));
                props.setProperty(title + ".iconY", String.valueOf(iconY));

                try
                {
                    frame.setIcon(false);
                }
                catch (PropertyVetoException ex)
                {
                }
            }

            props.setProperty(title + ".width", String.valueOf(frame.getWidth()));
            props.setProperty(title + ".height", String.valueOf(frame.getHeight()));
            props.setProperty(title + ".x", String.valueOf(frame.getX()));
            props.setProperty(title + ".y", String.valueOf(frame.getY()));

            if (iconified)
            {
                try
                {
                    frame.setIcon(true);
                }
                catch (PropertyVetoException ex)
                {
                }
            }
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE))
        {
            props.store(out, null);
        }
        catch (IOException e)
        {
            Logger.debug("Не получилось сохранить конфигурацию окон");
        }
    }

    public void restoreWindowStates()
    {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists())
        {
            return;
        }

        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(configFile))
        {
            props.load(in);
        }
        catch (IOException e)
        {
            Logger.debug("Не получилось загрузить конфигурацию окон");
            return;
        }

        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames)
        {
            String title = frame.getTitle();
            if (props.getProperty(title + ".x") == null ||
                    props.getProperty(title + ".y") == null ||
                    props.getProperty(title + ".width") == null ||
                    props.getProperty(title + ".height") == null ||
                    props.getProperty(title + ".iconified") == null) {
                continue;
            }

            boolean iconified = Boolean.parseBoolean(props.getProperty(title + ".iconified"));
            int width = Integer.parseInt(props.getProperty(title + ".width"));
            int height = Integer.parseInt(props.getProperty(title + ".height"));
            int x = Integer.parseInt(props.getProperty(title + ".x"));
            int y = Integer.parseInt(props.getProperty(title + ".y"));

            frame.setSize(width, height);
            frame.setLocation(x, y);

            if (iconified)
            {
                try
                {
                    frame.setIcon(true);
                }
                catch (PropertyVetoException ex){

                }
            }
        }
    }

    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createFileMenu());
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createGameMenu());

        return menuBar;
    }

    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitMenuItem = new JMenuItem("Выход", KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        exitMenuItem.addActionListener((event) -> {
            exitApplication();
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createLookAndFeelMenu()
    {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);

        return lookAndFeelMenu;
    }

    private JMenu createTestMenu()
    {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug("Новая строка");
        });
        testMenu.add(addLogMessageItem);

        return testMenu;
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
        }
    }

    private void exitApplication()
    {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Вы действительно хотите выйти из приложения?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            saveWindowStates();
            System.exit(0);
        }
    }

    private JMenu createGameMenu() {
        JMenu gameMenu = new JMenu("Игра");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        JMenuItem snakeItem = new JMenuItem("Битва червяков", KeyEvent.VK_C);
        snakeItem.addActionListener(e -> openSnakeGame());
        gameMenu.add(snakeItem);

        JMenuItem hunterItem = new JMenuItem("Охотник", KeyEvent.VK_H);
        hunterItem.addActionListener(e -> openHunterGame());
        gameMenu.add(hunterItem);

        JMenuItem defenseItem = new JMenuItem("Защита яблока", KeyEvent.VK_Z);
        defenseItem.addActionListener(e -> openDefenseGame());
        gameMenu.add(defenseItem);

        return gameMenu;
    }

    private void openHunterGame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int gameWidth = screenSize.width - 100;
        int gameHeight = screenSize.height - 150;

        HunterSnakeModel model = new HunterSnakeModel(gameWidth, gameHeight, "Hunter", new Color(0x4A90D9));
        HunterGameWindow window = new HunterGameWindow(model);

        window.setMaximizable(true);
        window.setSize(gameWidth, gameHeight);
        window.setLocation(50, 30);

        addWindow(window);
    }

    private void openSnakeGame() {
        SnakeSetupDialog dialog = new SnakeSetupDialog(this);
        SnakeSetupDialog.Config config = dialog.getResult();
        if (config == null) return;

        Color bgColor = SnakeSetupDialog.getBgColors()[
                java.util.Arrays.asList(
                        new String[]{"Светлая", "Тёмная", "Аврора", "Закат"}
                ).indexOf(config.backgroundKey)
                ];
        if (bgColor == null) bgColor = Color.WHITE;

        SnakeModel snakeModel = new SnakeModel(600, 500,
                config.name1, config.color1,
                config.name2, config.color2);

        SnakeGameWindow snakeWindow = new SnakeGameWindow(snakeModel, bgColor);
        addWindow(snakeWindow);
        restoreFrameState(snakeWindow, 0, 0, 616, 590);

        snakeWindow.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
                saveFrameState(snakeWindow);
            }
        });
    }

    private void openDefenseGame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = screenSize.width  - 100;
        int h = screenSize.height - 150;

        DefenseModel model = new DefenseModel(w, h);
        DefenseGameWindow window = new DefenseGameWindow(model);

        window.setSize(w, h);
        window.setLocation(50, 30);
        addWindow(window);
    }

    private void saveFrameState(JInternalFrame frame) {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException ignored) {}
        }
        String title = frame.getTitle();
        props.setProperty(title + ".x",        String.valueOf(frame.getX()));
        props.setProperty(title + ".y",        String.valueOf(frame.getY()));
        props.setProperty(title + ".width",    String.valueOf(frame.getWidth()));
        props.setProperty(title + ".height",   String.valueOf(frame.getHeight()));
        props.setProperty(title + ".iconified", String.valueOf(frame.isIcon()));
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            props.store(out, null);
        } catch (IOException ignored) {}
    }

    private void restoreFrameState(JInternalFrame frame, int defX, int defY, int defW, int defH) {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) { frame.setLocation(defX, defY); frame.setSize(defW, defH); return; }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(configFile)) { props.load(in); }
        catch (IOException e) { frame.setLocation(defX, defY); frame.setSize(defW, defH); return; }
        String title = frame.getTitle();
        if (props.getProperty(title + ".x") == null) { frame.setLocation(defX, defY); frame.setSize(defW, defH); return; }
        frame.setLocation(Integer.parseInt(props.getProperty(title + ".x")),
                Integer.parseInt(props.getProperty(title + ".y")));
        frame.setSize(Integer.parseInt(props.getProperty(title + ".width")),
                Integer.parseInt(props.getProperty(title + ".height")));
        if (Boolean.parseBoolean(props.getProperty(title + ".iconified", "false"))) {
            try { frame.setIcon(true); } catch (java.beans.PropertyVetoException ignored) {}
        }
    }

    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            exitApplication();
        } else {
            super.processWindowEvent(e);
        }
    }
}