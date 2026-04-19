package model;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import log.Logger;

public class ThemeManager {
    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + "robots_theme_config.txt";

    private List<ThemeData.RobotTheme> robotThemes = new ArrayList<>();
    private List<ThemeData.TargetTheme> targetThemes = new ArrayList<>();
    private List<ThemeData.BackgroundTheme> backgroundThemes = new ArrayList<>();

    private ThemeData.RobotTheme currentRobotTheme;
    private ThemeData.TargetTheme currentTargetTheme;
    private ThemeData.BackgroundTheme currentBackgroundTheme;

    public ThemeManager() {
        loadAllThemes();
        loadSavedThemes();
    }

    private void loadAllThemes() {
        robotThemes = loadThemesFromClasspath("/themes/robots/", "robot");
        targetThemes = loadThemesFromClasspath("/themes/targets/", "target");
        backgroundThemes = loadThemesFromClasspath("/themes/backgrounds/", "background");

        if (robotThemes.isEmpty()) {
            Logger.debug("Темы роботов не найдены, создаём тему по умолчанию");
            robotThemes.add(createDefaultRobotTheme());
        }
        if (targetThemes.isEmpty()) {
            Logger.debug("Темы целей не найдены, создаём тему по умолчанию");
            targetThemes.add(createDefaultTargetTheme());
        }
        if (backgroundThemes.isEmpty()) {
            Logger.debug("Темы фона не найдены, создаём тему по умолчанию");
            backgroundThemes.add(createDefaultBackgroundTheme());
        }

        currentRobotTheme = robotThemes.get(0);
        currentTargetTheme = targetThemes.get(0);
        currentBackgroundTheme = backgroundThemes.get(0);
    }

    private <T> List<T> loadThemesFromClasspath(String resourcePath, String type) {
        List<T> themes = new ArrayList<>();

        try {
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                Logger.debug("Ресурс не найден: " + resourcePath);
                return themes;
            }

            Path dir;
            try {
                dir = Paths.get(resourceUrl.toURI());
            } catch (Exception e) {
                Logger.debug("Не удалось получить URI для " + resourcePath + ", используем getPath(): " + e.getMessage());
                dir = Paths.get(resourceUrl.getPath());
            }

            if (!Files.exists(dir)) {
                Logger.debug("Директория не существует: " + resourcePath);
                return themes;
            }

            File folder = dir.toFile();
            File[] files = folder.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];

                    if (file.isFile() && file.getName().endsWith(".json")) {
                        try {
                            String json = readFileToString(file);
                            T theme = parseTheme(json, type);
                            if (theme != null) {
                                themes.add(theme);
                            }
                        } catch (Exception e) {
                            Logger.error("Ошибка загрузки темы из файла " + file.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            Logger.error("Ошибка загрузки тем из " + resourcePath + ": " + e.getMessage());
        }

        return themes;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseTheme(String json, String type) {
        try {
            switch (type) {
                case "robot":
                    ThemeData.RobotTheme robot = new ThemeData.RobotTheme();
                    robot.name = extractJsonValue(json, "name");
                    robot.bodyColor = extractJsonValue(json, "bodyColor");
                    robot.borderColor = extractJsonValue(json, "borderColor");
                    robot.eyeColor = extractJsonValue(json, "eyeColor");
                    robot.eyeBorderColor = extractJsonValue(json, "eyeBorderColor");

                    String bodyWidth = extractJsonValue(json, "bodyWidth");
                    String bodyHeight = extractJsonValue(json, "bodyHeight");
                    String eyeSize = extractJsonValue(json, "eyeSize");
                    String eyeOffsetX = extractJsonValue(json, "eyeOffsetX");

                    if (bodyWidth != null) {
                        robot.bodyWidth = Integer.parseInt(bodyWidth);
                    } else {
                        robot.bodyWidth = 30;
                    }

                    if (bodyHeight != null) {
                        robot.bodyHeight = Integer.parseInt(bodyHeight);
                    } else {
                        robot.bodyHeight = 10;
                    }

                    if (eyeSize != null) {
                        robot.eyeSize = Integer.parseInt(eyeSize);
                    } else {
                        robot.eyeSize = 5;
                    }

                    if (eyeOffsetX != null) {
                        robot.eyeOffsetX = Integer.parseInt(eyeOffsetX);
                    } else {
                        robot.eyeOffsetX = 10;
                    }

                    return (T) robot;

                case "target":
                    ThemeData.TargetTheme target = new ThemeData.TargetTheme();
                    target.name = extractJsonValue(json, "name");
                    target.color = extractJsonValue(json, "color");
                    target.shape = extractJsonValue(json, "shape");

                    String size = extractJsonValue(json, "size");
                    target.size = size != null ? Integer.parseInt(size) : 8;

                    return (T) target;

                case "background":
                    ThemeData.BackgroundTheme bg = new ThemeData.BackgroundTheme();
                    bg.name = extractJsonValue(json, "name");
                    bg.backgroundColor = extractJsonValue(json, "backgroundColor");
                    bg.pathColor = extractJsonValue(json, "pathColor");
                    bg.draftPathColor = extractJsonValue(json, "draftPathColor");

                    return (T) bg;

                default:
                    Logger.debug("Неизвестный тип темы: " + type);
                    return null;
            }
        } catch (Exception e) {
            Logger.error("Ошибка парсинга темы типа " + type + ": " + e.getMessage());
            return null;
        }
    }

    private String readFileToString(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int startIndex = colonIndex + 1;
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        if (startIndex >= json.length()) return null;

        char firstChar = json.charAt(startIndex);

        if (firstChar == '"') {
            int endQuote = findMatchingQuote(json, startIndex + 1);
            if (endQuote == -1) return null;
            return json.substring(startIndex + 1, endQuote);
        } else {
            int endValue = startIndex;
            while (endValue < json.length()) {
                char c = json.charAt(endValue);
                if (c == ',' || c == '}' || Character.isWhitespace(c)) {
                    break;
                }
                endValue++;
            }
            return json.substring(startIndex, endValue);
        }
    }

    private int findMatchingQuote(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                if (i > 0 && json.charAt(i - 1) != '\\') {
                    return i;
                }
            }
        }
        return -1;
    }

