package kz.edu.daa.benchmark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.imageio.ImageIO;

/** Small dependency-free Java2D plotter for the three required PNG charts. */
public final class PlotGenerator {
    public static final int WIDTH = 1_400;
    public static final int HEIGHT = 900;

    private static final Color[] COLORS = {
        new Color(31, 119, 180), new Color(255, 127, 14),
        new Color(44, 160, 44), new Color(214, 39, 40),
        new Color(148, 103, 189), new Color(140, 86, 75),
        new Color(227, 119, 194), new Color(127, 127, 127),
        new Color(188, 189, 34), new Color(23, 190, 207)
    };

    private PlotGenerator() {
    }

    public static void generate(List<BenchmarkRow> rows, Path plotsDirectory) throws IOException {
        Files.createDirectories(plotsDirectory);

        drawChart(
                rows,
                row -> true,
                BenchmarkRow::totalOperations,
                row -> row.algorithm() + " / " + row.density(),
                "Operations vs n",
                "n (vertices, log scale)",
                "counted operations (log scale)",
                true,
                plotsDirectory.resolve("operations-vs-n.png"));

        drawChart(
                rows,
                row -> true,
                BenchmarkRow::scansPerEdge,
                row -> row.algorithm() + " / " + row.density(),
                "Scans per edge",
                "n (vertices, log scale)",
                "edgeScans / m",
                false,
                plotsDirectory.resolve("scans-per-edge.png"));

        drawChart(
                rows,
                row -> row.algorithm().equals("Kruskal"),
                BenchmarkRow::scansPerEdge,
                BenchmarkRow::density,
                "Share of edges examined by Kruskal",
                "n (vertices, log scale)",
                "edgeScans / m",
                false,
                plotsDirectory.resolve("kruskal-edge-share.png"));
    }

