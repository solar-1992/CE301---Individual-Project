package Project.Game;

import java.util.HashMap;

/**
 * Caches Vector2D's
 */
public class CachedVector2DSource {
    private HashMap<Integer, HashMap<Integer, Vector2D>> vectors;

    public CachedVector2DSource() {
        vectors = new HashMap<>(Main.MAP_WIDTH);
    }

    public Vector2D getVector(int x, int y) {
        if (!vectors.containsKey(x)) vectors.put(x, new HashMap<Integer, Vector2D>(Main.MAP_HEIGHT));
        if (!vectors.get(x).containsKey(y)) vectors.get(x).put(y, new Vector2D(x, y, false));
        return vectors.get(x).get(y);
    }

    public Vector2D getVector(double x, double y) {
        return getVector((int) x, (int) y);
    }

    public int numberOfVectors() {
        int sum = 0;
        for (HashMap<Integer, Vector2D> temp : vectors.values()) {
            sum += temp.size();
        }
        return sum;
    }
}
