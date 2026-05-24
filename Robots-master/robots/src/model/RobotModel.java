package model;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import log.Logger;

public class RobotModel {
    private double x = 100;
    private double y = 100;
    private double direction = 0;

    private final List<double[]> path = new ArrayList<>();
    private int currentPathIndex = 0;

    private final List<RobotModelListener> listeners = new CopyOnWriteArrayList<>();
    private static final double MAX_VELOCITY = 300.0;
    private static final double MAX_ANGULAR_VELOCITY = 15.0;
    private static final double WAYPOINT_REACH_DISTANCE = 5.0;

    public RobotModel() {
        Logger.logFunction("RobotModel constructor");
    }

    public void addListener(RobotModelListener listener) {
        listeners.add(listener);
        Logger.logFunction("RobotModel.addListener");
    }

    public void removeListener(RobotModelListener listener) {
        listeners.remove(listener);
        Logger.logFunction("RobotModel.removeListener");
    }

    private void notifyListeners() {
        Logger.logFunction("RobotModel.notifyListeners");
        for (RobotModelListener listener : listeners) {
            listener.onModelChanged(this);
        }
    }

    public double getX() { Logger.logFunction("RobotModel.getX"); return x; }
    public double getY() { Logger.logFunction("RobotModel.getY"); return y; }
    public double getDirection() { Logger.logFunction("RobotModel.getDirection"); return direction; }

    public void setPath(List<double[]> newPath)
    {
        Logger.logFunction("RobotModel.setPath");
        synchronized (path) {
            path.clear();
            path.addAll(newPath);
            currentPathIndex = 0;
        }
        notifyListeners();
    }

    public List<double[]> getPath()
    {
        Logger.logFunction("RobotModel.getPath");
        synchronized (path) {
            return new ArrayList<>(path);
        }
    }

    public double[] getCurrentTarget() {
        Logger.logFunction("RobotModel.getCurrentTarget");
        synchronized (path) {
            if (path.isEmpty() || currentPathIndex >= path.size()) {
                return null;
            }
            return path.get(currentPathIndex);
        }
    }

    public void updateModel(int durationMs) {
        Logger.logFunction("RobotModel.updateModel");
        double[] target = getCurrentTarget();
        if (target == null) {
            notifyListeners();
            return;
        }

        double distance = distance(target[0], target[1], x, y);

        if (distance < WAYPOINT_REACH_DISTANCE) {
            synchronized (path) {
                currentPathIndex++;
            }
            notifyListeners();
            return;
        }

        double velocity = Math.min(MAX_VELOCITY, distance * 5);
        double angleToTarget = angleTo(x, y, target[0], target[1]);

        double angleDifference = asNormalizedRadians(angleToTarget - direction);
        if (angleDifference > Math.PI) {
            angleDifference = angleDifference - 2 * Math.PI;
        }

        double angularVelocity = 0;
        if (angleDifference > 0) {
            angularVelocity = MAX_ANGULAR_VELOCITY;
        } else if (angleDifference < 0) {
            angularVelocity = -MAX_ANGULAR_VELOCITY;
        }

        moveRobot(velocity, angularVelocity, durationMs / 1000.0);
        notifyListeners();
    }

    private void moveRobot(double velocity, double angularVelocity, double duration)
    {
        Logger.logFunction("RobotModel.moveRobot");
        velocity = applyLimits(velocity, 0, MAX_VELOCITY);
        angularVelocity = applyLimits(angularVelocity, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);

        double newX = x + velocity / angularVelocity *
                (Math.sin(direction + angularVelocity * duration) - Math.sin(direction));
        if (!Double.isFinite(newX))
        {
            newX = x + velocity * duration * Math.cos(direction);
        }
        double newY = y - velocity / angularVelocity *
                (Math.cos(direction + angularVelocity * duration) - Math.cos(direction));
        if (!Double.isFinite(newY))
        {
            newY = y + velocity * duration * Math.sin(direction);
        }

        x = newX;
        y = newY;
        direction = asNormalizedRadians(direction + angularVelocity * duration);
    }

    private static double asNormalizedRadians(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double angle = Math.atan2(dy, dx);
        return asNormalizedRadians(angle);
    }
}