    private static void drawChart(
            List<BenchmarkRow> rows,
            Predicate<BenchmarkRow> include,
            ToDoubleFunction<BenchmarkRow> yValue,
            java.util.function.Function<BenchmarkRow, String> seriesName,
            String title,
            String xLabel,
            String yLabel,
            boolean logY,
            Path output) throws IOException {
        Map<String, List<Point>> series = new LinkedHashMap<>();
        for (BenchmarkRow row : rows) {
            if (include.test(row)) {
                double y = yValue.applyAsDouble(row);
                if (y > 0 || !logY) {
                    series.computeIfAbsent(seriesName.apply(row), ignored -> new ArrayList<>())
                            .add(new Point(row.n(), y));
                }
            }
        }
        for (List<Point> points : series.values()) {
            points.sort(Comparator.comparingDouble(Point::x));
        }
        if (series.isEmpty()) {
            throw new IllegalArgumentException("Cannot draw a chart without data");
        }

        Bounds bounds = bounds(series, logY);
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            drawAxes(graphics, bounds, title, xLabel, yLabel, logY);
            drawSeries(graphics, series, bounds, logY);
            drawLegend(graphics, series.keySet().stream().toList());
        } finally {
            graphics.dispose();
        }
        if (!ImageIO.write(image, "png", output.toFile())) {
            throw new IOException("No PNG writer is available");
        }
    }

    private static Bounds bounds(Map<String, List<Point>> series, boolean logY) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (List<Point> points : series.values()) {
            for (Point point : points) {
                double x = Math.log10(point.x());
                double y = logY ? Math.log10(point.y()) : point.y();
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        if (minX == maxX) {
            minX -= 0.1;
            maxX += 0.1;
        }
        if (minY == maxY) {
            double padding = Math.max(0.1, Math.abs(minY) * 0.1);
            minY -= padding;
            maxY += padding;
        } else {
            double padding = (maxY - minY) * 0.08;
            minY -= padding;
            maxY += padding;
        }
        return new Bounds(minX, maxX, minY, maxY);
    }

    private static void drawAxes(
            Graphics2D graphics,
            Bounds bounds,
            String title,
            String xLabel,
            String yLabel,
            boolean logY) {
        int left = 125;
        int top = 90;
        int right = 1_080;
        int bottom = 785;

        graphics.setColor(new Color(248, 249, 251));
        graphics.fillRect(left, top, right - left, bottom - top);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        for (int tick = 0; tick <= 5; tick++) {
            double fraction = tick / 5.0;
            int x = (int) Math.round(left + fraction * (right - left));
            int y = (int) Math.round(bottom - fraction * (bottom - top));
            graphics.setColor(new Color(218, 222, 228));
            graphics.drawLine(x, top, x, bottom);
            graphics.drawLine(left, y, right, y);

            graphics.setColor(new Color(55, 60, 68));
            double xValue = Math.pow(10, bounds.minX() + fraction * (bounds.maxX() - bounds.minX()));
            String xText = formatNumber(xValue);
            graphics.drawString(xText, x - graphics.getFontMetrics().stringWidth(xText) / 2, bottom + 28);

            double transformedY = bounds.minY() + fraction * (bounds.maxY() - bounds.minY());
            double yValue = logY ? Math.pow(10, transformedY) : transformedY;
            String yText = formatNumber(yValue);
            graphics.drawString(yText, left - 18 - graphics.getFontMetrics().stringWidth(yText), y + 6);
        }

        graphics.setColor(new Color(35, 39, 47));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRect(left, top, right - left, bottom - top);

        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        FontMetrics titleMetrics = graphics.getFontMetrics();
        graphics.drawString(title, (WIDTH - titleMetrics.stringWidth(title)) / 2, 48);

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        FontMetrics labelMetrics = graphics.getFontMetrics();
        graphics.drawString(xLabel, left + (right - left - labelMetrics.stringWidth(xLabel)) / 2, 845);

        graphics.rotate(-Math.PI / 2);
        graphics.drawString(yLabel, -(top + (bottom - top + labelMetrics.stringWidth(yLabel)) / 2), 35);
        graphics.rotate(Math.PI / 2);
    }

    private static void drawSeries(
            Graphics2D graphics,
            Map<String, List<Point>> series,
            Bounds bounds,
            boolean logY) {
        int colorIndex = 0;
        for (List<Point> points : series.values()) {
            graphics.setColor(COLORS[colorIndex++ % COLORS.length]);
            graphics.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Integer previousX = null;
            Integer previousY = null;
            for (Point point : points) {
                int x = map(Math.log10(point.x()), bounds.minX(), bounds.maxX(), 125, 1_080);
                double transformedY = logY ? Math.log10(point.y()) : point.y();
                int y = map(transformedY, bounds.minY(), bounds.maxY(), 785, 90);
                if (previousX != null) {
                    graphics.drawLine(previousX, previousY, x, y);
                }
                graphics.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
                previousX = x;
                previousY = y;
            }
        }
    }

    private static void drawLegend(Graphics2D graphics, List<String> names) {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        int y = 115;
        for (int index = 0; index < names.size(); index++) {
            graphics.setColor(COLORS[index % COLORS.length]);
            graphics.setStroke(new BasicStroke(3f));
            graphics.drawLine(1_115, y, 1_150, y);
            graphics.fill(new Ellipse2D.Double(1_129, y - 4, 8, 8));
            graphics.setColor(new Color(35, 39, 47));
            graphics.drawString(names.get(index), 1_162, y + 6);
            y += 32;
        }
    }

    private static int map(double value, double min, double max, int outputMin, int outputMax) {
        double fraction = (value - min) / (max - min);
        return (int) Math.round(outputMin + fraction * (outputMax - outputMin));
    }

    private static String formatNumber(double value) {
        double absolute = Math.abs(value);
        if ((absolute >= 10_000) || (absolute > 0 && absolute < 0.01)) {
            return String.format(Locale.ROOT, "%.1e", value);
        }
        if (absolute >= 100) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record Point(double x, double y) {
    }

    private record Bounds(double minX, double maxX, double minY, double maxY) {
    }
}
