package kz.edu.daa.algorithm;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.metrics.AlgorithmMetrics;
import kz.edu.daa.result.SccResult;

/** Kosaraju's SCC algorithm with iterative DFS in both passes. */
public final class KosarajuScc {
    private KosarajuScc() {
    }

    public static SccResult compute(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Kosaraju requires a directed graph");
        }

        int vertices = graph.vertexCount();
        AlgorithmMetrics metrics = new AlgorithmMetrics();
        boolean[] visited = new boolean[vertices];
        int[] finishOrder = new int[vertices];
        int finishSize = fillFinishOrder(graph, visited, finishOrder, metrics);

        Graph reversed = graph.reversed();
        int[] componentOf = new int[vertices];
        Arrays.fill(componentOf, -1);
        int componentCount = assignComponents(
                reversed, finishOrder, finishSize, componentOf, metrics);

        return new SccResult(componentCount, componentOf, metrics.snapshot());
    }

    private static int fillFinishOrder(
            Graph graph,
            boolean[] visited,
            int[] finishOrder,
            AlgorithmMetrics metrics) {
        int[] vertexStack = new int[graph.vertexCount()];
        int[] nextNeighbor = new int[graph.vertexCount()];
        int finishSize = 0;

        for (int start = 0; start < graph.vertexCount(); start++) {
            if (visited[start]) {
                continue;
            }

            int depth = 0;
            visited[start] = true;
            vertexStack[depth] = start;
            nextNeighbor[depth] = 0;
            depth++;
            metrics.recordDepth(depth);

            while (depth > 0) {
                int top = depth - 1;
                int vertex = vertexStack[top];
                List<WeightedEdge> neighbors = graph.neighbors(vertex);

                if (nextNeighbor[top] < neighbors.size()) {
                    WeightedEdge edge = neighbors.get(nextNeighbor[top]++);
                    metrics.recordEdgeScan();
                    if (!visited[edge.to()]) {
                        visited[edge.to()] = true;
                        vertexStack[depth] = edge.to();
                        nextNeighbor[depth] = 0;
                        depth++;
                        metrics.recordDepth(depth);
                    }
                } else {
                    finishOrder[finishSize++] = vertex;
                    depth--;
                }
            }
        }
        return finishSize;
    }

    private static int assignComponents(
            Graph reversed,
            int[] finishOrder,
            int finishSize,
            int[] componentOf,
            AlgorithmMetrics metrics) {
        int[] stack = new int[reversed.vertexCount()];
        int componentCount = 0;

        for (int index = finishSize - 1; index >= 0; index--) {
            int root = finishOrder[index];
            if (componentOf[root] >= 0) {
                continue;
            }

            int size = 0;
            stack[size++] = root;
            componentOf[root] = componentCount;
            metrics.recordDepth(size);

            while (size > 0) {
                int vertex = stack[--size];
                for (WeightedEdge edge : reversed.neighbors(vertex)) {
                    metrics.recordEdgeScan();
                    int neighbor = edge.to();
                    if (componentOf[neighbor] < 0) {
                        componentOf[neighbor] = componentCount;
                        stack[size++] = neighbor;
                        metrics.recordDepth(size);
                    }
                }
            }
            componentCount++;
        }
        return componentCount;
    }
}
