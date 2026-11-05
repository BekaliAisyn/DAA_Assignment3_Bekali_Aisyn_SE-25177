# DAA Assignment 3: City Network & Job Scheduler

Graph project in Java for the Design and Analysis of Algorithms course. It has my own graph class and six algorithms written from scratch (only `ArrayList`, `ArrayDeque`, `PriorityQueue` and `Arrays.sort` come from the standard library):

- DSU with union by rank and path compression
- Kruskal and Prim (minimum spanning tree / forest)
- Kosaraju (strongly connected components) with an iterative DFS
- Kahn (topological order and cycle detection)
- Shortest paths in a DAG (one pass in topological order, negative weights allowed)

Every algorithm counts its own work (`dsuOps`, `edgeScans`, `comparisons`, `maxDepth`) so the results do not depend on how fast my laptop is. The analysis is in [REPORT.md](REPORT.md).

## What you need

- JDK 17 or newer (I ran it on JDK 21)
- Maven 3.9 or newer for the normal build
- Python 3 with `matplotlib`, only for the plots

## Build and test

```
mvn clean package
```

This compiles the code, runs all JUnit 5 tests and builds `target/daa-assignment3-1.0.jar`. To run only the tests:

```
mvn test
```

The tests read the small graphs from `data/`, so run Maven from the project root.

## Run the benchmark

```
java -Xmx3g -jar target/daa-assignment3-1.0.jar
```

It generates the ten graphs (five sizes, sparse and dense, three kinds each) with `new Random(42)`, runs all algorithms, and checks itself on the way: Kruskal and Prim must give the same weight, the topological order must be valid, Kosaraju must scan exactly 2m edges, Kahn and the DAG pass exactly m edges, and the MST weight, the SCC count and the sum of DAG distances must match the reference table from the assignment. If anything is wrong the program stops with an exception and does not write the CSV. On my machine the whole run takes about half a minute.

The output is `results/results.csv`: 50 rows, columns `algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result`. Only `time_ms` changes from machine to machine.

### Without Maven

If Maven is not available, plain `javac` works for the main code:

```
mkdir -p target/classes
javac -d target/classes $(find src/main/java -name "*.java")
java -Xmx3g -cp target/classes benchmark.Benchmark
```

## Plots and numbers for the report

```
python3 scripts/make_plots.py
```

This reads `results/results.csv` and writes three PNG files to `results/plots/` (`operations_vs_n.png`, `scans_per_edge.png`, `kruskal_edge_share.png`) and the counter-vs-formula tables and slope fits to `results/analysis.txt`.

## Input format

```
n m directed|undirected
u v w
u v w
...
```

Vertices are numbered 0 to n-1 and weights are `int`. Self-loops, parallel edges and isolated vertices are allowed. Files in `data/`:

| File | Kind | What it is for |
|---|---|---|
| `city_roads_small.txt` | undirected, 6 vertices | classic MST example, total weight 33 |
| `city_roads_disconnected.txt` | undirected, 9 vertices | 4 components, a self-loop and an isolated vertex, MST forest of weight 15 |
| `jobs_cyclic.txt` | directed, 8 vertices | two cycles and a self-loop, 4 strongly connected components, Kahn reports a cycle |
| `jobs_pipeline_dag.txt` | directed, 7 vertices | DAG with negative weights and an isolated vertex, distance sum 30 |

## Project layout

```
src/main/java/graph        Graph, Edge, GraphIO (adjacency lists, file reading and writing)
src/main/java/algorithms   DSU, Kruskal, Prim, Kosaraju, Kahn, DagShortestPath and their result classes
src/main/java/metrics      Metrics (the four counters)
src/main/java/benchmark    Generator (exact seeded generator) and Benchmark (self-checking run)
src/test/java              JUnit 5 tests
data/                      input graphs
results/                   results.csv, analysis.txt, plots/
scripts/make_plots.py      plots and analysis
```

## Tests

Seven test classes, 35 tests:

- DSU against a simple reference with random unions, counters, and a chain of 200 000 elements
- Kruskal and Prim on 100 random connected graphs (same weight, exactly n-1 edges), plus a brute-force check on tiny graphs, plus the disconnected forest case
- Kosaraju against brute force (BFS from every vertex in both directions) on random graphs with up to 200 vertices
- Kahn: valid order on random DAGs, cycle detection on cyclic graphs
- DAG shortest paths against Bellman-Ford on 200 small random DAGs with negative weights, and path restoring
- Stack safety: Kosaraju on a path of 100 000 vertices, also inside a thread with a 128 KB stack
- File input and edge cases

## Reference values

These are the numbers the benchmark prints and checks.

| n / m | MST weight | SCC count | Sum of DAG distances |
|---|---|---|---|
| 1 000 / 4 000 | 147 970 | 27 | 396 274 |
| 1 000 / 20 000 | 30 363 | 1 | 252 411 |
| 3 162 / 12 648 | 468 911 | 137 | 1 271 911 |
| 3 162 / 63 240 | 97 589 | 1 | 723 157 |
| 10 000 / 40 000 | 1 486 139 | 373 | 3 941 828 |
| 10 000 / 200 000 | 309 550 | 1 | 2 381 342 |
| 31 623 / 126 492 | 4 715 515 | 1 188 | 12 551 891 |
| 31 623 / 632 460 | 968 517 | 1 | 7 376 612 |
| 100 000 / 400 000 | 15 012 094 | 3 923 | 40 096 539 |
| 100 000 / 2 000 000 | 3 056 681 | 1 | 23 512 087 |

## Git workflow

`main` has only working code and carries the tag `v1.0`. The work was done on the branches `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths` and `feature/metrics`, merged into `main` with small commits in the `feat(...)`, `test(...)`, `docs(...)` style.