package algorithms;

import graph.Edge;

import java.util.List;

public final class MstResult {
    private final long totalWeight;
    private final List<Edge> edges;
    private final int components;

    public MstResult(long totalWeight, List<Edge> edges, int components) {
        this.totalWeight = totalWeight;
        this.edges = edges;
        this.components = components;
    }

    public long totalWeight() {
        return totalWeight;
    }

    public List<Edge> edges() {
        return edges;
    }

    public int edgeCount() {
        return edges.size();
    }

    public int components() {
        return components;
    }
}
