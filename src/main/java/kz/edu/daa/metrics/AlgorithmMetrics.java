package kz.edu.daa.metrics;

/** Mutable counters incremented at the exact operations they represent. */
public final class AlgorithmMetrics {
    private long findCalls;
    private long successfulUnions;
    private long edgeScans;
    private long weightComparisons;
    private long maxDepth;

    public void recordFindCall() {
        findCalls++;
    }

    public void recordSuccessfulUnion() {
        successfulUnions++;
    }

    public void recordEdgeScan() {
        edgeScans++;
    }

    public void recordWeightComparison() {
        weightComparisons++;
    }

    public void recordDepth(long depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth must be non-negative");
        }
        maxDepth = Math.max(maxDepth, depth);
    }

    public MetricsSnapshot snapshot() {
        return new MetricsSnapshot(findCalls, successfulUnions, edgeScans, weightComparisons, maxDepth);
    }

    public void reset() {
        findCalls = 0;
        successfulUnions = 0;
        edgeScans = 0;
        weightComparisons = 0;
        maxDepth = 0;
    }
}
