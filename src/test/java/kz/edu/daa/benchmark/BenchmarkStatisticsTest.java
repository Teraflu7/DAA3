package kz.edu.daa.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import kz.edu.daa.metrics.MetricsSnapshot;
import org.junit.jupiter.api.Test;

class BenchmarkStatisticsTest {
    @Test
    void fitsKnownPowerLawOnLogLogAxes() {
        List<BenchmarkRow> rows = List.of(
                row(10, 100),
                row(100, 10_000),
                row(1_000, 1_000_000));

        double slope = BenchmarkStatistics.logLogSlope(
                rows, ignored -> true, value -> value.metrics().edgeScans());

        assertEquals(2.0, slope, 1.0e-12);
    }

    @Test
    void needsAtLeastTwoPoints() {
        assertThrows(IllegalArgumentException.class,
                () -> BenchmarkStatistics.logLogSlope(
                        List.of(row(10, 100)), ignored -> true,
                        value -> value.metrics().edgeScans()));
    }

    private static BenchmarkRow row(int n, long scans) {
        return new BenchmarkRow(
                "test", "dag-sparse", n, n, 0,
                new MetricsSnapshot(0, 0, scans, 0, 0), 0);
    }
}
