package kz.edu.daa.dsu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.edu.daa.metrics.MetricsSnapshot;
import org.junit.jupiter.api.Test;

class DisjointSetUnionTest {
    @Test
    void joinsBySizeCompressesPathsAndCountsOperations() {
        DisjointSetUnion dsu = DisjointSetUnion.makeSet(5);

        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(2, 3));
        assertTrue(dsu.union(0, 2));
        assertFalse(dsu.union(1, 0));
        assertEquals(dsu.find(0), dsu.find(3));
        assertEquals(4, dsu.sizeOf(3));
        assertEquals(2, dsu.components());

        MetricsSnapshot metrics = dsu.metrics();
        assertEquals(11, metrics.findCalls());
        assertEquals(3, metrics.successfulUnions());
        assertEquals(14, metrics.dsuOps());
    }

    @Test
    void agreesWithSimpleReferenceAfterRandomUnions() {
        int elementCount = 80;
        DisjointSetUnion dsu = DisjointSetUnion.makeSet(elementCount);
        int[] label = new int[elementCount];
        for (int element = 0; element < elementCount; element++) {
            label[element] = element;
        }

        Random random = new Random(2026);
        for (int operation = 0; operation < 5_000; operation++) {
            int first = random.nextInt(elementCount);
            int second = random.nextInt(elementCount);
            boolean expected = mergeLabels(label, first, second);
            assertEquals(expected, dsu.union(first, second));

            if (operation % 100 == 0) {
                for (int left = 0; left < elementCount; left++) {
                    for (int right = 0; right < elementCount; right++) {
                        assertEquals(label[left] == label[right], dsu.connected(left, right));
                    }
                }
                assertEquals(distinctLabels(label), dsu.components());
            }
        }
        assertEquals(distinctLabels(label), dsu.components());
    }

    @Test
    void handlesEmptySetAndRejectsInvalidElements() {
        DisjointSetUnion empty = DisjointSetUnion.makeSet(0);
        assertEquals(0, empty.components());
        assertThrows(IndexOutOfBoundsException.class, () -> empty.find(0));
        assertThrows(IllegalArgumentException.class, () -> DisjointSetUnion.makeSet(-1));
    }

    private static boolean mergeLabels(int[] labels, int first, int second) {
        int oldLabel = labels[second];
        int newLabel = labels[first];
        if (oldLabel == newLabel) {
            return false;
        }
        for (int index = 0; index < labels.length; index++) {
            if (labels[index] == oldLabel) {
                labels[index] = newLabel;
            }
        }
        return true;
    }

    private static int distinctLabels(int[] labels) {
        return (int) java.util.Arrays.stream(labels).distinct().count();
    }
}
