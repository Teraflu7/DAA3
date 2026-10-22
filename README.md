# DAA3: Graph Algorithms

Design and Analysis of Algorithms - Assignment 3.

For a simple Russian explanation and a defense walkthrough, see
[`GUIDE_RU.md`](GUIDE_RU.md). For oral practice, use the 12-question
[`DEFENSE_QA_RU.md`](DEFENSE_QA_RU.md).

| Field | Value |
|---|---|
| Student name | `Tamiris` |
| Student surname | `Mukieva` |
| Group | `SE-2516` |
| GitHub repository | <https://github.com/Teraflu7/DAA3> |

## Implemented algorithms

- one weighted adjacency-list `Graph` for directed and undirected inputs;
- disjoint-set union with union by size and path compression;
- Kruskal and Prim minimum spanning forests;
- Kosaraju SCC with iterative DFS in both passes;
- Kahn topological sorting with cycle detection;
- shortest paths in a DAG from a virtual super-source;
- operation counters, deterministic graph generators, CSV and PNG plots.

The project uses Java 17, Maven Wrapper and JUnit 5. Algorithm code uses only
the Java standard library.

## Build and test

Windows PowerShell or the IntelliJ terminal:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

The test suite includes randomized reference checks, 100 Kruskal-versus-Prim
graphs, SCC versus brute-force reachability, DAG shortest paths versus
Bellman-Ford, an iterative DFS path with 100,000 vertices, and validation of the
committed 50-row CSV plus all three PNG files.

## Run the benchmark

Fast check at the two `n = 1000` densities:

```powershell
.\mvnw.cmd -q compile exec:java '-Dexec.args=--quick --output=results-quick'
```

Required full run (five sizes, two densities and five algorithm rows per case):

```powershell
.\mvnw.cmd -q compile exec:java '-Dexec.args=--full'
```

Linux/macOS full benchmark:

```bash
./mvnw -q compile exec:java -Dexec.args="--full"
```

The full command validates every result against the assignment reference table
before writing:

```text
results/results.csv
results/plots/operations-vs-n.png
results/plots/scans-per-edge.png
results/plots/kruskal-edge-share.png
```

The CSV contains exactly 50 data rows with this schema:

```text
algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result
```

Wall-clock time is the median of five runs. Conclusions in `REPORT.md` use
operation counters that are independent of wall-clock timing for the fixed
input and Java 17 implementation as the main evidence.

## Input graph format

```text
n m directed
u v weight
...
```

Use `undirected` instead of `directed` for an undirected graph. Vertices are
numbered `0..n-1`, weights are Java `int` values, and the reader accepts blank
lines plus `#` comments. Examples are in `data/`.

## Project layout

```text
data/                         small input graphs
src/main/java/kz/edu/daa/
  algorithm/                  Kruskal, Prim, Kosaraju, Kahn, DAG paths
  benchmark/                  generators, runner, CSV and PNG output
  dsu/                        disjoint-set union
  graph/                      graph representation and file reader
  metrics/                    operation counters
  result/                     immutable result records
src/test/java/kz/edu/daa/     JUnit 5 tests
results/                      required benchmark evidence
GUIDE_RU.md                   simple Russian defense guide
DEFENSE_QA_RU.md              mock oral defense with model answers
REPORT.md                     complexity and empirical analysis
```

## Reproducibility

Every generated graph starts with a fresh `new Random(42)` and follows the
exact call order from the assignment. A benchmark run stops immediately if an
MST weight, SCC count, DAG distance sum, topological order, or exact traversal
edge count is wrong.

## AI-use acknowledgement

An AI assistant was used for implementation support, test design, code review,
and documentation. Every algorithm is implemented in this repository rather
than imported from a library. The generated results are checked by deterministic
tests and by the reference values supplied with the assignment. The student is
responsible for reviewing the code and explaining it during the defense.

## Submission checklist

- Deadline: 23 October 2026, 23:59.
- Create the GitHub repository above before pushing the local history.
- Confirm `main` builds and the working tree is clean.
- Push `main` and all five `feature/*` branches to the GitHub URL above.
- Push the final annotated tag `v1.0`.
- Create `DAA_Assignment3_Tamiris_Mukieva_SE-2516.zip` without `.git`, `.idea`,
  `target`, or `results-quick`.
- Upload the ZIP to Moodle and include the GitHub repository link.
