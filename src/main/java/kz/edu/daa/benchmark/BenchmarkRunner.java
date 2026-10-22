package kz.edu.daa.benchmark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import kz.edu.daa.algorithm.DagShortestPaths;
import kz.edu.daa.algorithm.KahnTopologicalSort;
import kz.edu.daa.algorithm.KosarajuScc;
import kz.edu.daa.algorithm.KruskalMst;
import kz.edu.daa.algorithm.PrimMst;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.result.MstResult;
import kz.edu.daa.result.SccResult;
import kz.edu.daa.result.ShortestPathResult;
import kz.edu.daa.result.TopologicalResult;

/** Runs the reproducible benchmark, self-checks it, and writes CSV plus plots. */
public final class BenchmarkRunner {
    private static final int RUNS = 5;

    private BenchmarkRunner() {
    }

    public static void main(String[] args) throws IOException {
        Configuration configuration = Configuration.parse(args);
        List<BenchmarkRow> rows = run(configuration.quick());
        if (!configuration.quick()) {
            BenchmarkStatistics.printFullRunSummary(rows);
        }
        Path output = configuration.outputDirectory();
        CsvWriter.write(output.resolve("results.csv"), rows);
        PlotGenerator.generate(rows, output.resolve("plots"));
        System.out.printf(Locale.ROOT,
                "Wrote %d rows and three plots to %s%n", rows.size(), output.toAbsolutePath());
    }

    public static List<BenchmarkRow> run(boolean quick) {
        List<BenchmarkRow> rows = new ArrayList<>();
        List<Integer> sizes = quick
                ? ReferenceValues.sizes().subList(0, 1)
                : ReferenceValues.sizes();
        for (int vertices : sizes) {
            for (int multiplier : ReferenceValues.densityMultipliers()) {
                int edges = Math.multiplyExact(vertices, multiplier);
                String density = multiplier == 4 ? "sparse" : "dense";
                ReferenceValues.ReferenceValue reference = ReferenceValues.forGraph(vertices, edges);
                System.out.printf("Checking n=%d, m=%d (%s)%n", vertices, edges, density);

                rows.addAll(benchmarkMst(vertices, edges, density, reference));
                rows.add(benchmarkScc(vertices, edges, density, reference));
                rows.addAll(benchmarkDag(vertices, edges, density, reference));

                System.out.printf("  reference: MST=%d, SCC=%d, DAG sum=%d%n",
                        reference.mstWeight(), reference.sccCount(), reference.dagDistanceSum());
            }
        }
        return List.copyOf(rows);
    }

    private static List<BenchmarkRow> benchmarkMst(
            int vertices,
            int edges,
            String density,
            ReferenceValues.ReferenceValue reference) {
        Graph graph = GraphGenerator.connectedUndirected(vertices, edges);
        Measurement<MstResult> kruskal = measure(
                () -> KruskalMst.compute(graph), MstResult::totalWeight);
        Measurement<MstResult> prim = measure(
                () -> PrimMst.compute(graph), MstResult::totalWeight);

        verifyMst(graph, kruskal.value(), prim.value(), reference.mstWeight());
        return List.of(
                row("Kruskal", "undirected-" + density, graph,
                        kruskal.timeMs(), kruskal.value().metrics(), kruskal.value().totalWeight()),
                row("Prim", "undirected-" + density, graph,
                        prim.timeMs(), prim.value().metrics(), prim.value().totalWeight()));
    }

    private static BenchmarkRow benchmarkScc(
            int vertices,
            int edges,
            String density,
            ReferenceValues.ReferenceValue reference) {
        Graph graph = GraphGenerator.directed(vertices, edges);
        Measurement<SccResult> measurement = measure(
                () -> KosarajuScc.compute(graph), SccResult::componentCount);
        SccResult result = measurement.value();
        requireEqual("SCC count", reference.sccCount(), result.componentCount());
        requireEqual("Kosaraju edge scans", 2L * edges, result.metrics().edgeScans());
        return row("Kosaraju", "directed-" + density, graph,
                measurement.timeMs(), result.metrics(), result.componentCount());
    }

