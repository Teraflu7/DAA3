package kz.edu.daa.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import kz.edu.daa.dsu.DisjointSetUnion;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.GraphReader;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.result.MstResult;
import org.junit.jupiter.api.Test;

class MstTest {
    @Test
    void findsKnownMinimumSpanningTree() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "connected-undirected.txt"));

        assertAgreement(graph, 14, 1);
    }

    @Test
    void returnsForestForDisconnectedGraphAndIgnoresNegativeLoop() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "disconnected-undirected.txt"));
        MstResult kruskal = KruskalMst.compute(graph);
        MstResult prim = PrimMst.compute(graph);

        assertEquals(4, kruskal.totalWeight());
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(3, kruskal.components());
        assertEquals(kruskal.components(), prim.components());
        assertEquals(graph.vertexCount() - kruskal.components(), kruskal.edges().size());
        assertEquals(graph.vertexCount() - prim.components(), prim.edges().size());
        assertEquals(graph.edgeCount(), kruskal.metrics().edgeScans());
        assertEquals(2L * graph.edgeCount(), prim.metrics().edgeScans());
        assertEquals(kruskal.edges().size(), kruskal.metrics().successfulUnions());
        assertValidForest(graph, kruskal);
        assertValidForest(graph, prim);
    }

    @Test
    void supportsEmptyGraphsExtremeWeightsAndParallelEdges() {
        Graph empty = new Graph(0, false);
        assertAgreement(empty, 0, 0);

        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1, Integer.MAX_VALUE);
        graph.addEdge(0, 1, Integer.MIN_VALUE);
        graph.addEdge(1, 2, Integer.MAX_VALUE);
        graph.addEdge(2, 2, Integer.MIN_VALUE);
        assertAgreement(graph, -1, 1);
    }

    @Test
    void rejectsDirectedGraphs() {
        Graph directed = new Graph(2, true);
        directed.addEdge(0, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> KruskalMst.compute(directed));
        assertThrows(IllegalArgumentException.class, () -> PrimMst.compute(directed));
    }

    @Test
    void kruskalAndPrimAgreeOnOneHundredRandomConnectedGraphs() {
        Random random = new Random(1_003);
        for (int test = 0; test < 100; test++) {
            int vertices = 2 + random.nextInt(29);
            Graph graph = new Graph(vertices, false);

            for (int vertex = 1; vertex < vertices; vertex++) {
                int parent = random.nextInt(vertex);
                graph.addEdge(parent, vertex, random.nextInt(201) - 100);
            }
            for (int edge = 0; edge < vertices * 3; edge++) {
                int from = random.nextInt(vertices);
                int to = random.nextInt(vertices);
                graph.addEdge(from, to, random.nextInt(201) - 100);
            }

            MstResult kruskal = KruskalMst.compute(graph);
            MstResult prim = PrimMst.compute(graph);
            assertEquals(kruskal.totalWeight(), prim.totalWeight(), "test graph " + test);
            assertEquals(1, kruskal.components());
            assertEquals(1, prim.components());
            assertEquals(vertices - 1, kruskal.edges().size());
            assertEquals(vertices - 1, prim.edges().size());
            assertValidForest(graph, kruskal);
            assertValidForest(graph, prim);
        }
    }

    private static void assertAgreement(Graph graph, long expectedWeight, int expectedComponents) {
        MstResult kruskal = KruskalMst.compute(graph);
        MstResult prim = PrimMst.compute(graph);

        assertEquals(expectedWeight, kruskal.totalWeight());
        assertEquals(expectedWeight, prim.totalWeight());
        assertEquals(expectedComponents, kruskal.components());
        assertEquals(expectedComponents, prim.components());
        assertEquals(graph.vertexCount() - expectedComponents, kruskal.edges().size());
        assertEquals(graph.vertexCount() - expectedComponents, prim.edges().size());
        assertValidForest(graph, kruskal);
        assertValidForest(graph, prim);
    }

    private static void assertValidForest(Graph graph, MstResult result) {
        Map<EdgeKey, Integer> available = new HashMap<>();
        for (WeightedEdge edge : graph.edges()) {
            available.merge(EdgeKey.of(edge), 1, Integer::sum);
        }

        DisjointSetUnion originalConnectivity = DisjointSetUnion.makeSet(graph.vertexCount());
        for (WeightedEdge edge : graph.edges()) {
            originalConnectivity.union(edge.from(), edge.to());
        }

        DisjointSetUnion selectedConnectivity = DisjointSetUnion.makeSet(graph.vertexCount());
        long weightSum = 0;
        for (WeightedEdge edge : result.edges()) {
            EdgeKey key = EdgeKey.of(edge);
            int remaining = available.getOrDefault(key, 0);
            assertTrue(remaining > 0, "selected edge must exist in the input: " + edge);
            available.put(key, remaining - 1);
            assertTrue(selectedConnectivity.union(edge.from(), edge.to()),
                    "selected edges must be acyclic");
            weightSum += edge.weight();
        }
        assertEquals(result.totalWeight(), weightSum);
        assertEquals(graph.vertexCount() - result.components(), result.edges().size());

        for (int first = 0; first < graph.vertexCount(); first++) {
            for (int second = 0; second < graph.vertexCount(); second++) {
                assertEquals(originalConnectivity.connected(first, second),
                        selectedConnectivity.connected(first, second),
                        "the forest must span each original component");
            }
        }
    }

    private record EdgeKey(int low, int high, int weight) {
        private static EdgeKey of(WeightedEdge edge) {
            return new EdgeKey(
                    Math.min(edge.from(), edge.to()),
                    Math.max(edge.from(), edge.to()),
                    edge.weight());
        }
    }
}
