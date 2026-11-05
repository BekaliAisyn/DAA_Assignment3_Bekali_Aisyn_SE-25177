package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Prim {
    private Prim() {
    }

    public static MstResult run(Graph g, Metrics m) {
        m.reset();
        int n = g.n();
        List<Edge> all = g.edges();
        boolean[] inTree = new boolean[n];
        List<Edge> tree = new ArrayList<>();
        PriorityQueue<Long> pq = new PriorityQueue<>((a, b) -> {
            m.comparisons++;
            return Long.compare(a, b);
        });
        long total = 0;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            push(g, start, inTree, pq, m);
            while (!pq.isEmpty()) {
                long item = pq.poll();
                Edge e = all.get((int) (item & 0xFFFFFFFFL));
                int v = inTree[e.u] ? e.v : e.u;
                if (inTree[v]) {
                    continue;
                }
                inTree[v] = true;
                tree.add(e);
                total += e.w;
                push(g, v, inTree, pq, m);
            }
        }
        return new MstResult(total, tree, components);
    }

    private static void push(Graph g, int x, boolean[] inTree, PriorityQueue<Long> pq, Metrics m) {
        for (Edge e : g.adj(x)) {
            m.edgeScans++;
            if (!inTree[e.other(x)]) {
                pq.add(((long) e.w << 32) | e.id);
            }
        }
    }
}
