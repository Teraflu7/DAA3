package kz.edu.daa.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import kz.edu.daa.metrics.MetricsSnapshot;

/** Distances and parents produced by one topological relaxation pass. */
public record ShortestPathResult(long[] distances, int[] parents, MetricsSnapshot metrics) {
    public static final long INF = Long.MAX_VALUE / 4;

    public ShortestPathResult {
        Objects.requireNonNull(distances, "distances");
        Objects.requireNonNull(parents, "parents");
        Objects.requireNonNull(metrics, "metrics");
        if (distances.length != parents.length) {
            throw new IllegalArgumentException("Distances and parents must have equal length");
        }
        distances = distances.clone();
        parents = parents.clone();
    }

    @Override
    public long[] distances() {
        return distances.clone();
    }

    @Override
    public int[] parents() {
        return parents.clone();
    }

    public long distanceTo(int vertex) {
        checkVertex(vertex);
        return distances[vertex];
    }

    public int parentOf(int vertex) {
        checkVertex(vertex);
        return parents[vertex];
    }

    public boolean isReachable(int vertex) {
        return distanceTo(vertex) != INF;
    }

    public List<Integer> pathTo(int vertex) {
        checkVertex(vertex);
        if (!isReachable(vertex)) {
            return List.of();
        }

        List<Integer> reversedPath = new ArrayList<>();
        int current = vertex;
        while (current >= 0) {
            if (reversedPath.size() > parents.length) {
                throw new IllegalStateException("Parent array contains a cycle");
            }
            reversedPath.add(current);
            current = parents[current];
        }
        Collections.reverse(reversedPath);
        return List.copyOf(reversedPath);
    }

    public long distanceSum() {
        long sum = 0;
        for (long distance : distances) {
            if (distance != INF) {
                sum += distance;
            }
        }
        return sum;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= distances.length) {
            throw new IndexOutOfBoundsException("Invalid vertex: " + vertex);
        }
    }

    @Override
    public String toString() {
        return "ShortestPathResult[distances=" + Arrays.toString(distances)
                + ", parents=" + Arrays.toString(parents) + "]";
    }
}
