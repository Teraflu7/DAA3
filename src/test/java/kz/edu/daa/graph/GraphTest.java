package kz.edu.daa.graph;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GraphTest {
    @Test
    void storesDirectedEdgesAndIndegrees() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 7);
        graph.addEdge(0, 2, -3);
        graph.addEdge(2, 1, 5);

        assertTrue(graph.isDirected());
        assertEquals(3, graph.edgeCount());
        assertEquals(2, graph.neighbors(0).size());
        assertEquals(0, graph.neighbors(1).size());
        assertArrayEquals(new int[] {0, 2, 1, 0}, graph.indegrees());
    }

    @Test
    void storesOneLogicalUndirectedEdgeAndTwoAdjacencyEntries() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 2, 11);

        assertFalse(graph.isDirected());
        assertEquals(1, graph.edgeCount());
        assertEquals(new WeightedEdge(0, 2, 11), graph.edges().get(0));
        assertEquals(new WeightedEdge(0, 2, 11), graph.neighbors(0).get(0));
        assertEquals(new WeightedEdge(2, 0, 11), graph.neighbors(2).get(0));
    }

    @Test
    void reversesEveryDirectedEdge() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 4);
        graph.addEdge(2, 1, 6);

        Graph reversed = graph.reversed();

        assertEquals(new WeightedEdge(1, 0, 4), reversed.edges().get(0));
        assertEquals(new WeightedEdge(1, 2, 6), reversed.edges().get(1));
        assertArrayEquals(new int[] {1, 0, 1}, reversed.indegrees());
    }

    @Test
    void protectsInternalCollectionsAndIndegreeArray() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 1);

        assertSame(graph.neighbors(0), graph.neighbors(0));
        assertSame(graph.edges(), graph.edges());
        assertThrows(UnsupportedOperationException.class,
                () -> graph.neighbors(0).add(new WeightedEdge(0, 0, 0)));
        assertThrows(UnsupportedOperationException.class,
                () -> graph.edges().clear());

        int[] copy = graph.indegrees();
        copy[1] = 99;
        assertArrayEquals(new int[] {0, 1}, graph.indegrees());
    }

    @Test
    void validatesVerticesButAllowsLoopsAndParallelEdges() {
        Graph graph = new Graph(2, true);
        graph.addEdge(1, 1, 3);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 1, 4);

        assertEquals(3, graph.edgeCount());
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(-1, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.neighbors(2));
    }

    @Test
    void handlesUndirectedLoopsParallelEdgesIsolatedVerticesAndEmptyGraphs() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 0, -9);
        graph.addEdge(1, 2, 5);
        graph.addEdge(1, 2, 7);

        assertEquals(3, graph.edgeCount());
        assertEquals(2, graph.neighbors(0).size(), "an undirected loop contributes two arcs");
        assertEquals(2, graph.neighbors(1).size());
        assertTrue(graph.neighbors(3).isEmpty());

        Graph reversed = graph.reversed();
        assertFalse(reversed.isDirected());
        assertEquals(graph.edges(), reversed.edges());

        Graph empty = new Graph(0, false);
        assertEquals(0, empty.vertexCount());
        assertTrue(empty.edges().isEmpty());
    }
}
