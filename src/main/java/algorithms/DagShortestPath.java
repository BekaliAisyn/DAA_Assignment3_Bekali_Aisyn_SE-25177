package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DagShortestPath {
    public static final long INF = Long.MAX_VALUE;

    private final long[] dist;
    private final int[] parent;

    private DagShortestPath(long[] dist, int[] parent) {
        this.dist = dist;
        this.parent = parent;
    }

    public static DagShortestPath run(Graph g, int[] order, Metrics m) {
        m.reset();
        int n = g.n();
        if (order.length != n) {
            throw new IllegalArgumentException("order must contain every vertex once");
        }
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        int[] indeg = g.inDegrees();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                dist[v] = 0;
            }
        }
        for (int u : order) {
            if (dist[u] == INF) {
                continue;
            }
            for (Edge e : g.adj(u)) {
                m.edgeScans++;
                int v = e.v;
                long candidate = dist[u] + e.w;
                m.comparisons++;
                if (candidate < dist[v]) {
                    dist[v] = candidate;
                    parent[v] = u;
                }
            }
        }
        return new DagShortestPath(dist, parent);
    }

    public long distance(int v) {
        return dist[v];
    }

    public long[] distances() {
        return dist.clone();
    }

    public int parent(int v) {
        return parent[v];
    }

    public long sumOfDistances() {
        long sum = 0;
        for (long d : dist) {
            if (d != INF) {
                sum += d;
            }
        }
        return sum;
    }

    public List<Integer> pathTo(int v) {
        if (dist[v] == INF) {
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        for (int x = v; x != -1; x = parent[x]) {
            path.add(x);
        }
        Collections.reverse(path);
        return path;
    }
}
