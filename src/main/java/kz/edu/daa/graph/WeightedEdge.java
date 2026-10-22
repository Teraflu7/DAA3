package kz.edu.daa.graph;

/** A weighted edge whose endpoints are numbered from zero. */
public record WeightedEdge(int from, int to, int weight) {
    public WeightedEdge {
        if (from < 0 || to < 0) {
            throw new IllegalArgumentException("Edge endpoints must be non-negative");
        }
    }

    public WeightedEdge reversed() {
        return new WeightedEdge(to, from, weight);
    }
}
