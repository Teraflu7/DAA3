package kz.edu.daa.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BenchmarkSmokeTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void quickRunProducesTenRowsCsvAndThreeReadablePlots() throws IOException {
        List<BenchmarkRow> rows = BenchmarkRunner.run(true);
        Path csv = temporaryDirectory.resolve("results.csv");
        Path plots = temporaryDirectory.resolve("plots");
        CsvWriter.write(csv, rows);
        PlotGenerator.generate(rows, plots);

        assertEquals(10, rows.size());
        List<String> csvLines = Files.readAllLines(csv);
        assertEquals(CsvWriter.HEADER, csvLines.get(0));
        assertEquals(11, csvLines.size());

        for (String file : List.of(
                "operations-vs-n.png", "scans-per-edge.png", "kruskal-edge-share.png")) {
            Path imagePath = plots.resolve(file);
            assertTrue(Files.size(imagePath) > 10_000, file);
            BufferedImage image = ImageIO.read(imagePath.toFile());
            assertEquals(PlotGenerator.WIDTH, image.getWidth());
            assertEquals(PlotGenerator.HEIGHT, image.getHeight());
        }
    }
}
