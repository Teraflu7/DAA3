package kz.edu.daa.metrics;

/** Immutable operation counts produced by one algorithm run. */
public record MetricsSnapshot(
        long findCalls,
        long successfulUnions,
        long edgeScans,
        long weightComparisons,
        long maxDepth) {

    public MetricsSnapshot {
        if (findCalls < 0 || successfulUnions < 0 || edgeScans < 0
                || weightComparisons < 0 || maxDepth < 0) {
            throw new IllegalArgumentException("Metric values must be non-negative");
        }
    }

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    public long totalOperations() {
        return dsuOps() + edgeScans + weightComparisons;
    }
}
