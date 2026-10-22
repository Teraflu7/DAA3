package kz.edu.daa.benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Writes the exact CSV schema required by the assignment. */
public final class CsvWriter {
    public static final String HEADER =
            "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";

    private CsvWriter() {
    }

    public static void write(Path path, List<BenchmarkRow> rows) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (BenchmarkRow row : rows) {
                writer.write(format(row));
                writer.newLine();
            }
        }
    }

    private static String format(BenchmarkRow row) {
        return String.format(Locale.ROOT,
                "%s,%s,%d,%d,%.6f,%d,%d,%d,%d,%d",
                row.algorithm(),
                row.graph(),
                row.n(),
                row.m(),
                row.timeMs(),
                row.metrics().dsuOps(),
                row.metrics().edgeScans(),
                row.metrics().weightComparisons(),
                row.metrics().maxDepth(),
                row.result());
    }
}
