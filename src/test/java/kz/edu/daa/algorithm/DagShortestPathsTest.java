package kz.edu.daa.algorithm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.GraphReader;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.result.ShortestPathResult;
import org.junit.jupiter.api.Test;

class DagShortestPathsTest {
    @Test
    void handlesMultipleSourcesNegativeWeightsAndRestoresPath() throws IOException {
        Graph dag = GraphReader.read(Path.of("data", "dag-negative.txt"));

        ShortestPathResult result = DagShortestPaths.compute(dag);

        assertArrayEquals(new long[] {0, 0, 1, -1, 2, 10}, result.distances());
        assertEquals(12, result.distanceSum());
        assertEquals(List.of(1, 2, 3, 4), result.pathTo(4));
        assertEquals(dag.edgeCount(), result.metrics().edgeScans());
    }

    @Test
    void agreesWithBellmanFordOnOneHundredRandomDags() {
        Random random = new Random(9_001);
        for (int test = 0; test < 100; test++) {
            int vertices = 1 + random.nextInt(30);
            Graph dag = new Graph(vertices, true);
            for (int from = 0; from < vertices; from++) {
                for (int to = from + 1; to < vertices; to++) {
                    if (random.nextDouble() < 0.16) {
                        dag.addEdge(from, to, random.nextInt(41) - 20);
                    }
                }
            }

            ShortestPathResult actual = DagShortestPaths.compute(dag);
            assertArrayEquals(bellmanFordFromVirtualSource(dag), actual.distances(),
                    "random DAG " + test);
            assertEquals(dag.edgeCount(), actual.metrics().edgeScans());
        }
    }

    @Test
    void rejectsCyclesAndUndirectedGraphs() {
        Graph cyclic = new Graph(2, true);
        cyclic.addEdge(0, 1, 1);
        cyclic.addEdge(1, 0, -2);
        assertThrows(IllegalArgumentException.class, () -> DagShortestPaths.compute(cyclic));

        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPaths.compute(new Graph(2, false)));
    }

    private static long[] bellmanFordFromVirtualSource(Graph graph) {
        long[] distances = new long[graph.vertexCount()];
        Arrays.fill(distances, ShortestPathResult.INF);
        int[] indegrees = graph.indegrees();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            if (indegrees[vertex] == 0) {
                distances[vertex] = 0;
            }
        }

        for (int pass = 1; pass < graph.vertexCount(); pass++) {
            boolean changed = false;
            for (WeightedEdge edge : graph.edges()) {
                if (distances[edge.from()] != ShortestPathResult.INF) {
                    long candidate = distances[edge.from()] + edge.weight();
                    if (candidate < distances[edge.to()]) {
                        distances[edge.to()] = candidate;
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }
        return distances;
    }
}