    private static List<BenchmarkRow> benchmarkDag(
            int vertices,
            int edges,
            String density,
            ReferenceValues.ReferenceValue reference) {
        Graph graph = GraphGenerator.dag(vertices, edges);
        Measurement<TopologicalResult> kahn = measure(
                () -> KahnTopologicalSort.compute(graph), result -> result.order().length);
        Measurement<ShortestPathResult> shortest = measure(
                () -> DagShortestPaths.compute(graph), ShortestPathResult::distanceSum);

        TopologicalResult topological = kahn.value();
        if (topological.hasCycle() || !KahnTopologicalSort.isValidOrder(graph, topological.order())) {
            throw new IllegalStateException("Generated DAG failed topological validation");
        }
        requireEqual("Kahn order size", vertices, topological.order().length);
        requireEqual("Kahn edge scans", edges, topological.metrics().edgeScans());
        requireEqual("DAG distance sum", reference.dagDistanceSum(), shortest.value().distanceSum());
        requireEqual("DAG edge scans", edges, shortest.value().metrics().edgeScans());

        return List.of(
                row("Kahn", "dag-" + density, graph,
                        kahn.timeMs(), topological.metrics(), topological.order().length),
                row("DagShortestPath", "dag-" + density, graph,
                        shortest.timeMs(), shortest.value().metrics(), shortest.value().distanceSum()));
    }

    private static void verifyMst(
            Graph graph,
            MstResult kruskal,
            MstResult prim,
            long expectedWeight) {
        requireEqual("Kruskal/Prim weight", kruskal.totalWeight(), prim.totalWeight());
        requireEqual("reference MST weight", expectedWeight, kruskal.totalWeight());
        requireEqual("Kruskal component count", 1, kruskal.components());
        requireEqual("Prim component count", 1, prim.components());
        requireEqual("Kruskal edge count", graph.vertexCount() - 1, kruskal.edges().size());
        requireEqual("Prim edge count", graph.vertexCount() - 1, prim.edges().size());
    }

    private static BenchmarkRow row(
            String algorithm,
            String graphName,
            Graph graph,
            double timeMs,
            kz.edu.daa.metrics.MetricsSnapshot metrics,
            long result) {
        return new BenchmarkRow(
                algorithm, graphName, graph.vertexCount(), graph.edgeCount(), timeMs, metrics, result);
    }

    private static <T> Measurement<T> measure(Supplier<T> algorithm, ToLongFunction<T> checksum) {
        Objects.requireNonNull(algorithm, "algorithm");
        long[] durations = new long[RUNS];
        T representative = null;
        long expectedChecksum = 0;

        for (int run = 0; run < RUNS; run++) {
            long start = System.nanoTime();
            T value = algorithm.get();
            durations[run] = System.nanoTime() - start;
            long actualChecksum = checksum.applyAsLong(value);
            if (run == 0) {
                representative = value;
                expectedChecksum = actualChecksum;
            } else if (actualChecksum != expectedChecksum) {
                throw new IllegalStateException("An algorithm returned inconsistent results across runs");
            }
        }

        Arrays.sort(durations);
        return new Measurement<>(representative, durations[RUNS / 2] / 1_000_000.0);
    }

    private static void requireEqual(String label, long expected, long actual) {
        if (expected != actual) {
            throw new IllegalStateException(label + " mismatch: expected " + expected + ", got " + actual);
        }
    }

    private record Measurement<T>(T value, double timeMs) {
    }

    private record Configuration(boolean quick, Path outputDirectory) {
        private static Configuration parse(String[] args) {
            boolean quick = false;
            Path output = null;
            for (String argument : args) {
                if (argument.equals("--quick")) {
                    quick = true;
                } else if (argument.equals("--full")) {
                    quick = false;
                } else if (argument.startsWith("--output=")) {
                    output = Path.of(argument.substring("--output=".length()));
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + argument);
                }
            }
            if (output == null) {
                output = Path.of(quick ? "results-quick" : "results");
            }
            return new Configuration(quick, output);
        }
    }
}
