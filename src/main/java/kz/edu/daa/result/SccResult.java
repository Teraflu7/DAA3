package kz.edu.daa.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import kz.edu.daa.metrics.MetricsSnapshot;

/** Strongly connected components returned by an SCC algorithm. */
public record SccResult(int componentCount, int[] componentOf, MetricsSnapshot metrics) {
    public SccResult {
        Objects.requireNonNull(componentOf, "componentOf");
        Objects.requireNonNull(metrics, "metrics");
        if (componentCount < 0) {
            throw new IllegalArgumentException("Component count must be non-negative");
        }
        componentOf = componentOf.clone();
        for (int component : componentOf) {
            if (component < 0 || component >= componentCount) {
                throw new IllegalArgumentException("Invalid component id: " + component);
            }
        }
    }

    @Override
    public int[] componentOf() {
        return componentOf.clone();
    }

    public int componentOf(int vertex) {
        if (vertex < 0 || vertex >= componentOf.length) {
            throw new IndexOutOfBoundsException("Invalid vertex: " + vertex);
        }
        return componentOf[vertex];
    }

    public List<List<Integer>> components() {
        List<List<Integer>> groups = new ArrayList<>(componentCount);
        for (int component = 0; component < componentCount; component++) {
            groups.add(new ArrayList<>());
        }
        for (int vertex = 0; vertex < componentOf.length; vertex++) {
            groups.get(componentOf[vertex]).add(vertex);
        }
        return groups.stream().map(List::copyOf).toList();
    }

    @Override
    public String toString() {
        return "SccResult[componentCount=" + componentCount
                + ", componentOf=" + Arrays.toString(componentOf) + "]";
    }
}
