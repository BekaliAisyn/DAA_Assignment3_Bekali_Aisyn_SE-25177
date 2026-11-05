package graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;

public final class GraphIO {
    private GraphIO() {
    }

    public static Graph read(Path path) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = nextLine(in);
            if (header == null) {
                throw new IOException("empty file: " + path);
            }
            StringTokenizer st = new StringTokenizer(header);
            if (st.countTokens() != 3) {
                throw new IOException("first line must be: n m directed|undirected");
            }
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            String kind = st.nextToken();
            boolean directed;
            if (kind.equals("directed")) {
                directed = true;
            } else if (kind.equals("undirected")) {
                directed = false;
            } else {
                throw new IOException("unknown graph kind: " + kind);
            }
            Graph g = new Graph(n, directed);
            for (int i = 0; i < m; i++) {
                String line = nextLine(in);
                if (line == null) {
                    throw new IOException("expected " + m + " edges but found " + i);
                }
                StringTokenizer et = new StringTokenizer(line);
                if (et.countTokens() != 3) {
                    throw new IOException("bad edge line: " + line);
                }
                int u = Integer.parseInt(et.nextToken());
                int v = Integer.parseInt(et.nextToken());
                int w = Integer.parseInt(et.nextToken());
                g.addEdge(u, v, w);
            }
            return g;
        }
    }

    public static void write(Graph g, Path path) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            out.write(g.n() + " " + g.m() + " " + (g.isDirected() ? "directed" : "undirected"));
            out.newLine();
            for (Edge e : g.edges()) {
                out.write(e.u + " " + e.v + " " + e.w);
                out.newLine();
            }
        }
    }

    private static String nextLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                return line;
            }
        }
        return null;
    }
}
