package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SccTest {

    private static boolean[][] reachable(Graph g) {
        int n = g.n();
        boolean[][] reach = new boolean[n][n];
        for (int s = 0; s < n; s++) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            reach[s][s] = true;
            queue.add(s);
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (Edge e : g.adj(u)) {
                    if (!reach[s][e.v]) {
                        reach[s][e.v] = true;
                        queue.add(e.v);
                    }
                }
            }
        }
        return reach;
    }

    @Test
    void matchesBruteForceOnRandomGraphs() {
        Random rnd = new Random(31);
        for (int round = 0; round < 60; round++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n + 1);
            Graph g = TestGraphs.randomDirected(rnd, n, m);
            boolean[][] fwd = reachable(g);
            boolean[][] bwd = reachable(g.reversed());
            SccResult r = Kosaraju.run(g, new Metrics());
            int[] comp = r.component();
            int bruteCount = 0;
            boolean[] counted = new boolean[n];
            for (int u = 0; u < n; u++) {
                if (!counted[u]) {
                    bruteCount++;
                    for (int v = 0; v < n; v++) {
                        if (fwd[u][v] && bwd[u][v]) {
                            counted[v] = true;
                        }
                    }
                }
            }
            assertEquals(bruteCount, r.count());
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    boolean same = fwd[u][v] && bwd[u][v];
                    assertEquals(same, comp[u] == comp[v]);
                }
            }
        }
    }

    @Test
    void cyclicFileHasFourComponents() throws IOException {
        Graph g = GraphIO.read(Path.of("data", "jobs_cyclic.txt"));
        SccResult r = Kosaraju.run(g, new Metrics());
        assertEquals(4, r.count());
        int[] c = r.component();
        assertEquals(c[0], c[1]);
        assertEquals(c[1], c[2]);
        assertEquals(c[3], c[4]);
        assertEquals(c[4], c[5]);
        assertEquals(false, c[2] == c[3]);
        assertEquals(false, c[6] == c[7]);
    }

    @Test
    void everyVertexOfADagIsItsOwnComponent() {
        Random rnd = new Random(8);
        Graph g = TestGraphs.randomDag(rnd, 150, 600, 1, 9);
        assertEquals(150, Kosaraju.run(g, new Metrics()).count());
    }

    @Test
    void isolatedVerticesSelfLoopsAndParallelEdges() {
        Graph g = new Graph(6, true);
        g.addEdge(0, 0, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 1, 1);
        g.addEdge(2, 1, 1);
        assertEquals(5, Kosaraju.run(g, new Metrics()).count());
        assertEquals(0, Kosaraju.run(new Graph(0, true), new Metrics()).count());
    }

    @Test
    void scansExactlyTwoMEdges() {
        Random rnd = new Random(12);
        Graph g = TestGraphs.randomDirected(rnd, 500, 2500);
        Metrics m = new Metrics();
        Kosaraju.run(g, m);
        assertEquals(2L * g.m(), m.edgeScans);
        assertEquals(true, m.maxDepth >= 1 && m.maxDepth <= g.n());
    }
}
