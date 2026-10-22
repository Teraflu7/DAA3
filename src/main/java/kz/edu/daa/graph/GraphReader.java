package kz.edu.daa.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads the assignment format: n m directed|undirected, followed by m edges. */
public final class GraphReader {
    private GraphReader() {
    }

    public static Graph read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            DataLineReader lines = new DataLineReader(reader);
            DataLine header = lines.next();
            if (header == null) {
                throw new IllegalArgumentException("Graph file is empty");
            }

            String[] headerParts = split(header, 3, "header");
            int vertices = parseInt(headerParts[0], header.number(), "vertex count");
            int edgeCount = parseInt(headerParts[1], header.number(), "edge count");
            if (vertices < 0 || edgeCount < 0) {
                throw new IllegalArgumentException("Counts must be non-negative at line " + header.number());
            }

            boolean directed = switch (headerParts[2]) {
                case "directed" -> true;
                case "undirected" -> false;
                default -> throw new IllegalArgumentException(
                        "Expected directed or undirected at line " + header.number());
            };

            Graph graph = new Graph(vertices, directed);
            for (int index = 0; index < edgeCount; index++) {
                DataLine edgeLine = lines.next();
                if (edgeLine == null) {
                    throw new IllegalArgumentException(
                            "Expected " + edgeCount + " edges but found " + index);
                }
                String[] parts = split(edgeLine, 3, "edge");
                int from = parseInt(parts[0], edgeLine.number(), "from");
                int to = parseInt(parts[1], edgeLine.number(), "to");
                int weight = parseInt(parts[2], edgeLine.number(), "weight");
                try {
                    graph.addEdge(from, to, weight);
                } catch (IndexOutOfBoundsException exception) {
                    throw new IllegalArgumentException(
                            "Invalid endpoint at line " + edgeLine.number() + ": " + exception.getMessage(),
                            exception);
                }
            }

            DataLine extra = lines.next();
            if (extra != null) {
                throw new IllegalArgumentException("Unexpected extra edge at line " + extra.number());
            }
            return graph;
        }
    }

    private static String[] split(DataLine line, int expected, String kind) {
        String[] parts = line.text().split("\\s+");
        if (parts.length != expected) {
            throw new IllegalArgumentException(
                    "Invalid " + kind + " at line " + line.number() + ": expected " + expected + " fields");
        }
        return parts;
    }

    private static int parseInt(String value, int line, String field) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + field + " at line " + line + ": " + value, exception);
        }
    }

    private record DataLine(String text, int number) {
    }

    private static final class DataLineReader {
        private final BufferedReader reader;
        private int lineNumber;

        private DataLineReader(BufferedReader reader) {
            this.reader = reader;
        }

        private DataLine next() throws IOException {
            String raw;
            while ((raw = reader.readLine()) != null) {
                lineNumber++;
                int commentStart = raw.indexOf('#');
                String content = (commentStart >= 0 ? raw.substring(0, commentStart) : raw).trim();
                if (!content.isEmpty()) {
                    return new DataLine(content, lineNumber);
                }
            }
            return null;
        }
    }
}
