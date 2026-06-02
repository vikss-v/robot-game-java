package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class DefenseModelTest {

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    private DefenseModel model;

    @BeforeEach
    void setUp() {
        model = new DefenseModel(WIDTH, HEIGHT);
    }

    @Test
    @DisplayName("Игра стартует в фазе WAITING")
    void testInitialPhaseIsWaiting() {
        assertEquals(DefenseModel.GamePhase.WAITING, model.getPhase());
    }

    @Test
    @DisplayName("Яблоко начинает с полными HP")
    void testInitialAppleHpIsFull() {
        assertEquals(DefenseModel.APPLE_MAX_HP, model.getAppleHp());
    }

    @Test
    @DisplayName("Яблоко расположено в центре поля")
    void testAppleIsAtCenter() {
        assertEquals(WIDTH / 2,  model.getApple().x);
        assertEquals(HEIGHT / 2, model.getApple().y);
    }

    @Test
    @DisplayName("Размер поля соответствует переданным параметрам")
    void testFieldSize() {
        assertEquals(WIDTH,  model.getFieldWidth());
        assertEquals(HEIGHT, model.getFieldHeight());
    }

    @Test
    @DisplayName("В начале нет червей")
    void testNoWormsInitially() {
        assertTrue(model.getWorms().isEmpty());
    }

    @Test
    @DisplayName("startGame переводит игру в фазу WAVE_ACTIVE")
    void testStartGameTransitionsToWaveActive() {
        model.startGame();
        assertEquals(DefenseModel.GamePhase.WAVE_ACTIVE, model.getPhase());
    }

    @Test
    @DisplayName("Повторный вызов startGame не меняет фазу")
    void testStartGameCalledTwiceIgnored() {
        model.startGame();
        model.startGame(); // второй вызов должен игнорироваться
        assertEquals(DefenseModel.GamePhase.WAVE_ACTIVE, model.getPhase());
    }

    @Test
    @DisplayName("tick в фазе WAITING ничего не меняет")
    void testTickInWaitingPhaseDoesNothing() {
        model.tick(0.1);
        assertEquals(DefenseModel.GamePhase.WAITING, model.getPhase());
        assertTrue(model.getWorms().isEmpty());
    }

    @Test
    @DisplayName("Роботы существуют и доступны")
    void testRobotsExist() {
        assertNotNull(model.getRobot1());
        assertNotNull(model.getRobot2());
    }

    @Test
    @DisplayName("setRobot1Direction задаёт вектор скорости")
    void testSetRobot1Direction() {
        model.startGame();
        model.setRobot1Direction(1.0, 0.0);
        double xBefore = model.getRobot1().x;

        model.tick(0.5);

        assertTrue(model.getRobot1().x > xBefore, "Робот 1 должен двигаться вправо");
    }

    @Test
    @DisplayName("setRobot2Direction задаёт вектор скорости")
    void testSetRobot2Direction() {
        model.startGame();
        model.setRobot2Direction(-1.0, 0.0);
        double xBefore = model.getRobot2().x;

        model.tick(0.5);

        assertTrue(model.getRobot2().x < xBefore, "Робот 2 должен двигаться влево");
    }

    @Test
    @DisplayName("Робот не выходит за границы поля")
    void testRobotStaysInBounds() {
        model.startGame();
        model.setRobot1Direction(1.0, 0.0);

        for (int i = 0; i < 200; i++) {
            model.tick(0.1);
        }

        DefenseModel.Robot r = model.getRobot1();
        assertTrue(r.x >= r.radius && r.x <= WIDTH  - r.radius, "X должен быть в границах");
        assertTrue(r.y >= r.radius && r.y <= HEIGHT - r.radius, "Y должен быть в границах");
    }

    @Test
    @DisplayName("Текущая волна — 0 в начале")
    void testInitialWaveIsZero() {
        assertEquals(0, model.getCurrentWave());
    }

    @Test
    @DisplayName("HP яблока не превышает максимум")
    void testAppleHpNotExceedsMax() {
        assertTrue(model.getAppleHp() <= DefenseModel.APPLE_MAX_HP);
    }

    @Test
    @DisplayName("remainingPauseMs возвращает 0 вне фазы BETWEEN_WAVES")
    void testRemainingPauseMsOutsidePause() {
        assertEquals(0, model.getRemainingPauseMs());
        model.startGame();
        assertEquals(0, model.getRemainingPauseMs());
    }

    @Test
    @DisplayName("Слушатель получает уведомление при startGame")
    void testListenerNotifiedOnStartGame() {
        boolean[] notified = {false};
        model.addListener(m -> notified[0] = true);

        model.startGame();

        assertTrue(notified[0]);
    }

    @Test
    @DisplayName("Слушатель получает уведомления при tick")
    void testListenerNotifiedOnTick() {
        model.startGame();
        int[] count = {0};
        model.addListener(m -> count[0]++);

        model.tick(0.1);
        model.tick(0.1);

        assertTrue(count[0] >= 2);
    }

    @Test
    @DisplayName("После removeListener уведомления прекращаются")
    void testListenerRemovedStopsNotifications() {
        model.startGame();
        int[] count = {0};
        DefenseModelListener listener = m -> count[0]++;
        model.addListener(listener);

        model.tick(0.1);
        int before = count[0];

        model.removeListener(listener);
        model.tick(0.1);

        assertEquals(before, count[0]);
    }
}