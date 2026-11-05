package graph;

public final class Edge {
    public final int id;
    public final int u;
    public final int v;
    public final int w;

    public Edge(int id, int u, int v, int w) {
        this.id = id;
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public int other(int x) {
        return x == u ? v : u;
    }

    @Override
    public String toString() {
        return u + " -> " + v + " (" + w + ")";
    }
}
