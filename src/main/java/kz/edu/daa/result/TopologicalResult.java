package kz.edu.daa.result;

import java.util.Arrays;
import java.util.Objects;
import kz.edu.daa.metrics.MetricsSnapshot;

/** Kahn result. A cyclic graph deliberately exposes no misleading partial order. */
public record TopologicalResult(boolean hasCycle, int[] order, MetricsSnapshot metrics) {
    public TopologicalResult {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(metrics, "metrics");
        order = order.clone();
        if (hasCycle && order.length != 0) {
            throw new IllegalArgumentException("A cyclic graph must not expose a partial order");
        }
    }

    @Override
    public int[] order() {
        return order.clone();
    }

    public boolean isAcyclic() {
        return !hasCycle;
    }

    @Override
    public String toString() {
        return "TopologicalResult[hasCycle=" + hasCycle
                + ", order=" + Arrays.toString(order) + "]";
    }
}
