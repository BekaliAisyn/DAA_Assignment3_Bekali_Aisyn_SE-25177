package graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final int n;
    private final boolean directed;
    private final List<Edge>[] adj;
    private final List<Edge> edges = new ArrayList<>();
    private final int[] inDegree;

    @SuppressWarnings("unchecked")
    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        this.n = n;
        this.directed = directed;
        this.adj = new List[n];
        for (int i = 0; i < n; i++) {
            adj[i] = new ArrayList<>(0);
        }
        this.inDegree = new int[n];
    }

    public Edge addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n) {
            throw new IllegalArgumentException("vertex out of range: " + u + " " + v);
        }
        Edge e = new Edge(edges.size(), u, v, w);
        edges.add(e);
        adj[u].add(e);
        inDegree[v]++;
        if (!directed && u != v) {
            adj[v].add(e);
            inDegree[u]++;
        }
        return e;
    }

    public int n() {
        return n;
    }

    public int m() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> adj(int u) {
        return adj[u];
    }

    public List<Edge> edges() {
        return edges;
    }

    public int[] inDegrees() {
        return inDegree.clone();
    }

    public Graph reversed() {
        Graph r = new Graph(n, directed);
        for (Edge e : edges) {
            if (directed) {
                r.addEdge(e.v, e.u, e.w);
            } else {
                r.addEdge(e.u, e.v, e.w);
            }
        }
        return r;
    }
}
