package model;

import java.util.Random;

public class ObstacleMap {

    private static final int CELL_SIZE      = 20;
    private static final double OBSTACLE_CHANCE = 0.12;

    private final boolean[][] grid;
    private final int cols;
    private final int rows;
    private final AStarPathfinder pathfinder;

    public ObstacleMap(int fieldWidth, int fieldHeight) {
        this.cols = fieldWidth  / CELL_SIZE;
        this.rows = fieldHeight / CELL_SIZE;
        this.grid = new boolean[cols][rows];

        generateObstacles(fieldWidth, fieldHeight);

        this.pathfinder = new AStarPathfinder(fieldWidth, fieldHeight, CELL_SIZE, grid);
    }

    private void generateObstacles(int fieldWidth, int fieldHeight) {
        Random rnd = new Random();

        int safeCol = 100 / CELL_SIZE;
        int safeRow = 100 / CELL_SIZE;

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (Math.abs(c - safeCol) <= 2 && Math.abs(r - safeRow) <= 2) continue;

                grid[c][r] = rnd.nextDouble() < OBSTACLE_CHANCE;
            }
        }

        removeIsolatedObstacles();
    }

    private void removeIsolatedObstacles() {
        for (int c = 1; c < cols - 1; c++) {
            for (int r = 1; r < rows - 1; r++) {
                if (!grid[c][r]) continue;

                boolean allNeighborsBlocked =
                        grid[c-1][r] && grid[c+1][r] &&
                                grid[c][r-1] && grid[c][r+1];

                if (allNeighborsBlocked) {
                    grid[c][r] = false;
                }
            }
        }
    }

    public boolean isObstacle(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return false;
        return grid[col][row];
    }

    public boolean isObstacleAt(double pixelX, double pixelY) {
        int col = (int) (pixelX / CELL_SIZE);
        int row = (int) (pixelY / CELL_SIZE);
        return isObstacle(col, row);
    }

    public AStarPathfinder getPathfinder() { return pathfinder; }
    public boolean[][] getGrid()           { return grid; }
    public int getCols()                   { return cols; }
    public int getRows()                   { return rows; }
    public int getCellSize()               { return CELL_SIZE; }
}