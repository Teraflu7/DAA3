package kz.edu.daa.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.GraphReader;
import kz.edu.daa.result.TopologicalResult;
import org.junit.jupiter.api.Test;

class KahnTopologicalSortTest {
    @Test
    void returnsValidOrderForDag() throws IOException {
        Graph dag = GraphReader.read(Path.of("data", "dag-negative.txt"));

        TopologicalResult result = KahnTopologicalSort.compute(dag);

        assertTrue(result.isAcyclic());
        assertEquals(dag.vertexCount(), result.order().length);
        assertTrue(KahnTopologicalSort.isValidOrder(dag, result.order()));
        assertEquals(dag.edgeCount(), result.metrics().edgeScans());
    }

    @Test
    void reportsCyclesInsteadOfReturningPartialOrder() throws IOException {
        Graph cyclic = GraphReader.read(Path.of("data", "cyclic-directed.txt"));
        TopologicalResult result = KahnTopologicalSort.compute(cyclic);

        assertTrue(result.hasCycle());
        assertEquals(0, result.order().length);

        Graph loop = new Graph(1, true);
        loop.addEdge(0, 0, 1);
        assertTrue(KahnTopologicalSort.compute(loop).hasCycle());
    }

    @Test
    void validatesOrdersAndRejectsBadOnes() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 3, 1);

        assertTrue(KahnTopologicalSort.isValidOrder(graph, new int[] {0, 1, 2, 3}));
        assertTrue(KahnTopologicalSort.isValidOrder(graph, new int[] {0, 2, 3, 1}));
        assertFalse(KahnTopologicalSort.isValidOrder(graph, new int[] {1, 0, 2, 3}));
        assertFalse(KahnTopologicalSort.isValidOrder(graph, new int[] {0, 1, 1, 3}));
        assertFalse(KahnTopologicalSort.isValidOrder(graph, new int[] {0, 1, 2}));
    }

    @Test
    void handlesOneHundredRandomDags() {
        Random random = new Random(42);
        for (int test = 0; test < 100; test++) {
            int vertices = 1 + random.nextInt(50);
            Graph dag = new Graph(vertices, true);
            for (int from = 0; from < vertices; from++) {
                for (int to = from + 1; to < vertices; to++) {
                    if (random.nextDouble() < 0.08) {
                        dag.addEdge(from, to, 1);
                    }
                }
            }

            TopologicalResult result = KahnTopologicalSort.compute(dag);
            assertFalse(result.hasCycle());
            assertTrue(KahnTopologicalSort.isValidOrder(dag, result.order()));
            assertEquals(dag.edgeCount(), result.metrics().edgeScans());
        }
    }

    @Test
    void rejectsUndirectedGraph() {
        assertThrows(IllegalArgumentException.class,
                () -> KahnTopologicalSort.compute(new Graph(2, false)));
    }
}
