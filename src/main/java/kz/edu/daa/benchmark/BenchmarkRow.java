package kz.edu.daa.benchmark;

import java.util.Objects;
import kz.edu.daa.metrics.MetricsSnapshot;

/** One row of results/results.csv. */
public record BenchmarkRow(
        String algorithm,
        String graph,
        int n,
        int m,
        double timeMs,
        MetricsSnapshot metrics,
        long result) {

    public BenchmarkRow {
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(metrics, "metrics");
        if (n < 0 || m < 0 || !Double.isFinite(timeMs) || timeMs < 0) {
            throw new IllegalArgumentException("Invalid benchmark row");
        }
    }

    public long totalOperations() {
        return metrics.totalOperations();
    }

    public double scansPerEdge() {
        return m == 0 ? 0.0 : (double) metrics.edgeScans() / m;
    }

    public String density() {
        return graph.endsWith("-sparse") ? "sparse" : "dense";
    }
}
