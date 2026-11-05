package algorithms;

import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MstTest {

    @Test
    void kruskalAndPrimAgreeOnHundredRandomConnectedGraphs() {
        Random rnd = new Random(2024);
        for (int round = 0; round < 100; round++) {
            int n = 2 + rnd.nextInt(80);
            int extra = rnd.nextInt(4 * n);
            Graph g = TestGraphs.randomConnectedUndirected(rnd, n, extra);
            MstResult k = Kruskal.run(g, new Metrics());
            MstResult p = Prim.run(g, new Metrics());
            assertEquals(k.totalWeight(), p.totalWeight());
            assertEquals(n - 1, k.edgeCount());
            assertEquals(n - 1, p.edgeCount());
            assertEquals(1, k.components());
            assertEquals(1, p.components());
        }
    }

    @Test
    void matchesBruteForceOnTinyGraphs() {
        Random rnd = new Random(99);
        for (int round = 0; round < 60; round++) {
            int n = 2 + rnd.nextInt(5);
            int extra = rnd.nextInt(5);
            Graph g = TestGraphs.randomConnectedUndirected(rnd, n, extra);
            long expected = TestGraphs.bruteForceMstWeight(g);
            assertEquals(expected, Kruskal.run(g, new Metrics()).totalWeight());
            assertEquals(expected, Prim.run(g, new Metrics()).totalWeight());
        }
    }

    @Test
    void smallRoadNetworkFromFile() throws IOException {
        Graph g = GraphIO.read(Path.of("data", "city_roads_small.txt"));
        assertEquals(33, Kruskal.run(g, new Metrics()).totalWeight());
        assertEquals(33, Prim.run(g, new Metrics()).totalWeight());
    }

    @Test
    void disconnectedGraphGivesForestAndDoesNotThrow() throws IOException {
        Graph g = GraphIO.read(Path.of("data", "city_roads_disconnected.txt"));
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(4, k.components());
        assertEquals(4, p.components());
        assertEquals(15, k.totalWeight());
        assertEquals(15, p.totalWeight());
        assertEquals(g.n() - 4, k.edgeCount());
        assertEquals(g.n() - 4, p.edgeCount());
    }

    @Test
    void selfLoopsParallelEdgesAndIsolatedVertices() {
        Graph g = new Graph(5, false);
        g.addEdge(0, 0, 1);
        g.addEdge(0, 1, 9);
        g.addEdge(0, 1, 4);
        g.addEdge(1, 2, 6);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 2, 1);
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(6, k.totalWeight());
        assertEquals(6, p.totalWeight());
        assertEquals(3, k.components());
        assertEquals(3, p.components());
    }

    @Test
    void emptyAndSingleVertexGraphs() {
        Graph none = new Graph(0, false);
        Graph one = new Graph(1, false);
        assertDoesNotThrow(() -> Kruskal.run(none, new Metrics()));
        assertDoesNotThrow(() -> Prim.run(none, new Metrics()));
        assertEquals(0, Kruskal.run(one, new Metrics()).totalWeight());
        assertEquals(1, Prim.run(one, new Metrics()).components());
    }

    @Test
    void countersAreFilledInsideTheAlgorithms() {
        Random rnd = new Random(5);
        Graph g = TestGraphs.randomConnectedUndirected(rnd, 200, 800);
        Metrics km = new Metrics();
        Metrics pm = new Metrics();
        Kruskal.run(g, km);
        Prim.run(g, pm);
        assertEquals(true, km.dsuOps > 0 && km.comparisons > 0 && km.edgeScans > 0);
        assertEquals(true, km.edgeScans <= g.m());
        long adjacencyEntries = 0;
        for (int v = 0; v < g.n(); v++) {
            adjacencyEntries += g.adj(v).size();
        }
        assertEquals(adjacencyEntries, pm.edgeScans);
        assertEquals(0, pm.dsuOps);
    }
}
