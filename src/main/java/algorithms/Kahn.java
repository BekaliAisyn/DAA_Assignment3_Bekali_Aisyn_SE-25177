package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

public final class Kahn {
    private Kahn() {
    }

    public static TopoResult run(Graph g, Metrics m) {
        m.reset();
        int n = g.n();
        int[] indeg = g.inDegrees();
        int[] queue = new int[n];
        int head = 0;
        int tail = 0;
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                queue[tail++] = v;
            }
        }
        while (head < tail) {
            int u = queue[head++];
            for (Edge e : g.adj(u)) {
                m.edgeScans++;
                int v = e.other(u);
                if (--indeg[v] == 0) {
                    queue[tail++] = v;
                }
            }
        }
        return new TopoResult(queue, tail, tail < n);
    }

    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.n();
        if (order.length != n) {
            return false;
        }
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || pos[v] != -1) {
                return false;
            }
            pos[v] = i;
        }
        for (Edge e : g.edges()) {
            if (pos[e.u] >= pos[e.v]) {
                return false;
            }
        }
        return true;
    }
}
