package kz.edu.daa.benchmark;

import java.util.List;

/** Deterministic checks copied from the assignment reference table. */
public final class ReferenceValues {
    private static final List<Integer> SIZES = List.of(1_000, 3_162, 10_000, 31_623, 100_000);
    private static final List<Integer> DENSITY_MULTIPLIERS = List.of(4, 20);

    private static final List<ReferenceValue> VALUES = List.of(
            new ReferenceValue(1_000, 4_000, 147_970, 27, 396_274),
            new ReferenceValue(1_000, 20_000, 30_363, 1, 252_411),
            new ReferenceValue(3_162, 12_648, 468_911, 137, 1_271_911),
            new ReferenceValue(3_162, 63_240, 97_589, 1, 723_157),
            new ReferenceValue(10_000, 40_000, 1_486_139, 373, 3_941_828),
            new ReferenceValue(10_000, 200_000, 309_550, 1, 2_381_342),
            new ReferenceValue(31_623, 126_492, 4_715_515, 1_188, 12_551_891),
            new ReferenceValue(31_623, 632_460, 968_517, 1, 7_376_612),
            new ReferenceValue(100_000, 400_000, 15_012_094, 3_923, 40_096_539),
            new ReferenceValue(100_000, 2_000_000, 3_056_681, 1, 23_512_087));

    private ReferenceValues() {
    }

    public static List<Integer> sizes() {
        return SIZES;
    }

    public static List<Integer> densityMultipliers() {
        return DENSITY_MULTIPLIERS;
    }

    public static ReferenceValue forGraph(int vertices, int edges) {
        return VALUES.stream()
                .filter(value -> value.vertices() == vertices && value.edges() == edges)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No reference value for n=" + vertices + ", m=" + edges));
    }

    public static List<ReferenceValue> all() {
        return VALUES;
    }

    public record ReferenceValue(
            int vertices,
            int edges,
            long mstWeight,
            int sccCount,
            long dagDistanceSum) {
    }
}
