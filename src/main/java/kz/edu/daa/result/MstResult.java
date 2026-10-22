package kz.edu.daa.result;

import java.util.List;
import java.util.Objects;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.metrics.MetricsSnapshot;

/** Result of a minimum spanning tree or forest computation. */
public record MstResult(
        long totalWeight,
        List<WeightedEdge> edges,
        int components,
        MetricsSnapshot metrics) {

    public MstResult {
        edges = List.copyOf(Objects.requireNonNull(edges, "edges"));
        Objects.requireNonNull(metrics, "metrics");
        if (components < 0) {
            throw new IllegalArgumentException("Component count must be non-negative");
        }
    }
}
