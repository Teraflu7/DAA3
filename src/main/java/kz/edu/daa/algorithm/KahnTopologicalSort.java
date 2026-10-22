package kz.edu.daa.algorithm;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.metrics.AlgorithmMetrics;
import kz.edu.daa.result.TopologicalResult;

/** Topological sorting with in-degrees and a queue. */
public final class KahnTopologicalSort {
    private KahnTopologicalSort() {
    }

    public static TopologicalResult compute(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        requireDirected(graph);

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        int[] indegrees = graph.indegrees();
        Queue<Integer> ready = new ArrayDeque<>();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            if (indegrees[vertex] == 0) {
                ready.add(vertex);
            }
        }

        int[] order = new int[graph.vertexCount()];
        int processed = 0;
        while (!ready.isEmpty()) {
            int vertex = ready.remove();
            order[processed++] = vertex;
            for (WeightedEdge edge : graph.neighbors(vertex)) {
                metrics.recordEdgeScan();
                if (--indegrees[edge.to()] == 0) {
                    ready.add(edge.to());
                }
            }
        }

        if (processed != graph.vertexCount()) {
            return new TopologicalResult(true, new int[0], metrics.snapshot());
        }
        return new TopologicalResult(false, order, metrics.snapshot());
    }

    public static boolean isValidOrder(Graph graph, int[] order) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(order, "order");
        requireDirected(graph);
        if (order.length != graph.vertexCount()) {
            return false;
        }

        int[] position = new int[graph.vertexCount()];
        Arrays.fill(position, -1);
        for (int index = 0; index < order.length; index++) {
            int vertex = order[index];
            if (vertex < 0 || vertex >= graph.vertexCount() || position[vertex] >= 0) {
                return false;
            }
            position[vertex] = index;
        }
        for (WeightedEdge edge : graph.edges()) {
            if (position[edge.from()] >= position[edge.to()]) {
                return false;
            }
        }
        return true;
    }

    private static void requireDirected(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sorting requires a directed graph");
        }
    }
}