    private ThemeData.RobotTheme createDefaultRobotTheme() {
        Logger.debug("Создаём стандартную тему робота");
        ThemeData.RobotTheme theme = new ThemeData.RobotTheme();
        theme.name = "Стандартный робот";
        theme.bodyColor = "#FF00FF";
        theme.borderColor = "#000000";
        theme.eyeColor = "#FFFFFF";
        theme.eyeBorderColor = "#000000";
        theme.bodyWidth = 30;
        theme.bodyHeight = 10;
        theme.eyeSize = 5;
        theme.eyeOffsetX = 10;
        return theme;
    }

    private ThemeData.TargetTheme createDefaultTargetTheme() {
        Logger.debug("Создаём стандартную тему цели");
        ThemeData.TargetTheme theme = new ThemeData.TargetTheme();
        theme.name = "Красная точка";
        theme.color = "#FF3232";
        theme.size = 8;
        theme.shape = "circle";
        return theme;
    }

    private ThemeData.BackgroundTheme createDefaultBackgroundTheme() {
        Logger.debug("Создаём стандартную тему фона");
        ThemeData.BackgroundTheme theme = new ThemeData.BackgroundTheme();
        theme.name = "Светлая";
        theme.backgroundColor = "#FFFFFF";
        theme.pathColor = "#6E64FF";
        theme.draftPathColor = "#6DFF3C";
        return theme;
    }

    private void loadSavedThemes() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            Logger.debug("Файл конфигурации тем не найден: " + CONFIG_FILE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String robotName = reader.readLine();
            String targetName = reader.readLine();
            String bgName = reader.readLine();

            if (robotName != null && !robotName.isEmpty()) {
                ThemeData.RobotTheme found = findRobotThemeByName(robotName);
                if (found != null) {
                    currentRobotTheme = found;
                    Logger.debug("Загружена тема робота: " + robotName);
                } else {
                    Logger.debug("Тема робота не найдена: " + robotName);
                }
            }
            if (targetName != null && !targetName.isEmpty()) {
                ThemeData.TargetTheme found = findTargetThemeByName(targetName);
                if (found != null) {
                    currentTargetTheme = found;
                    Logger.debug("Загружена тема цели: " + targetName);
                } else {
                    Logger.debug("Тема цели не найдена: " + targetName);
                }
            }
            if (bgName != null && !bgName.isEmpty()) {
                ThemeData.BackgroundTheme found = findBackgroundThemeByName(bgName);
                if (found != null) {
                    currentBackgroundTheme = found;
                    Logger.debug("Загружена тема фона: " + bgName);
                } else {
                    Logger.debug("Тема фона не найдена: " + bgName);
                }
            }
        } catch (IOException e) {
            Logger.error("Ошибка загрузки конфигурации тем: " + e.getMessage());
        }
    }

    public void saveCurrentThemes() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.println(currentRobotTheme.name);
            writer.println(currentTargetTheme.name);
            writer.println(currentBackgroundTheme.name);
            Logger.debug("Темы сохранены: робот=" + currentRobotTheme.name + ", цель=" + currentTargetTheme.name + ", фон=" + currentBackgroundTheme.name);
        } catch (IOException e) {
            Logger.error("Ошибка сохранения конфигурации тем: " + e.getMessage());
        }
    }

    private ThemeData.RobotTheme findRobotThemeByName(String name) {
        if (name == null) return null;
        for (ThemeData.RobotTheme theme : robotThemes) {
            if (theme.name != null && theme.name.equals(name)) return theme;
        }
        return null;
    }

    private ThemeData.TargetTheme findTargetThemeByName(String name) {
        if (name == null) return null;
        for (ThemeData.TargetTheme theme : targetThemes) {
            if (theme.name != null && theme.name.equals(name)) return theme;
        }
        return null;
    }

    private ThemeData.BackgroundTheme findBackgroundThemeByName(String name) {
        if (name == null) return null;
        for (ThemeData.BackgroundTheme theme : backgroundThemes) {
            if (theme.name != null && theme.name.equals(name)) return theme;
        }
        return null;
    }

    public List<ThemeData.RobotTheme> getRobotThemes() { return robotThemes; }
    public List<ThemeData.TargetTheme> getTargetThemes() { return targetThemes; }
    public List<ThemeData.BackgroundTheme> getBackgroundThemes() { return backgroundThemes; }
    public ThemeData.RobotTheme getCurrentRobotTheme() { return currentRobotTheme; }
    public ThemeData.TargetTheme getCurrentTargetTheme() { return currentTargetTheme; }
    public ThemeData.BackgroundTheme getCurrentBackgroundTheme() { return currentBackgroundTheme; }
    public void setCurrentRobotTheme(ThemeData.RobotTheme theme) { this.currentRobotTheme = theme; }
    public void setCurrentTargetTheme(ThemeData.TargetTheme theme) { this.currentTargetTheme = theme; }
    public void setCurrentBackgroundTheme(ThemeData.BackgroundTheme theme) { this.currentBackgroundTheme = theme; }
}