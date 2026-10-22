package kz.edu.daa.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kz.edu.daa.algorithm.DagShortestPaths;
import kz.edu.daa.algorithm.KahnTopologicalSort;
import kz.edu.daa.algorithm.KosarajuScc;
import kz.edu.daa.algorithm.KruskalMst;
import kz.edu.daa.algorithm.PrimMst;
import kz.edu.daa.graph.Graph;
import kz.edu.daa.result.MstResult;
import org.junit.jupiter.api.Test;

class GraphGeneratorTest {
    @Test
    void sameSeedProducesIdenticalGraphs() {
        assertEquals(GraphGenerator.connectedUndirected(50, 200).edges(),
                GraphGenerator.connectedUndirected(50, 200).edges());
        assertEquals(GraphGenerator.directed(50, 200).edges(),
                GraphGenerator.directed(50, 200).edges());
        assertEquals(GraphGenerator.dag(50, 200).edges(),
                GraphGenerator.dag(50, 200).edges());
    }

    @Test
    void generatedUndirectedGraphIsConnectedAndDagIsAcyclic() {
        Graph undirected = GraphGenerator.connectedUndirected(100, 400);
        MstResult forest = KruskalMst.compute(undirected);
        assertEquals(1, forest.components());
        assertEquals(99, forest.edges().size());

        Graph dag = GraphGenerator.dag(100, 400);
        assertTrue(KahnTopologicalSort.compute(dag).isAcyclic());
    }

    @Test
    void firstReferenceRowMatchesExactly() {
        int vertices = 1_000;
        int edges = 4_000;
        ReferenceValues.ReferenceValue expected = ReferenceValues.forGraph(vertices, edges);

        Graph undirected = GraphGenerator.connectedUndirected(vertices, edges);
        MstResult kruskal = KruskalMst.compute(undirected);
        MstResult prim = PrimMst.compute(undirected);
        assertEquals(expected.mstWeight(), kruskal.totalWeight());
        assertEquals(expected.mstWeight(), prim.totalWeight());

        Graph directed = GraphGenerator.directed(vertices, edges);
        assertEquals(expected.sccCount(), KosarajuScc.compute(directed).componentCount());

        Graph dag = GraphGenerator.dag(vertices, edges);
        assertEquals(expected.dagDistanceSum(), DagShortestPaths.compute(dag).distanceSum());
    }

    @Test
    void rejectsImpossibleRequests() {
        assertThrows(IllegalArgumentException.class,
                () -> GraphGenerator.connectedUndirected(5, 3));
        assertThrows(IllegalArgumentException.class,
                () -> GraphGenerator.directed(1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> GraphGenerator.dag(-1, 0));
    }
}
