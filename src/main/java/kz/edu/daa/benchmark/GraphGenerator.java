package kz.edu.daa.benchmark;

import java.util.Random;
import kz.edu.daa.graph.Graph;

/** Exact deterministic generators specified in the assignment. */
public final class GraphGenerator {
    public static final long SEED = 42L;

    private GraphGenerator() {
    }

    public static Graph connectedUndirected(int vertices, int edges) {
        validateVertexAndEdgeCounts(vertices, edges);
        if (vertices == 0 && edges != 0) {
            throw new IllegalArgumentException("An empty graph cannot contain edges");
        }
        if (vertices > 0 && edges < vertices - 1) {
            throw new IllegalArgumentException("A connected graph needs at least n - 1 edges");
        }
        requireTwoVerticesWhenEdgesExist(vertices, edges);

        Random random = new Random(SEED);
        int[] permutation = shuffledRange(vertices, random);
        Graph graph = new Graph(vertices, false);

        for (int index = 1; index < vertices; index++) {
            int parent = permutation[random.nextInt(index)];
            int child = permutation[index];
            graph.addEdge(parent, child, nextWeight(random));
        }
        addRandomEdges(graph, edges - graph.edgeCount(), random, false, null);
        return graph;
    }

    public static Graph directed(int vertices, int edges) {
        validateVertexAndEdgeCounts(vertices, edges);
        requireTwoVerticesWhenEdgesExist(vertices, edges);

        Random random = new Random(SEED);
        Graph graph = new Graph(vertices, true);
        addRandomEdges(graph, edges, random, false, null);
        return graph;
    }

    public static Graph dag(int vertices, int edges) {
        validateVertexAndEdgeCounts(vertices, edges);
        requireTwoVerticesWhenEdgesExist(vertices, edges);

        Random random = new Random(SEED);
        int[] rank = shuffledRange(vertices, random);
        Graph graph = new Graph(vertices, true);
        addRandomEdges(graph, edges, random, true, rank);
        return graph;
    }

    private static int[] shuffledRange(int size, Random random) {
        int[] values = new int[size];
        for (int index = 0; index < size; index++) {
            values[index] = index;
        }
        for (int index = size - 1; index > 0; index--) {
            int other = random.nextInt(index + 1);
            int temporary = values[index];
            values[index] = values[other];
            values[other] = temporary;
        }
        return values;
    }

    private static void addRandomEdges(
            Graph graph,
            int count,
            Random random,
            boolean orientByRank,
            int[] rank) {
        for (int edge = 0; edge < count; edge++) {
            int from = random.nextInt(graph.vertexCount());
            int to = random.nextInt(graph.vertexCount());
            while (to == from) {
                to = random.nextInt(graph.vertexCount());
            }
            if (orientByRank && rank[from] > rank[to]) {
                int temporary = from;
                from = to;
                to = temporary;
            }
            graph.addEdge(from, to, nextWeight(random));
        }
    }

    private static int nextWeight(Random random) {
        return 1 + random.nextInt(1_000);
    }

    private static void validateVertexAndEdgeCounts(int vertices, int edges) {
        if (vertices < 0 || edges < 0) {
            throw new IllegalArgumentException("Vertex and edge counts must be non-negative");
        }
    }

    private static void requireTwoVerticesWhenEdgesExist(int vertices, int edges) {
        if (vertices < 2 && edges > 0) {
            throw new IllegalArgumentException("The generator forbids self-loops, so n must be at least 2");
        }
    }
}
