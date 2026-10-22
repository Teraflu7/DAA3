package kz.edu.daa.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphReaderTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void readsCommentsBlankLinesAndNegativeWeights() throws IOException {
        Path file = write("""
                # a directed graph
                4 3 directed

                0 1 7
                1 2 -5 # negative weights are valid
                0 3 2
                """);

        Graph graph = GraphReader.read(file);

        assertTrue(graph.isDirected());
        assertEquals(4, graph.vertexCount());
        assertEquals(3, graph.edgeCount());
        assertEquals(-5, graph.neighbors(1).get(0).weight());
    }

    @Test
    void readsUndirectedGraph() throws IOException {
        Graph graph = GraphReader.read(write("""
                3 2 undirected
                0 1 9
                1 2 8
                """));

        assertFalse(graph.isDirected());
        assertEquals(1, graph.neighbors(0).size());
        assertEquals(2, graph.neighbors(1).size());
    }

    @Test
    void rejectsMalformedFiles() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> GraphReader.read(write("3 two directed")));
        assertThrows(IllegalArgumentException.class,
                () -> GraphReader.read(write("3 2 directed\n0 1 4")));
        assertThrows(IllegalArgumentException.class,
                () -> GraphReader.read(write("3 1 directed\n0 1 4\n1 2 5")));
        assertThrows(IllegalArgumentException.class,
                () -> GraphReader.read(write("3 1 directed\n0 3 4")));
        assertThrows(IllegalArgumentException.class,
                () -> GraphReader.read(write("3 0 sideways")));
    }

    private Path write(String content) throws IOException {
        Path file = Files.createTempFile(temporaryDirectory, "graph-", ".txt");
        return Files.writeString(file, content);
    }
}
