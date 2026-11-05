package graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphIOTest {

    @Test
    void readsTheSmallInputFiles() throws IOException {
        Graph roads = GraphIO.read(Path.of("data", "city_roads_disconnected.txt"));
        assertEquals(9, roads.n());
        assertEquals(8, roads.m());
        assertEquals(false, roads.isDirected());

        Graph cyclic = GraphIO.read(Path.of("data", "jobs_cyclic.txt"));
        assertEquals(8, cyclic.n());
        assertEquals(10, cyclic.m());
        assertTrue(cyclic.isDirected());

        Graph dag = GraphIO.read(Path.of("data", "jobs_pipeline_dag.txt"));
        assertEquals(7, dag.n());
        assertEquals(9, dag.m());
    }

    @Test
    void writeThenReadGivesTheSameGraph() throws IOException {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 1, -2);
        g.addEdge(0, 1, 7);
        Path tmp = Files.createTempFile("graph", ".txt");
        try {
            GraphIO.write(g, tmp);
            Graph back = GraphIO.read(tmp);
            assertEquals(g.n(), back.n());
            assertEquals(g.m(), back.m());
            assertEquals(g.isDirected(), back.isDirected());
            for (int i = 0; i < g.m(); i++) {
                assertEquals(g.edges().get(i).u, back.edges().get(i).u);
                assertEquals(g.edges().get(i).v, back.edges().get(i).v);
                assertEquals(g.edges().get(i).w, back.edges().get(i).w);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void badInputIsReportedAsAnException() throws IOException {
        Path tmp = Files.createTempFile("bad", ".txt");
        try {
            Files.writeString(tmp, "3 1 sideways\n0 1 2\n", StandardCharsets.UTF_8);
            assertThrows(IOException.class, () -> GraphIO.read(tmp));
            Files.writeString(tmp, "3 2 directed\n0 1 2\n", StandardCharsets.UTF_8);
            assertThrows(IOException.class, () -> GraphIO.read(tmp));
            Files.writeString(tmp, "3 1 directed\n0 5 2\n", StandardCharsets.UTF_8);
            assertThrows(IllegalArgumentException.class, () -> GraphIO.read(tmp));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void undirectedEdgeAppearsInBothAdjacencyLists() {
        Graph g = new Graph(3, false);
        g.addEdge(0, 1, 4);
        g.addEdge(2, 2, 1);
        assertEquals(1, g.adj(0).size());
        assertEquals(1, g.adj(1).size());
        assertEquals(1, g.adj(2).size());
        assertEquals(2, g.m());
    }
}
