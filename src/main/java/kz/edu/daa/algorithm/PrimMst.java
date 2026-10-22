package kz.edu.daa.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.metrics.AlgorithmMetrics;
import kz.edu.daa.result.MstResult;

/** Best-key Prim with stale-entry skipping, restarted for each component. */
public final class PrimMst {
    private PrimMst() {
    }

    public static MstResult compute(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim requires an undirected graph");
        }

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        boolean[] visited = new boolean[graph.vertexCount()];
        int[] bestParent = new int[graph.vertexCount()];
        int[] bestWeight = new int[graph.vertexCount()];
        Arrays.fill(bestParent, -1);
        Arrays.fill(bestWeight, Integer.MAX_VALUE);

        PriorityQueue<Candidate> queue = new PriorityQueue<>(countingComparator(metrics));
        List<WeightedEdge> selected = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;

        for (int start = 0; start < graph.vertexCount(); start++) {
            if (visited[start]) {
                continue;
            }
            components++;
            bestWeight[start] = 0;
            bestParent[start] = -1;
            queue.add(new Candidate(start, -1, 0));

            while (!queue.isEmpty()) {
                Candidate candidate = queue.remove();
                int vertex = candidate.vertex();
                if (visited[vertex]
                        || candidate.weight() != bestWeight[vertex]
                        || candidate.parent() != bestParent[vertex]) {
                    continue;
                }

                visited[vertex] = true;
                if (candidate.parent() >= 0) {
                    selected.add(new WeightedEdge(candidate.parent(), vertex, candidate.weight()));
                    totalWeight += candidate.weight();
                }

                for (WeightedEdge edge : graph.neighbors(vertex)) {
                    metrics.recordEdgeScan();
                    int neighbor = edge.to();
                    if (!visited[neighbor] && isBetter(edge, bestWeight[neighbor], bestParent[neighbor], metrics)) {
                        bestWeight[neighbor] = edge.weight();
                        bestParent[neighbor] = vertex;
                        queue.add(new Candidate(neighbor, vertex, edge.weight()));
                    }
                }
            }
        }

        return new MstResult(totalWeight, selected, components, metrics.snapshot());
    }

    private static boolean isBetter(
            WeightedEdge edge,
            int currentWeight,
            int currentParent,
            AlgorithmMetrics metrics) {
        metrics.recordWeightComparison();
        int byWeight = Integer.compare(edge.weight(), currentWeight);
        if (byWeight != 0) {
            return byWeight < 0;
        }
        return currentParent < 0 || edge.from() < currentParent;
    }

    private static Comparator<Candidate> countingComparator(AlgorithmMetrics metrics) {
        return (first, second) -> {
            metrics.recordWeightComparison();
            int byWeight = Integer.compare(first.weight(), second.weight());
            if (byWeight != 0) {
                return byWeight;
            }
            int firstLow = Math.min(first.parent(), first.vertex());
            int secondLow = Math.min(second.parent(), second.vertex());
            int byLowEndpoint = Integer.compare(firstLow, secondLow);
            if (byLowEndpoint != 0) {
                return byLowEndpoint;
            }
            int firstHigh = Math.max(first.parent(), first.vertex());
            int secondHigh = Math.max(second.parent(), second.vertex());
            int byHighEndpoint = Integer.compare(firstHigh, secondHigh);
            if (byHighEndpoint != 0) {
                return byHighEndpoint;
            }
            return Integer.compare(first.vertex(), second.vertex());
        };
    }

    private record Candidate(int vertex, int parent, int weight) {
    }
}
