package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RobotModelTest {

    private RobotModel robot;

    @BeforeEach
    void setUp() {
        robot = new RobotModel();
    }

    @Test
    @DisplayName("Робот стартует в позиции (100, 100)")
    void testInitialPosition() {
        assertEquals(100.0, robot.getX(), 0.001);
        assertEquals(100.0, robot.getY(), 0.001);
    }

    @Test
    @DisplayName("Начальное направление робота — 0 радиан")
    void testInitialDirection() {
        assertEquals(0.0, robot.getDirection(), 0.001);
    }

    @Test
    @DisplayName("Начальный путь пустой, цель — null")
    void testInitialPathIsEmpty() {
        assertTrue(robot.getPath().isEmpty());
        assertNull(robot.getCurrentTarget());
    }

    @Test
    @DisplayName("setPath сохраняет точки и первая становится текущей целью")
    void testSetPathStoresWaypoints() {
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{200.0, 300.0});
        path.add(new double[]{400.0, 500.0});

        robot.setPath(path);

        assertEquals(2, robot.getPath().size());
        double[] target = robot.getCurrentTarget();
        assertNotNull(target);
        assertEquals(200.0, target[0], 0.001);
        assertEquals(300.0, target[1], 0.001);
    }

    @Test
    @DisplayName("setPath с пустым списком очищает путь и цель")
    void testSetEmptyPathClearsTarget() {
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{200.0, 200.0});
        robot.setPath(path);

        robot.setPath(new ArrayList<>());

        assertTrue(robot.getPath().isEmpty());
        assertNull(robot.getCurrentTarget());
    }

    @Test
    @DisplayName("getPath возвращает копию — изменение снаружи не влияет на модель")
    void testGetPathReturnsCopy() {
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{200.0, 200.0});
        robot.setPath(path);

        List<double[]> returned = robot.getPath();
        returned.clear(); // пытаемся сломать внутреннее состояние

        assertEquals(1, robot.getPath().size()); // модель не изменилась
    }

    @Test
    @DisplayName("После одного шага робот смещается в сторону цели")
    void testRobotMovesTowardTarget() {
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{500.0, 100.0}); // цель строго вправо
        robot.setPath(path);

        double startX = robot.getX();
        robot.updateModel(100); // один шаг 100 мс

        assertTrue(robot.getX() > startX, "Робот должен двигаться вправо к цели");
    }

    @Test
    @DisplayName("Робот достигает одиночной цели за разумное время")
    void testRobotReachesSingleTarget() {
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{110.0, 100.0}); // близкая цель — 10 пикселей вправо
        robot.setPath(path);

        for (int i = 0; i < 200; i++) {
            robot.updateModel(10);
        }

        assertNull(robot.getCurrentTarget(), "Цель должна быть достигнута");
    }

    @Test
    @DisplayName("Робот проходит цепочку из двух точек")
    void testRobotFollowsMultiPointPath() {
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{110.0, 100.0});
        path.add(new double[]{120.0, 100.0});
        robot.setPath(path);

        for (int i = 0; i < 500; i++) {
            robot.updateModel(10);
        }

        assertNull(robot.getCurrentTarget(), "Все точки пути должны быть пройдены");
    }

    @Test
    @DisplayName("Повторный вызов setPath сбрасывает прогресс по пути")
    void testSetPathResetsProgress() {
        List<double[]> path1 = new ArrayList<>();
        path1.add(new double[]{500.0, 100.0});
        robot.setPath(path1);
        for (int i = 0; i < 50; i++) robot.updateModel(10);

        List<double[]> path2 = new ArrayList<>();
        path2.add(new double[]{200.0, 200.0});
        robot.setPath(path2);

        double[] target = robot.getCurrentTarget();
        assertNotNull(target);
        assertEquals(200.0, target[0], 0.001);
        assertEquals(200.0, target[1], 0.001);
    }

    @Test
    @DisplayName("Слушатель получает уведомление при вызове setPath")
    void testListenerNotifiedOnSetPath() {
        boolean[] notified = {false};
        robot.addListener(m -> notified[0] = true);

        robot.setPath(new ArrayList<>());

        assertTrue(notified[0], "Слушатель должен получить уведомление");
    }

    @Test
    @DisplayName("Слушатель получает уведомление при updateModel")
    void testListenerNotifiedOnUpdate() {
        int[] callCount = {0};
        robot.addListener(m -> callCount[0]++);

        robot.updateModel(10);
        robot.updateModel(10);

        assertTrue(callCount[0] >= 2, "Слушатель должен получить минимум 2 уведомления");
    }

    @Test
    @DisplayName("После removeListener уведомления прекращаются")
    void testListenerRemovedStopsNotifications() {
        int[] callCount = {0};
        RobotModelListener listener = m -> callCount[0]++;
        robot.addListener(listener);

        robot.updateModel(10);
        int countBefore = callCount[0];

        robot.removeListener(listener);
        robot.updateModel(10);

        assertEquals(countBefore, callCount[0], "После удаления слушатель не должен получать события");
    }
}