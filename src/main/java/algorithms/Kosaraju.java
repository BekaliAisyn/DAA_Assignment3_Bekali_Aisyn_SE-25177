package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;
import java.util.List;

public final class Kosaraju {
    private Kosaraju() {
    }

    public static SccResult run(Graph g, Metrics m) {
        m.reset();
        int n = g.n();
        int[] finishOrder = firstPass(g, m);
        Graph reversed = g.reversed();
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int[] stack = new int[n];
        int[] next = new int[n];
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = finishOrder[i];
            if (component[s] != -1) {
                continue;
            }
            int sp = 0;
            component[s] = count;
            stack[sp++] = s;
            m.depth(sp);
            while (sp > 0) {
                int u = stack[sp - 1];
                List<Edge> list = reversed.adj(u);
                if (next[u] < list.size()) {
                    Edge e = list.get(next[u]++);
                    m.edgeScans++;
                    int v = e.other(u);
                    if (component[v] == -1) {
                        component[v] = count;
                        stack[sp++] = v;
                        m.depth(sp);
                    }
                } else {
                    sp--;
                }
            }
            count++;
        }
        return new SccResult(component, count);
    }

    private static int[] firstPass(Graph g, Metrics m) {
        int n = g.n();
        boolean[] seen = new boolean[n];
        int[] order = new int[n];
        int[] stack = new int[n];
        int[] next = new int[n];
        int done = 0;
        for (int s = 0; s < n; s++) {
            if (seen[s]) {
                continue;
            }
            int sp = 0;
            seen[s] = true;
            stack[sp++] = s;
            m.depth(sp);
            while (sp > 0) {
                int u = stack[sp - 1];
                List<Edge> list = g.adj(u);
                if (next[u] < list.size()) {
                    Edge e = list.get(next[u]++);
                    m.edgeScans++;
                    int v = e.other(u);
                    if (!seen[v]) {
                        seen[v] = true;
                        stack[sp++] = v;
                        m.depth(sp);
                    }
                } else {
                    order[done++] = u;
                    sp--;
                }
            }
        }
        return order;
    }
}
