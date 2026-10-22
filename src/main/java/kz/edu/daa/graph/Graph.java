package kz.edu.daa.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Weighted adjacency-list graph used by every algorithm in the assignment.
 * Undirected edges are stored once in {@link #edges()} and in both directions
 * in the adjacency lists.
 */
public final class Graph {
    private final int vertexCount;
    private final boolean directed;
    private final List<List<WeightedEdge>> adjacency;
    private final List<WeightedEdge> edges;
    private final int[] indegrees;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count must be non-negative");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        this.adjacency = new ArrayList<>(vertexCount);
        this.edges = new ArrayList<>();
        this.indegrees = new int[vertexCount];
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);

        WeightedEdge edge = new WeightedEdge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);

        if (directed) {
            indegrees[to]++;
        } else {
            adjacency.get(to).add(edge.reversed());
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<WeightedEdge> neighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    /** Returns every logical edge exactly once. */
    public List<WeightedEdge> edges() {
        return Collections.unmodifiableList(edges);
    }

    /** Returns a defensive copy, so Kahn may mutate it safely. */
    public int[] indegrees() {
        return indegrees.clone();
    }

    public Graph reversed() {
        Graph reversed = new Graph(vertexCount, directed);
        for (WeightedEdge edge : edges) {
            if (directed) {
                reversed.addEdge(edge.to(), edge.from(), edge.weight());
            } else {
                reversed.addEdge(edge.from(), edge.to(), edge.weight());
            }
        }
        return reversed;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException(
                    "Vertex " + vertex + " is outside 0.." + (vertexCount - 1));
        }
    }
}
