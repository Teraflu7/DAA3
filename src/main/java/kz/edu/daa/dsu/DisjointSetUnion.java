package kz.edu.daa.dsu;

import java.util.Objects;
import kz.edu.daa.metrics.AlgorithmMetrics;
import kz.edu.daa.metrics.MetricsSnapshot;

/** Disjoint-set union with union by size and full path compression. */
public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] size;
    private final AlgorithmMetrics metrics;
    private int components;

    private DisjointSetUnion(int elementCount, AlgorithmMetrics metrics) {
        if (elementCount < 0) {
            throw new IllegalArgumentException("Element count must be non-negative");
        }
        this.metrics = Objects.requireNonNull(metrics, "metrics");
        this.parent = new int[elementCount];
        this.size = new int[elementCount];
        this.components = elementCount;
        for (int element = 0; element < elementCount; element++) {
            parent[element] = element;
            size[element] = 1;
        }
    }

    public static DisjointSetUnion makeSet(int elementCount) {
        return new DisjointSetUnion(elementCount, new AlgorithmMetrics());
    }

    public static DisjointSetUnion makeSet(int elementCount, AlgorithmMetrics metrics) {
        return new DisjointSetUnion(elementCount, metrics);
    }

    public int find(int element) {
        checkElement(element);
        metrics.recordFindCall();

        int root = element;
        while (root != parent[root]) {
            root = parent[root];
        }
        while (element != root) {
            int next = parent[element];
            parent[element] = root;
            element = next;
        }
        return root;
    }

    public boolean union(int first, int second) {
        int firstRoot = find(first);
        int secondRoot = find(second);
        if (firstRoot == secondRoot) {
            return false;
        }

        if (size[firstRoot] < size[secondRoot]) {
            int temporary = firstRoot;
            firstRoot = secondRoot;
            secondRoot = temporary;
        }
        parent[secondRoot] = firstRoot;
        size[firstRoot] += size[secondRoot];
        components--;
        metrics.recordSuccessfulUnion();
        return true;
    }

    public boolean connected(int first, int second) {
        return find(first) == find(second);
    }

    public int components() {
        return components;
    }

    public int sizeOf(int element) {
        return size[find(element)];
    }

    public MetricsSnapshot metrics() {
        return metrics.snapshot();
    }

    private void checkElement(int element) {
        if (element < 0 || element >= parent.length) {
            throw new IndexOutOfBoundsException(
                    "Element " + element + " is outside 0.." + (parent.length - 1));
        }
    }
}
