package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import algorithms.SccResult;
import algorithms.TopoResult;
import graph.Graph;
import metrics.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class Benchmark {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final int[] DENSITY = {4, 20};
    private static final String[] DENSITY_NAME = {"sparse", "dense"};
    private static final int RUNS = 5;

    private static final long[] REF_MST = {
            147970L, 30363L, 468911L, 97589L, 1486139L,
            309550L, 4715515L, 968517L, 15012094L, 3056681L};
    private static final int[] REF_SCC = {27, 1, 137, 1, 373, 1, 1188, 1, 3923, 1};
    private static final long[] REF_DAG = {
            396274L, 252411L, 1271911L, 723157L, 3941828L,
            2381342L, 12551891L, 7376612L, 40096539L, 23512087L};

    private static final class Row {
        final String algorithm;
        final String graph;
        final int n;
        final int m;
        final double timeMs;
        final Metrics metrics;
        final long result;

        Row(String algorithm, String graph, int n, int m, double timeMs, Metrics metrics, long result) {
            this.algorithm = algorithm;
            this.graph = graph;
            this.n = n;
            this.m = m;
            this.timeMs = timeMs;
            this.metrics = metrics;
            this.result = result;
        }
    }

    private static final class Timed<T> {
        final T value;
        final double medianMs;
        final Metrics metrics;

        Timed(T value, double medianMs, Metrics metrics) {
            this.value = value;
            this.medianMs = medianMs;
            this.metrics = metrics;
        }
    }

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results");
        Files.createDirectories(outDir);
        List<Row> rows = new ArrayList<>();
        int index = 0;
        System.out.printf("%-14s %12s %12s %8s %8s %14s %14s%n",
                "graph", "MST weight", "reference", "SCC", "ref", "DAG dist sum", "reference");
        for (int n : SIZES) {
            for (int d = 0; d < DENSITY.length; d++) {
                int m = DENSITY[d] * n;
                String name = "n" + n + "_" + DENSITY_NAME[d];
                runGraph(name, n, m, index, rows);
                index++;
            }
        }
        Path csv = outDir.resolve("results.csv");
        writeCsv(csv, rows);
        System.out.println();
        System.out.println("All checks passed. " + rows.size() + " rows written to " + csv);
    }

    private static void runGraph(String name, int n, int m, int index, List<Row> rows) {
        Graph u = Generator.undirected(n, m);
        Timed<MstResult> kruskal = time(() -> new Metrics(), (met) -> Kruskal.run(u, met));
        Timed<MstResult> prim = time(() -> new Metrics(), (met) -> Prim.run(u, met));
        long kw = kruskal.value.totalWeight();
        long pw = prim.value.totalWeight();
        if (kw != pw) {
            throw new IllegalStateException(name + ": Kruskal " + kw + " != Prim " + pw);
        }
        if (kruskal.value.components() != 1 || prim.value.components() != 1) {
            throw new IllegalStateException(name + ": generated graph should be connected");
        }
        if (kruskal.value.edgeCount() != n - 1 || prim.value.edgeCount() != n - 1) {
            throw new IllegalStateException(name + ": MST must have n-1 edges");
        }
        if (kw != REF_MST[index]) {
            throw new IllegalStateException(name + ": MST weight " + kw + " differs from reference " + REF_MST[index]);
        }
        rows.add(new Row("Kruskal", name, n, u.m(), kruskal.medianMs, kruskal.metrics, kw));
        rows.add(new Row("Prim", name, n, u.m(), prim.medianMs, prim.metrics, pw));

        Graph dir = Generator.directed(n, m);
        Timed<SccResult> scc = time(() -> new Metrics(), (met) -> Kosaraju.run(dir, met));
        int sccCount = scc.value.count();
        if (sccCount != REF_SCC[index]) {
            throw new IllegalStateException(name + ": SCC count " + sccCount + " differs from reference " + REF_SCC[index]);
        }
        if (scc.metrics.edgeScans != 2L * dir.m()) {
            throw new IllegalStateException(name + ": Kosaraju must scan 2m edges");
        }
        rows.add(new Row("Kosaraju", name, n, dir.m(), scc.medianMs, scc.metrics, sccCount));

        Graph dag = Generator.dag(n, m);
        Timed<TopoResult> kahn = time(() -> new Metrics(), (met) -> Kahn.run(dag, met));
        if (kahn.value.hasCycle() || kahn.value.processed() != n) {
            throw new IllegalStateException(name + ": generated DAG reported a cycle");
        }
        int[] order = kahn.value.order();
        if (!Kahn.isValidOrder(dag, order)) {
            throw new IllegalStateException(name + ": topological order is not valid");
        }
        if (kahn.metrics.edgeScans != dag.m()) {
            throw new IllegalStateException(name + ": Kahn must scan m edges");
        }
        rows.add(new Row("Kahn", name, n, dag.m(), kahn.medianMs, kahn.metrics, kahn.value.processed()));

        Timed<DagShortestPath> sp = time(() -> new Metrics(), (met) -> DagShortestPath.run(dag, order, met));
        long sum = sp.value.sumOfDistances();
        if (sp.metrics.edgeScans != dag.m()) {
            throw new IllegalStateException(name + ": DAG relaxation must scan m edges");
        }
        if (sum != REF_DAG[index]) {
            throw new IllegalStateException(name + ": distance sum " + sum + " differs from reference " + REF_DAG[index]);
        }
        rows.add(new Row("DagShortestPath", name, n, dag.m(), sp.medianMs, sp.metrics, sum));

        System.out.printf("%-14s %12d %12d %8d %8d %14d %14d%n",
                name, kw, REF_MST[index], sccCount, REF_SCC[index], sum, REF_DAG[index]);
    }

    private interface Run<T> {
        T apply(Metrics m);
    }

    private static <T> Timed<T> time(Supplier<Metrics> fresh, Run<T> run) {
        double[] times = new double[RUNS];
        T value = null;
        Metrics last = null;
        for (int i = 0; i < RUNS; i++) {
            Metrics met = fresh.get();
            long start = System.nanoTime();
            value = run.apply(met);
            times[i] = (System.nanoTime() - start) / 1_000_000.0;
            last = met;
        }
        Arrays.sort(times);
        return new Timed<>(value, times[RUNS / 2], last);
    }

    private static void writeCsv(Path path, List<Row> rows) throws IOException {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            out.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            for (Row r : rows) {
                out.println(String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                        r.algorithm, r.graph, r.n, r.m, r.timeMs,
                        r.metrics.dsuOps, r.metrics.edgeScans, r.metrics.comparisons,
                        r.metrics.maxDepth, r.result));
            }
        }
    }
}
