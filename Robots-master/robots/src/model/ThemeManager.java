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
        Logger.logFunction("ThemeManager constructor");
        loadAllThemes();
        loadSavedThemes();
    }

    private void loadAllThemes() {
        Logger.logFunction("ThemeManager.loadAllThemes");
        robotThemes = loadThemesFromClasspath("/themes/robots", "robot");
        targetThemes = loadThemesFromClasspath("/themes/targets", "target");
        backgroundThemes = loadThemesFromClasspath("/themes/backgrounds", "background");

        if (robotThemes.isEmpty()) robotThemes.add(createDefaultRobotTheme());
        if (targetThemes.isEmpty()) targetThemes.add(createDefaultTargetTheme());
        if (backgroundThemes.isEmpty()) backgroundThemes.add(createDefaultBackgroundTheme());

        currentRobotTheme = robotThemes.get(0);
        currentTargetTheme = targetThemes.get(0);
        currentBackgroundTheme = backgroundThemes.get(0);
    }

    private <T> List<T> loadThemesFromClasspath(String resourcePath, String type) {
        Logger.logFunction("ThemeManager.loadThemesFromClasspath");
        List<T> themes = new ArrayList<>();
        try {
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                Logger.debug("Ресурс не найден: " + resourcePath);
                return themes;
            }

            Path dirPath = Paths.get(resourceUrl.toURI());

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.json")) {
                for (Path entry : stream) {
                    String json = new String(Files.readAllBytes(entry));
                    T theme = parseTheme(json, type);
                    if (theme != null) {
                        themes.add(theme);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Ошибка при чтении папки " + resourcePath + ": " + e.getMessage());
        }
        return themes;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseTheme(String json, String type) {
        Logger.logFunction("ThemeManager.parseTheme");
        try {
            switch (type) {
                case "robot":
                    ThemeData.RobotTheme robot = new ThemeData.RobotTheme();
                    robot.name = extractJsonValue(json, "name");
                    robot.bodyColor = extractJsonValue(json, "bodyColor");
                    robot.borderColor = extractJsonValue(json, "borderColor");
                    robot.eyeColor = extractJsonValue(json, "eyeColor");
                    robot.eyeBorderColor = extractJsonValue(json, "eyeBorderColor");
                    robot.bodyWidth = parseOptionalInt(extractJsonValue(json, "bodyWidth"), 30);
                    robot.bodyHeight = parseOptionalInt(extractJsonValue(json, "bodyHeight"), 10);
                    robot.eyeSize = parseOptionalInt(extractJsonValue(json, "eyeSize"), 5);
                    robot.eyeOffsetX = parseOptionalInt(extractJsonValue(json, "eyeOffsetX"), 10);
                    return (T) robot;

                case "target":
                    ThemeData.TargetTheme target = new ThemeData.TargetTheme();
                    target.name = extractJsonValue(json, "name");
                    target.color = extractJsonValue(json, "color");
                    target.shape = extractJsonValue(json, "shape");
                    target.size = parseOptionalInt(extractJsonValue(json, "size"), 8);
                    return (T) target;

                case "background":
                    ThemeData.BackgroundTheme bg = new ThemeData.BackgroundTheme();
                    bg.name = extractJsonValue(json, "name");
                    bg.backgroundColor = extractJsonValue(json, "backgroundColor");
                    bg.pathColor = extractJsonValue(json, "pathColor");
                    bg.draftPathColor = extractJsonValue(json, "draftPathColor");
                    return (T) bg;

                default: return null;
            }
        } catch (Exception e) {
            Logger.error("Ошибка парсинга JSON темы (" + type + "): " + e.getMessage());
            return null;
        }
    }

    private int parseOptionalInt(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int startIndex = colonIndex + 1;
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) startIndex++;

        if (json.charAt(startIndex) == '"') {
            int endQuote = json.indexOf("\"", startIndex + 1);
            return json.substring(startIndex + 1, endQuote);
        } else {
            int endValue = startIndex;
            while (endValue < json.length() && !String.valueOf(json.charAt(endValue)).matches("[,\\}\\s]")) endValue++;
            return json.substring(startIndex, endValue);
        }
    }

    private void loadSavedThemes() {
        Logger.logFunction("ThemeManager.loadSavedThemes");
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String robotName = reader.readLine();
            String targetName = reader.readLine();
            String bgName = reader.readLine();

            currentRobotTheme = findByName(robotThemes, robotName, currentRobotTheme);
            currentTargetTheme = findByName(targetThemes, targetName, currentTargetTheme);
            currentBackgroundTheme = findByName(backgroundThemes, bgName, currentBackgroundTheme);
        } catch (IOException e) {
            Logger.error("Ошибка загрузки конфигурации: " + e.getMessage());
        }
    }

    private <T> T findByName(List<T> list, String name, T defaultTheme) {
        if (name == null) return defaultTheme;
        for (T item : list) {
            try {
                if (item.getClass().getField("name").get(item).equals(name)) return item;
            } catch (Exception ignored) {}
        }
        return defaultTheme;
    }

    public void saveCurrentThemes() {
        Logger.logFunction("ThemeManager.saveCurrentThemes");
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.println(currentRobotTheme.name);
            writer.println(currentTargetTheme.name);
            writer.println(currentBackgroundTheme.name);
        } catch (IOException e) {
            Logger.error("Ошибка сохранения конфигурации: " + e.getMessage());
        }
    }

    private ThemeData.RobotTheme createDefaultRobotTheme() {
        Logger.logFunction("ThemeManager.createDefaultRobotTheme");
        ThemeData.RobotTheme t = new ThemeData.RobotTheme();
        t.name = "Стандартный робот"; t.bodyColor = "#FF00FF"; t.borderColor = "#000000";
        t.eyeColor = "#FFFFFF"; t.eyeBorderColor = "#000000"; t.bodyWidth = 30;
        t.bodyHeight = 10; t.eyeSize = 5; t.eyeOffsetX = 10;
        return t;
    }

    private ThemeData.TargetTheme createDefaultTargetTheme() {
        Logger.logFunction("ThemeManager.createDefaultTargetTheme");
        ThemeData.TargetTheme t = new ThemeData.TargetTheme();
        t.name = "Красная точка"; t.color = "#FF3232"; t.size = 8; t.shape = "circle";
        return t;
    }

    private ThemeData.BackgroundTheme createDefaultBackgroundTheme() {
        Logger.logFunction("ThemeManager.createDefaultBackgroundTheme");
        ThemeData.BackgroundTheme t = new ThemeData.BackgroundTheme();
        t.name = "Светлая"; t.backgroundColor = "#FFFFFF"; t.pathColor = "#6E64FF"; t.draftPathColor = "#6DFF3C";
        return t;
    }

    public List<ThemeData.RobotTheme> getRobotThemes() { Logger.logFunction("ThemeManager.getRobotThemes"); return robotThemes; }
    public List<ThemeData.TargetTheme> getTargetThemes() { Logger.logFunction("ThemeManager.getTargetThemes"); return targetThemes; }
    public List<ThemeData.BackgroundTheme> getBackgroundThemes() { Logger.logFunction("ThemeManager.getBackgroundThemes"); return backgroundThemes; }
    public ThemeData.RobotTheme getCurrentRobotTheme() { Logger.logFunction("ThemeManager.getCurrentRobotTheme"); return currentRobotTheme; }
    public ThemeData.TargetTheme getCurrentTargetTheme() { Logger.logFunction("ThemeManager.getCurrentTargetTheme"); return currentTargetTheme; }
    public ThemeData.BackgroundTheme getCurrentBackgroundTheme() { Logger.logFunction("ThemeManager.getCurrentBackgroundTheme"); return currentBackgroundTheme; }
    public void setCurrentRobotTheme(ThemeData.RobotTheme theme) { Logger.logFunction("ThemeManager.setCurrentRobotTheme"); this.currentRobotTheme = theme; }
    public void setCurrentTargetTheme(ThemeData.TargetTheme theme) { Logger.logFunction("ThemeManager.setCurrentTargetTheme"); this.currentTargetTheme = theme; }
    public void setCurrentBackgroundTheme(ThemeData.BackgroundTheme theme) { Logger.logFunction("ThemeManager.setCurrentBackgroundTheme"); this.currentBackgroundTheme = theme; }
}