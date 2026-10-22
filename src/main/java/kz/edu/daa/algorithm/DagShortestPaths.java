package kz.edu.daa.algorithm;

import java.util.Arrays;
import java.util.Objects;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.metrics.AlgorithmMetrics;
import kz.edu.daa.result.ShortestPathResult;
import kz.edu.daa.result.TopologicalResult;

/** Shortest paths in a DAG from a virtual zero-weight super-source. */
public final class DagShortestPaths {
    private DagShortestPaths() {
    }

    public static ShortestPathResult compute(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("DAG shortest paths require a directed graph");
        }

        TopologicalResult topological = KahnTopologicalSort.compute(graph);
        if (topological.hasCycle()) {
            throw new IllegalArgumentException("Shortest paths require an acyclic graph");
        }

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        long[] distances = new long[graph.vertexCount()];
        int[] parents = new int[graph.vertexCount()];
        Arrays.fill(distances, ShortestPathResult.INF);
        Arrays.fill(parents, -1);

        int[] indegrees = graph.indegrees();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            if (indegrees[vertex] == 0) {
                distances[vertex] = 0;
            }
        }

        for (int vertex : topological.order()) {
            for (WeightedEdge edge : graph.neighbors(vertex)) {
                metrics.recordEdgeScan();
                if (distances[vertex] == ShortestPathResult.INF) {
                    continue;
                }
                long candidate = distances[vertex] + edge.weight();
                metrics.recordWeightComparison();
                if (candidate < distances[edge.to()]) {
                    distances[edge.to()] = candidate;
                    parents[edge.to()] = vertex;
                }
            }
        }

        return new ShortestPathResult(distances, parents, metrics.snapshot());
    }
}
