package algorithms;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StackSafetyTest {

    @Test
    void kosarajuOnAPathOfOneHundredThousandVertices() {
        int n = 100_000;
        Graph g = TestGraphs.directedPath(n);
        Metrics m = new Metrics();
        SccResult r = Kosaraju.run(g, m);
        assertEquals(n, r.count());
        assertEquals(n, m.maxDepth);
        assertEquals(2L * g.m(), m.edgeScans);
    }

    @Test
    void kosarajuWorksEvenInAThreadWithATinyJvmStack() throws InterruptedException {
        int n = 100_000;
        Graph g = TestGraphs.directedPath(n);
        Throwable[] failure = new Throwable[1];
        int[] count = new int[1];
        Thread t = new Thread(null, () -> {
            try {
                count[0] = Kosaraju.run(g, new Metrics()).count();
            } catch (Throwable e) {
                failure[0] = e;
            }
        }, "tiny-stack", 128 * 1024);
        t.start();
        t.join();
        assertNull(failure[0]);
        assertEquals(n, count[0]);
    }

    @Test
    void oneBigCycleGivesOneComponent() {
        int n = 100_000;
        Graph g = TestGraphs.directedPath(n);
        g.addEdge(n - 1, 0, 1);
        Metrics m = new Metrics();
        assertEquals(1, Kosaraju.run(g, m).count());
        assertEquals(n, m.maxDepth);
    }

    @Test
    void kahnOnALongPath() {
        int n = 100_000;
        Graph g = TestGraphs.directedPath(n);
        TopoResult r = Kahn.run(g, new Metrics());
        assertEquals(n, r.processed());
        assertEquals(false, r.hasCycle());
    }
}
