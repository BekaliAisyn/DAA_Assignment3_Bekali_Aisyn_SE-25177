package algorithms;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DsuTest {

    @Test
    void randomUnionsAgreeWithSimpleReference() {
        Random rnd = new Random(7);
        for (int round = 0; round < 50; round++) {
            int n = 1 + rnd.nextInt(300);
            DSU dsu = new DSU(n);
            int[] label = new int[n];
            for (int i = 0; i < n; i++) {
                label[i] = i;
            }
            int ops = rnd.nextInt(600);
            for (int k = 0; k < ops; k++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);
                boolean expectedMerge = label[a] != label[b];
                boolean merged = dsu.union(a, b);
                assertEquals(expectedMerge, merged);
                if (expectedMerge) {
                    int from = label[b];
                    int to = label[a];
                    for (int v = 0; v < n; v++) {
                        if (label[v] == from) {
                            label[v] = to;
                        }
                    }
                }
            }
            for (int k = 0; k < 400; k++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);
                assertEquals(label[a] == label[b], dsu.find(a) == dsu.find(b));
            }
            Set<Integer> distinct = new HashSet<>();
            for (int v = 0; v < n; v++) {
                distinct.add(label[v]);
            }
            assertEquals(distinct.size(), dsu.components());
        }
    }

    @Test
    void countersTrackFindsAndSuccessfulUnions() {
        DSU dsu = new DSU(10);
        assertEquals(10, dsu.components());
        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(1, 2));
        assertFalse(dsu.union(0, 2));
        dsu.find(5);
        dsu.find(6);
        dsu.find(7);
        assertEquals(2, dsu.unionCalls());
        assertEquals(3, dsu.findCalls());
        assertEquals(5, dsu.dsuOps());
        assertEquals(8, dsu.components());
    }

    @Test
    void makeSetResetsEverything() {
        DSU dsu = new DSU(5);
        dsu.union(0, 1);
        dsu.find(0);
        dsu.makeSet(7);
        assertEquals(7, dsu.components());
        assertEquals(0, dsu.dsuOps());
        assertFalse(dsu.find(0) == dsu.find(1));
    }

    @Test
    void longChainStaysFastWithBothHeuristics() {
        int n = 200_000;
        DSU dsu = new DSU(n);
        for (int i = 0; i + 1 < n; i++) {
            dsu.union(i, i + 1);
        }
        assertEquals(1, dsu.components());
        int root = dsu.find(0);
        for (int i = 0; i < n; i += 997) {
            assertEquals(root, dsu.find(i));
        }
    }
}
