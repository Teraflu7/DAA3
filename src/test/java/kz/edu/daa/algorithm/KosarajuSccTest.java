package kz.edu.daa.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.GraphReader;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.result.SccResult;
import org.junit.jupiter.api.Test;

class KosarajuSccTest {
    @Test
    void findsKnownComponentsWithLoopsParallelEdgesAndIsolation() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "cyclic-directed.txt"));

        SccResult result = KosarajuScc.compute(graph);

        assertEquals(4, result.componentCount());
        assertSameComponent(result, 0, 1, 2);
        assertSameComponent(result, 3, 4);
        assertNotEquals(result.componentOf(4), result.componentOf(5));
        assertNotEquals(result.componentOf(5), result.componentOf(6));
        assertEquals(2L * graph.edgeCount(), result.metrics().edgeScans());
    }

    @Test
    void agreesWithMutualReachabilityOnRandomGraphs() {
        Random random = new Random(2_026);
        for (int test = 0; test < 100; test++) {
            int vertices = 1 + random.nextInt(200);
            Graph graph = new Graph(vertices, true);
            int edges = random.nextInt(vertices * 5 + 1);
            for (int edge = 0; edge < edges; edge++) {
                graph.addEdge(random.nextInt(vertices), random.nextInt(vertices), 1);
            }

            SccResult result = KosarajuScc.compute(graph);
            boolean[][] reachable = reachability(graph);
            for (int from = 0; from < vertices; from++) {
                for (int to = 0; to < vertices; to++) {
                    boolean expectedSame = reachable[from][to] && reachable[to][from];
                    assertEquals(expectedSame,
                            result.componentOf(from) == result.componentOf(to),
                            "test=" + test + ", from=" + from + ", to=" + to);
                }
            }
            assertEquals(2L * graph.edgeCount(), result.metrics().edgeScans());
        }
    }

    @Test
    void survivesPathOfOneHundredThousandVerticesWithoutRecursion() {
        int vertices = 100_000;
        Graph path = new Graph(vertices, true);
        for (int vertex = 0; vertex + 1 < vertices; vertex++) {
            path.addEdge(vertex, vertex + 1, 1);
        }

        SccResult result = KosarajuScc.compute(path);

        assertEquals(vertices, result.componentCount());
        assertEquals(vertices, result.metrics().maxDepth());
        assertEquals(2L * path.edgeCount(), result.metrics().edgeScans());
    }

    @Test
    void handlesEmptyGraphAndRejectsUndirectedInput() {
        SccResult empty = KosarajuScc.compute(new Graph(0, true));
        assertEquals(0, empty.componentCount());
        assertEquals(0, empty.componentOf().length);

        assertThrows(IllegalArgumentException.class,
                () -> KosarajuScc.compute(new Graph(2, false)));
    }

    private static void assertSameComponent(SccResult result, int first, int... others) {
        for (int vertex : others) {
            assertEquals(result.componentOf(first), result.componentOf(vertex));
        }
    }

    private static boolean[][] reachability(Graph graph) {
        boolean[][] reachable = new boolean[graph.vertexCount()][graph.vertexCount()];
        for (int start = 0; start < graph.vertexCount(); start++) {
            Queue<Integer> queue = new ArrayDeque<>();
            reachable[start][start] = true;
            queue.add(start);
            while (!queue.isEmpty()) {
                int vertex = queue.remove();
                for (WeightedEdge edge : graph.neighbors(vertex)) {
                    if (!reachable[start][edge.to()]) {
                        reachable[start][edge.to()] = true;
                        queue.add(edge.to());
                    }
                }
            }
        }
        return reachable;
    }
}
