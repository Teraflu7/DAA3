package kz.edu.daa.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DataFilesTest {
    @Test
    void readsEveryTrackedInputGraph() throws IOException {
        Graph connected = GraphReader.read(Path.of("data", "connected-undirected.txt"));
        Graph disconnected = GraphReader.read(Path.of("data", "disconnected-undirected.txt"));
        Graph cyclic = GraphReader.read(Path.of("data", "cyclic-directed.txt"));
        Graph dag = GraphReader.read(Path.of("data", "dag-negative.txt"));

        assertFalse(connected.isDirected());
        assertEquals(7, connected.edgeCount());
        assertFalse(disconnected.isDirected());
        assertEquals(0, disconnected.neighbors(5).size());
        assertTrue(cyclic.isDirected());
        assertEquals(2, cyclic.neighbors(4).stream().filter(edge -> edge.to() == 5).count());
        assertTrue(dag.isDirected());
        assertEquals(-2, dag.neighbors(2).get(0).weight());
    }
}
