# DAA3: Graph Algorithms

Standalone Java 17 and Maven project for Design and Analysis of Algorithms,
Assignment 3.

The project will contain a weighted adjacency-list graph, DSU, Kruskal, Prim,
iterative Kosaraju, Kahn topological sorting, shortest paths in a DAG, JUnit 5
tests, a deterministic benchmark, CSV evidence and three PNG plots.

## Build

```powershell
.\mvnw.cmd clean test
```

## Benchmark

```powershell
.\mvnw.cmd -q exec:java '-Dexec.args=--full'
```

Detailed usage, student information and the AI-use acknowledgement will be
completed before the release tag.
