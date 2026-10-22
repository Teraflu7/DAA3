package kz.edu.daa.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class CommittedResultsTest {
    private static final Path RESULTS = Path.of("results", "results.csv");
    private static final Path PLOTS = Path.of("results", "plots");

    @Test
    void committedCsvContainsAllReferenceCasesAndValidCounters() throws IOException {
        List<String> lines = Files.readAllLines(RESULTS, StandardCharsets.UTF_8);
        assertEquals(CsvWriter.HEADER, lines.get(0));
        assertEquals(51, lines.size(), "header plus exactly 50 benchmark rows");

        Map<Key, CsvRow> rows = new HashMap<>();
        for (String line : lines.subList(1, lines.size())) {
            CsvRow row = CsvRow.parse(line);
            assertTrue(Double.isFinite(row.timeMs()) && row.timeMs() >= 0);
            assertTrue(row.dsuOps() >= 0 && row.edgeScans() >= 0
                    && row.comparisons() >= 0 && row.maxDepth() >= 0);
            Key key = new Key(row.algorithm(), row.graph(), row.n(), row.m());
            assertNull(rows.put(key, row), "duplicate CSV row: " + key);
        }
        assertEquals(50, rows.size());

        for (ReferenceValues.ReferenceValue reference : ReferenceValues.all()) {
            int n = reference.vertices();
            int m = reference.edges();
            String density = m == Math.multiplyExact(4, n) ? "sparse" : "dense";

            CsvRow kruskal = require(rows, "Kruskal", "undirected-" + density, n, m);
            assertEquals(reference.mstWeight(), kruskal.result());
            assertTrue(kruskal.edgeScans() >= n - 1L && kruskal.edgeScans() <= m);
            assertEquals(2L * kruskal.edgeScans() + n - 1L, kruskal.dsuOps());
            assertTrue(kruskal.comparisons() > 0);
            assertEquals(0, kruskal.maxDepth());

            CsvRow prim = require(rows, "Prim", "undirected-" + density, n, m);
            assertEquals(reference.mstWeight(), prim.result());
            assertEquals(2L * m, prim.edgeScans());
            assertEquals(0, prim.dsuOps());
            assertTrue(prim.comparisons() > 0);
            assertEquals(0, prim.maxDepth());

            CsvRow kosaraju = require(rows, "Kosaraju", "directed-" + density, n, m);
            assertEquals(reference.sccCount(), kosaraju.result());
            assertEquals(2L * m, kosaraju.edgeScans());
            assertEquals(0, kosaraju.dsuOps());
            assertEquals(0, kosaraju.comparisons());
            assertTrue(kosaraju.maxDepth() >= 1 && kosaraju.maxDepth() <= n);

            CsvRow kahn = require(rows, "Kahn", "dag-" + density, n, m);
            assertEquals(n, kahn.result());
            assertEquals(m, kahn.edgeScans());
            assertEquals(0, kahn.dsuOps());
            assertEquals(0, kahn.comparisons());
            assertEquals(0, kahn.maxDepth());

            CsvRow dag = require(rows, "DagShortestPath", "dag-" + density, n, m);
            assertEquals(reference.dagDistanceSum(), dag.result());
            assertEquals(m, dag.edgeScans());
            assertEquals(m, dag.comparisons());
            assertEquals(0, dag.dsuOps());
            assertEquals(0, dag.maxDepth());
        }
    }

    @Test
    void committedPlotsAreTheExpectedReadableImages() throws IOException {
        Set<String> expected = Set.of(
                "operations-vs-n.png", "scans-per-edge.png", "kruskal-edge-share.png");
        Set<String> actual;
        try (var paths = Files.list(PLOTS)) {
            actual = paths.map(path -> path.getFileName().toString()).collect(Collectors.toSet());
        }
        assertEquals(expected, actual);

        for (String fileName : expected) {
            Path path = PLOTS.resolve(fileName);
            assertTrue(Files.isRegularFile(path));
            assertTrue(Files.size(path) > 10_000, "plot should not be an empty placeholder");
            BufferedImage image = ImageIO.read(path.toFile());
            assertNotNull(image, "PNG must be readable: " + fileName);
            assertEquals(PlotGenerator.WIDTH, image.getWidth());
            assertEquals(PlotGenerator.HEIGHT, image.getHeight());
        }
    }

    @Test
    void benchmarkConfigurationCannotBeMutated() {
        assertEquals(List.of(1_000, 3_162, 10_000, 31_623, 100_000), ReferenceValues.sizes());
        assertEquals(List.of(4, 20), ReferenceValues.densityMultipliers());
        assertThrows(UnsupportedOperationException.class, () -> ReferenceValues.sizes().add(7));
        assertThrows(UnsupportedOperationException.class,
                () -> ReferenceValues.densityMultipliers().add(7));
    }

    private static CsvRow require(
            Map<Key, CsvRow> rows,
            String algorithm,
            String graph,
            int n,
            int m) {
        Key key = new Key(algorithm, graph, n, m);
        CsvRow row = rows.get(key);
        assertNotNull(row, "missing CSV row: " + key);
        return row;
    }

    private record Key(String algorithm, String graph, int n, int m) {
    }

    private record CsvRow(
            String algorithm,
            String graph,
            int n,
            int m,
            double timeMs,
            long dsuOps,
            long edgeScans,
            long comparisons,
            long maxDepth,
            long result) {

        private static CsvRow parse(String line) {
            String[] fields = line.split(",", -1);
            assertEquals(10, fields.length, "CSV column count: " + line);
            return new CsvRow(
                    fields[0],
                    fields[1],
                    Integer.parseInt(fields[2]),
                    Integer.parseInt(fields[3]),
                    Double.parseDouble(fields[4]),
                    Long.parseLong(fields[5]),
                    Long.parseLong(fields[6]),
                    Long.parseLong(fields[7]),
                    Long.parseLong(fields[8]),
                    Long.parseLong(fields[9]));
        }
    }
}
