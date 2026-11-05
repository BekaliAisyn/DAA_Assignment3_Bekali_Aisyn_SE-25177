package benchmark;

import graph.Graph;

import java.util.Random;

public final class Generator {
    public static final long SEED = 42L;

    private Generator() {
    }

    public static Graph undirected(int n, int m) {
        Random rnd = new Random(SEED);
        int[] perm = shuffled(n, rnd);
        Graph g = new Graph(n, false);
        for (int i = 1; i < n; i++) {
            int parent = perm[rnd.nextInt(i)];
            g.addEdge(parent, perm[i], weight(rnd));
        }
        for (int k = n - 1; k < m; k++) {
            int[] ab = randomPair(n, rnd);
            g.addEdge(ab[0], ab[1], weight(rnd));
        }
        return g;
    }

    public static Graph directed(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, true);
        for (int k = 0; k < m; k++) {
            int[] ab = randomPair(n, rnd);
            g.addEdge(ab[0], ab[1], weight(rnd));
        }
        return g;
    }

    public static Graph dag(int n, int m) {
        Random rnd = new Random(SEED);
        int[] rank = shuffled(n, rnd);
        Graph g = new Graph(n, true);
        for (int k = 0; k < m; k++) {
            int[] ab = randomPair(n, rnd);
            int a = ab[0];
            int b = ab[1];
            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, weight(rnd));
        }
        return g;
    }

    private static int[] shuffled(int n, Random rnd) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            p[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = p[i];
            p[i] = p[j];
            p[j] = t;
        }
        return p;
    }

    private static int[] randomPair(int n, Random rnd) {
        int a = rnd.nextInt(n);
        int b = rnd.nextInt(n);
        while (b == a) {
            b = rnd.nextInt(n);
        }
        return new int[]{a, b};
    }

    private static int weight(Random rnd) {
        return 1 + rnd.nextInt(1000);
    }
}
