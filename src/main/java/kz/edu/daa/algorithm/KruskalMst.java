package kz.edu.daa.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import kz.edu.daa.dsu.DisjointSetUnion;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.graph.WeightedEdge;
import kz.edu.daa.metrics.AlgorithmMetrics;
import kz.edu.daa.result.MstResult;

/** Kruskal's minimum spanning forest algorithm. */
public final class KruskalMst {
    private KruskalMst() {
    }

    public static MstResult compute(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        requireUndirected(graph);

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        List<WeightedEdge> sortedEdges = new ArrayList<>(graph.edges());
        sortedEdges.sort(countingComparator(metrics));

        DisjointSetUnion dsu = DisjointSetUnion.makeSet(graph.vertexCount(), metrics);
        List<WeightedEdge> selected = new ArrayList<>();
        long totalWeight = 0;

        for (WeightedEdge edge : sortedEdges) {
            if (dsu.components() == 1) {
                break;
            }
            metrics.recordEdgeScan();
            if (dsu.union(edge.from(), edge.to())) {
                selected.add(edge);
                totalWeight += edge.weight();
            }
        }

        return new MstResult(totalWeight, selected, dsu.components(), metrics.snapshot());
    }

    private static Comparator<WeightedEdge> countingComparator(AlgorithmMetrics metrics) {
        return (first, second) -> {
            metrics.recordWeightComparison();
            int byWeight = Integer.compare(first.weight(), second.weight());
            if (byWeight != 0) {
                return byWeight;
            }
            int firstLow = Math.min(first.from(), first.to());
            int secondLow = Math.min(second.from(), second.to());
            int byLowEndpoint = Integer.compare(firstLow, secondLow);
            if (byLowEndpoint != 0) {
                return byLowEndpoint;
            }
            int firstHigh = Math.max(first.from(), first.to());
            int secondHigh = Math.max(second.from(), second.to());
            return Integer.compare(firstHigh, secondHigh);
        };
    }

    private static void requireUndirected(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal requires an undirected graph");
        }
    }
}
