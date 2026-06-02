package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ThemeManagerTest {

    private ThemeManager themeManager;

    @BeforeEach
    void setUp() {
        themeManager = new ThemeManager();
    }

    @Test
    @DisplayName("Список тем роботов непустой после инициализации")
    void testRobotThemesNotEmpty() {
        assertFalse(themeManager.getRobotThemes().isEmpty());
    }

    @Test
    @DisplayName("Список тем целей непустой после инициализации")
    void testTargetThemesNotEmpty() {
        assertFalse(themeManager.getTargetThemes().isEmpty());
    }

    @Test
    @DisplayName("Список тем фонов непустой после инициализации")
    void testBackgroundThemesNotEmpty() {
        assertFalse(themeManager.getBackgroundThemes().isEmpty());
    }

    @Test
    @DisplayName("Текущая тема робота не null после инициализации")
    void testCurrentRobotThemeNotNull() {
        assertNotNull(themeManager.getCurrentRobotTheme());
    }

    @Test
    @DisplayName("Текущая тема цели не null после инициализации")
    void testCurrentTargetThemeNotNull() {
        assertNotNull(themeManager.getCurrentTargetTheme());
    }

    @Test
    @DisplayName("Текущая тема фона не null после инициализации")
    void testCurrentBackgroundThemeNotNull() {
        assertNotNull(themeManager.getCurrentBackgroundTheme());
    }

    @Test
    @DisplayName("setCurrentRobotTheme меняет активную тему робота")
    void testSetCurrentRobotTheme() {
        ThemeData.RobotTheme theme = themeManager.getRobotThemes().get(0);
        themeManager.setCurrentRobotTheme(theme);
        assertEquals(theme, themeManager.getCurrentRobotTheme());
    }

    @Test
    @DisplayName("setCurrentTargetTheme меняет активную тему цели")
    void testSetCurrentTargetTheme() {
        ThemeData.TargetTheme theme = themeManager.getTargetThemes().get(0);
        themeManager.setCurrentTargetTheme(theme);
        assertEquals(theme, themeManager.getCurrentTargetTheme());
    }

    @Test
    @DisplayName("setCurrentBackgroundTheme меняет активную тему фона")
    void testSetCurrentBackgroundTheme() {
        ThemeData.BackgroundTheme theme = themeManager.getBackgroundThemes().get(0);
        themeManager.setCurrentBackgroundTheme(theme);
        assertEquals(theme, themeManager.getCurrentBackgroundTheme());
    }

    @Test
    @DisplayName("У каждой темы робота есть непустое имя")
    void testAllRobotThemesHaveNames() {
        for (ThemeData.RobotTheme t : themeManager.getRobotThemes()) {
            assertNotNull(t.name, "Имя темы не должно быть null");
            assertFalse(t.name.isEmpty(), "Имя темы не должно быть пустым");
        }
    }

    @Test
    @DisplayName("У каждой темы робота корректный цвет тела — парсится без исключений")
    void testRobotThemeBodyColorParseable() {
        for (ThemeData.RobotTheme t : themeManager.getRobotThemes()) {
            assertDoesNotThrow(t::getBodyColor,
                    "Цвет тела темы '" + t.name + "' должен парситься корректно");
        }
    }

    @Test
    @DisplayName("У каждой темы фона корректный цвет — парсится без исключений")
    void testBackgroundThemeColorParseable() {
        for (ThemeData.BackgroundTheme t : themeManager.getBackgroundThemes()) {
            assertDoesNotThrow(t::getBackgroundColor,
                    "Цвет фона темы '" + t.name + "' должен парситься корректно");
        }
    }

    @Test
    @DisplayName("Размеры тела робота положительные")
    void testRobotThemeDimensionsPositive() {
        for (ThemeData.RobotTheme t : themeManager.getRobotThemes()) {
            assertTrue(t.bodyWidth > 0,  "bodyWidth темы '" + t.name + "' должен быть > 0");
            assertTrue(t.bodyHeight > 0, "bodyHeight темы '" + t.name + "' должен быть > 0");
            assertTrue(t.eyeSize > 0,    "eyeSize темы '" + t.name + "' должен быть > 0");
        }
    }

    @Test
    @DisplayName("Форма цели — одно из допустимых значений")
    void testTargetThemeShapeIsValid() {
        for (ThemeData.TargetTheme t : themeManager.getTargetThemes()) {
            assertTrue(
                    t.shape.equals("circle") || t.shape.equals("square") || t.shape.equals("cross"),
                    "Форма цели '" + t.shape + "' не входит в допустимые: circle, square, cross"
            );
        }
    }
}