package model;

import java.util.*;

public class AStarPathfinder {

    private static class Node implements Comparable<Node> {
        final int x, y;
        double g;
        double h;
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        double f() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f(), other.f());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node n = (Node) o;
            return x == n.x && y == n.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private final int cols;
    private final int rows;
    private final int cellSize;
    private final boolean[][] obstacles;

    private static final int[][] DIRS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    public AStarPathfinder(int fieldWidth, int fieldHeight, int cellSize, boolean[][] obstacles) {
        this.cellSize = cellSize;
        this.cols = fieldWidth  / cellSize;
        this.rows = fieldHeight / cellSize;
        this.obstacles = obstacles;
    }

    public List<double[]> findPath(double startX, double startY, double goalX, double goalY) {
        int sc = (int) (startX / cellSize);
        int sr = (int) (startY / cellSize);
        int gc = (int) (goalX  / cellSize);
        int gr = (int) (goalY  / cellSize);
        if (isBlocked(gc, gr)) {
            int[] free = findNearestFree(gc, gr);
            if (free == null) return Collections.emptyList();
            gc = free[0];
            gr = free[1];
        }
        if (isBlocked(sc, sr)) {
            int[] free = findNearestFree(sc, sr);
            if (free == null) return Collections.emptyList();
            sc = free[0];
            sr = free[1];
        }

        Node start = new Node(sc, sr);
        Node goal  = new Node(gc, gr);

        start.g = 0;
        start.h = heuristic(start, goal);

        PriorityQueue<Node> open   = new PriorityQueue<>();
        Map<Node, Node>     cameFrom = new HashMap<>();
        Map<String, Double> gScore   = new HashMap<>();

        open.add(start);
        gScore.put(key(sc, sr), 0.0);

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.x == goal.x && current.y == goal.y) {
                return reconstructPath(current, goalX, goalY);
            }

            for (int[] dir : DIRS) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (!inBounds(nx, ny) || isBlocked(nx, ny)) continue;

                double stepCost = (dir[0] != 0 && dir[1] != 0) ? Math.sqrt(2) : 1.0;
                double tentativeG = current.g + stepCost;

                String nKey = key(nx, ny);
                if (tentativeG < gScore.getOrDefault(nKey, Double.MAX_VALUE)) {
                    gScore.put(nKey, tentativeG);

                    Node neighbor = new Node(nx, ny);
                    neighbor.g = tentativeG;
                    neighbor.h = heuristic(neighbor, goal);
                    neighbor.parent = current;

                    open.add(neighbor);
                }
            }
        }

        List<double[]> fallback = new ArrayList<>();
        fallback.add(new double[]{goalX, goalY});
        return fallback;
    }

    private List<double[]> reconstructPath(Node end, double goalX, double goalY) {
        List<double[]> path = new ArrayList<>();
        Node current = end;

        while (current != null) {
            double px = current.x * cellSize + cellSize / 2.0;
            double py = current.y * cellSize + cellSize / 2.0;
            path.add(new double[]{px, py});
            current = current.parent;
        }

        Collections.reverse(path);

        if (!path.isEmpty()) {
            path.set(path.size() - 1, new double[]{goalX, goalY});
        }

        return path;
    }

    private double heuristic(Node a, Node b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean inBounds(int c, int r) {
        return c >= 0 && c < cols && r >= 0 && r < rows;
    }

    private boolean isBlocked(int c, int r) {
        if (!inBounds(c, r)) return true;
        return obstacles[c][r];
    }

    private String key(int c, int r) {
        return c + "," + r;
    }

    private int[] findNearestFree(int c, int r) {
        Queue<int[]> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(new int[]{c, r});
        visited.add(key(c, r));

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            if (!isBlocked(cur[0], cur[1])) return cur;

            for (int[] dir : DIRS) {
                int nx = cur[0] + dir[0];
                int ny = cur[1] + dir[1];
                String k = key(nx, ny);
                if (inBounds(nx, ny) && !visited.contains(k)) {
                    visited.add(k);
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        return null;
    }

    public int getCols()   { return cols; }
    public int getRows()   { return rows; }
    public int getCellSize() { return cellSize; }
}