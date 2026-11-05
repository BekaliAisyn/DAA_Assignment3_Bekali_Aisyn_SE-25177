package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopoTest {

    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(77);
        for (int round = 0; round < 50; round++) {
            int n = 1 + rnd.nextInt(300);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n + 1), 1, 20);
            Metrics m = new Metrics();
            TopoResult r = Kahn.run(g, m);
            assertEquals(false, r.hasCycle());
            int[] order = r.order();
            assertEquals(n, order.length);
            int[] pos = new int[n];
            for (int i = 0; i < n; i++) {
                pos[order[i]] = i;
            }
            for (Edge e : g.edges()) {
                assertTrue(pos[e.u] < pos[e.v]);
            }
            assertTrue(Kahn.isValidOrder(g, order));
            assertEquals(g.m(), m.edgeScans);
        }
    }

    @Test
    void cycleIsDetectedInsteadOfReturningAnOrder() throws IOException {
        Graph g = GraphIO.read(Path.of("data", "jobs_cyclic.txt"));
        TopoResult r = Kahn.run(g, new Metrics());
        assertTrue(r.hasCycle());
        assertTrue(r.processed() < g.n());
        assertThrows(IllegalStateException.class, r::order);
    }

    @Test
    void simpleCyclesAndSelfLoopAreDetected() {
        Graph triangle = new Graph(3, true);
        triangle.addEdge(0, 1, 1);
        triangle.addEdge(1, 2, 1);
        triangle.addEdge(2, 0, 1);
        assertTrue(Kahn.run(triangle, new Metrics()).hasCycle());

        Graph loop = new Graph(2, true);
        loop.addEdge(0, 1, 1);
        loop.addEdge(1, 1, 1);
        assertTrue(Kahn.run(loop, new Metrics()).hasCycle());
    }

    @Test
    void pipelineFileGivesValidOrder() throws IOException {
        Graph g = GraphIO.read(Path.of("data", "jobs_pipeline_dag.txt"));
        TopoResult r = Kahn.run(g, new Metrics());
        assertEquals(false, r.hasCycle());
        assertTrue(Kahn.isValidOrder(g, r.order()));
    }

    @Test
    void emptyGraphAndParallelEdges() {
        TopoResult empty = Kahn.run(new Graph(0, true), new Metrics());
        assertEquals(0, empty.order().length);

        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 1, 2);
        g.addEdge(1, 2, 3);
        assertTrue(Kahn.isValidOrder(g, Kahn.run(g, new Metrics()).order()));
    }

    @Test
    void validatorRejectsWrongOrders() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        assertEquals(false, Kahn.isValidOrder(g, new int[]{2, 1, 0}));
        assertEquals(false, Kahn.isValidOrder(g, new int[]{0, 1}));
        assertEquals(false, Kahn.isValidOrder(g, new int[]{0, 0, 2}));
        assertTrue(Kahn.isValidOrder(g, new int[]{0, 1, 2}));
    }
}
