package kz.edu.daa.benchmark;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Reproducible least-squares slopes used in REPORT.md. */
public final class BenchmarkStatistics {
    private BenchmarkStatistics() {
    }

    public static double logLogSlope(
            List<BenchmarkRow> rows,
            Predicate<BenchmarkRow> include,
            ToDoubleFunction<BenchmarkRow> yValue) {
        List<BenchmarkRow> selected = rows.stream().filter(include).toList();
        if (selected.size() < 2) {
            throw new IllegalArgumentException("At least two points are required for a slope");
        }

        double meanX = selected.stream().mapToDouble(row -> Math.log(row.n())).average().orElseThrow();
        double meanY = selected.stream().mapToDouble(row -> Math.log(yValue.applyAsDouble(row)))
                .average().orElseThrow();
        double numerator = 0;
        double denominator = 0;
        for (BenchmarkRow row : selected) {
            double centeredX = Math.log(row.n()) - meanX;
            double centeredY = Math.log(yValue.applyAsDouble(row)) - meanY;
            numerator += centeredX * centeredY;
            denominator += centeredX * centeredX;
        }
        return numerator / denominator;
    }

    public static void printFullRunSummary(List<BenchmarkRow> rows) {
        for (String density : List.of("sparse", "dense")) {
            double traversal = logLogSlope(rows,
                    row -> row.algorithm().equals("Kahn") && row.density().equals(density),
                    row -> row.metrics().edgeScans());
            double kruskalOperations = logLogSlope(rows,
                    row -> row.algorithm().equals("Kruskal") && row.density().equals(density),
                    row -> row.totalOperations());
            double kruskalScans = logLogSlope(rows,
                    row -> row.algorithm().equals("Kruskal") && row.density().equals(density),
                    row -> row.metrics().edgeScans());
            System.out.printf(Locale.ROOT,
                    "Slopes %s: traversal=%.3f, Kruskal operations=%.3f, Kruskal scans=%.3f%n",
                    density, traversal, kruskalOperations, kruskalScans);
        }
    }
}
