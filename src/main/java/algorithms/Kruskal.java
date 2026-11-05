package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Kruskal {
    private Kruskal() {
    }

    public static MstResult run(Graph g, Metrics m) {
        m.reset();
        Edge[] sorted = g.edges().toArray(new Edge[0]);
        Arrays.sort(sorted, (a, b) -> {
            m.comparisons++;
            return Integer.compare(a.w, b.w);
        });
        DSU dsu = new DSU(g.n());
        List<Edge> tree = new ArrayList<>();
        long total = 0;
        for (Edge e : sorted) {
            if (dsu.components() <= 1) {
                break;
            }
            m.edgeScans++;
            if (dsu.find(e.u) != dsu.find(e.v)) {
                dsu.union(e.u, e.v);
                tree.add(e);
                total += e.w;
            }
        }
        m.dsuOps = dsu.dsuOps();
        return new MstResult(total, tree, dsu.components());
    }
}
