package kz.edu.daa.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AlgorithmMetricsTest {
    @Test
    void recordsOperationsAndKeepsMaximumDepth() {
        AlgorithmMetrics metrics = new AlgorithmMetrics();
        metrics.recordFindCall();
        metrics.recordFindCall();
        metrics.recordSuccessfulUnion();
        metrics.recordEdgeScan();
        metrics.recordWeightComparison();
        metrics.recordDepth(4);
        metrics.recordDepth(2);

        MetricsSnapshot snapshot = metrics.snapshot();
        assertEquals(2, snapshot.findCalls());
        assertEquals(1, snapshot.successfulUnions());
        assertEquals(3, snapshot.dsuOps());
        assertEquals(1, snapshot.edgeScans());
        assertEquals(1, snapshot.weightComparisons());
        assertEquals(4, snapshot.maxDepth());
        assertEquals(5, snapshot.totalOperations());
    }

    @Test
    void resetsEveryCounterAndRejectsNegativeDepth() {
        AlgorithmMetrics metrics = new AlgorithmMetrics();
        metrics.recordFindCall();
        metrics.recordSuccessfulUnion();
        metrics.recordEdgeScan();
        metrics.recordWeightComparison();
        metrics.recordDepth(8);

        metrics.reset();

        assertEquals(new MetricsSnapshot(0, 0, 0, 0, 0), metrics.snapshot());
        assertThrows(IllegalArgumentException.class, () -> metrics.recordDepth(-1));
    }
}
