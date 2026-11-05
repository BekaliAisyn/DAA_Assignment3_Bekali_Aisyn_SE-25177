package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DagPathTest {

    private static long[] bellmanFord(Graph g) {
        int n = g.n();
        long inf = Long.MAX_VALUE;
        long[] dist = new long[n];
        Arrays.fill(dist, inf);
        int[] indeg = g.inDegrees();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                dist[v] = 0;
            }
        }
        for (int round = 0; round < n; round++) {
            boolean changed = false;
            for (Edge e : g.edges()) {
                if (dist[e.u] != inf && dist[e.u] + e.w < dist[e.v]) {
                    dist[e.v] = dist[e.u] + e.w;
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return dist;
    }

    @Test
    void matchesBellmanFordOnSmallRandomDagsWithNegativeWeights() {
        Random rnd = new Random(4242);
        for (int round = 0; round < 200; round++) {
            int n = 1 + rnd.nextInt(40);
            int m = rnd.nextInt(4 * n + 1);
            Graph g = TestGraphs.randomDag(rnd, n, m, -15, 25);
            int[] order = Kahn.run(g, new Metrics()).order();
            DagShortestPath sp = DagShortestPath.run(g, order, new Metrics());
            assertTrue(Arrays.equals(bellmanFord(g), sp.distances()));
        }
    }

    @Test
    void pipelineFileDistancesAndPaths() throws IOException {
        Graph g = GraphIO.read(Path.of("data", "jobs_pipeline_dag.txt"));
        int[] order = Kahn.run(g, new Metrics()).order();
        DagShortestPath sp = DagShortestPath.run(g, order, new Metrics());
        long[] expected = {0, 5, 3, 10, 7, 5, 0};
        assertTrue(Arrays.equals(expected, sp.distances()));
        assertEquals(30, sp.sumOfDistances());
        assertEquals(-1, sp.parent(0));
        assertEquals(-1, sp.parent(6));
        List<Integer> path = sp.pathTo(5);
        assertEquals(0, path.get(0).intValue());
        assertEquals(5, path.get(path.size() - 1).intValue());
        long walked = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            int a = path.get(i);
            int b = path.get(i + 1);
            long best = Long.MAX_VALUE;
            for (Edge e : g.adj(a)) {
                if (e.v == b) {
                    best = Math.min(best, e.w);
                }
            }
            walked += best;
        }
        assertEquals(sp.distance(5), walked);
    }

    @Test
    void everySourceStartsAtZero() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 2, 4);
        g.addEdge(1, 2, -3);
        g.addEdge(2, 3, 2);
        int[] order = Kahn.run(g, new Metrics()).order();
        DagShortestPath sp = DagShortestPath.run(g, order, new Metrics());
        assertEquals(0, sp.distance(0));
        assertEquals(0, sp.distance(1));
        assertEquals(0, sp.distance(4));
        assertEquals(-3, sp.distance(2));
        assertEquals(-1, sp.distance(3));
        assertEquals(List.of(1, 2, 3), sp.pathTo(3));
    }

    @Test
    void singleOnePassScansEachEdgeOnce() {
        Random rnd = new Random(3);
        Graph g = TestGraphs.randomDag(rnd, 400, 2000, 1, 1000);
        int[] order = Kahn.run(g, new Metrics()).order();
        Metrics m = new Metrics();
        DagShortestPath.run(g, order, m);
        assertEquals(g.m(), m.edgeScans);
        assertEquals(g.m(), m.comparisons);
    }

    @Test
    void rejectsAnIncompleteOrder() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPath.run(g, new int[]{0, 1}, new Metrics()));
    }
}
