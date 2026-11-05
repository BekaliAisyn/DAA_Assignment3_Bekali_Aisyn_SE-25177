package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.Random;

final class TestGraphs {
    private TestGraphs() {
    }

    static Graph randomConnectedUndirected(Random rnd, int n, int extraEdges) {
        Graph g = new Graph(n, false);
        for (int v = 1; v < n; v++) {
            g.addEdge(rnd.nextInt(v), v, 1 + rnd.nextInt(50));
        }
        for (int i = 0; i < extraEdges; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(50));
        }
        return g;
    }

    static Graph randomDirected(Random rnd, int n, int m) {
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(20));
        }
        return g;
    }

    static Graph randomDag(Random rnd, int n, int m, int lowWeight, int highWeight) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = rank[i];
            rank[i] = rank[j];
            rank[j] = t;
        }
        Graph g = new Graph(n, true);
        if (n < 2) {
            return g;
        }
        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            while (b == a) {
                b = rnd.nextInt(n);
            }
            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, lowWeight + rnd.nextInt(highWeight - lowWeight + 1));
        }
        return g;
    }

    static Graph directedPath(int n) {
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) {
            g.addEdge(i, i + 1, 1);
        }
        return g;
    }

    static long bruteForceMstWeight(Graph g) {
        int n = g.n();
        int m = g.m();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != n - 1) {
                continue;
            }
            int[] comp = new int[n];
            for (int i = 0; i < n; i++) {
                comp[i] = i;
            }
            long sum = 0;
            boolean ok = true;
            for (int i = 0; i < m && ok; i++) {
                if ((mask & (1 << i)) == 0) {
                    continue;
                }
                Edge e = g.edges().get(i);
                int a = comp[e.u];
                int b = comp[e.v];
                if (a == b) {
                    ok = false;
                    break;
                }
                for (int v = 0; v < n; v++) {
                    if (comp[v] == b) {
                        comp[v] = a;
                    }
                }
                sum += e.w;
            }
            if (ok && sum < best) {
                best = sum;
            }
        }
        return best;
    }
}
