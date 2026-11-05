package algorithms;

public class DSU {
    private int[] parent;
    private int[] rank;
    private int components;
    private long findCalls;
    private long unionCalls;

    public DSU(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        components = n;
        findCalls = 0;
        unionCalls = 0;
    }

    public int find(int x) {
        findCalls++;
        return root(x);
    }

    public boolean union(int x, int y) {
        int a = root(x);
        int b = root(y);
        if (a == b) {
            return false;
        }
        if (rank[a] < rank[b]) {
            int t = a;
            a = b;
            b = t;
        }
        parent[b] = a;
        if (rank[a] == rank[b]) {
            rank[a]++;
        }
        components--;
        unionCalls++;
        return true;
    }

    public int components() {
        return components;
    }

    public long findCalls() {
        return findCalls;
    }

    public long unionCalls() {
        return unionCalls;
    }

    public long dsuOps() {
        return findCalls + unionCalls;
    }

    private int root(int x) {
        int r = x;
        while (parent[r] != r) {
            r = parent[r];
        }
        while (parent[x] != r) {
            int next = parent[x];
            parent[x] = r;
            x = next;
        }
        return r;
    }
}
