package site.klade.simulation;

/**
 * Allocation policy for hierarchical indices (index-independent genetic operator).
 *
 * <p>Pure functions over {@link Index} values — no state, no randomness, no sets.
 * Indexes encode order only (no tree/ownership semantics).</p>
 *
 */
public final class Indexes {

    private Indexes() {
    }

    /**
     * Allocates a fresh index strictly between {@code left} and {@code right}.
     *
     * <p>Precondition: {@code left} and {@code right} must be the target genome's
     * <b>current consecutive neighbours</b> (the gap the caller picked) — or a null endpoint:</p>
     * <ul>
     *   <li>{@code left == null} — insert before the current first index</li>
     *   <li>{@code right == null} — append after the current last index</li>
     *   <li>both {@code null} — empty genome, yields the root index</li>
     * </ul>
     *
     * <p>Because the endpoints are consecutive, the result is guaranteed not to collide with
     * any index of that genome — no occupancy bookkeeping needed here.
     * {@code Genome.ensureSortedAndUnique()} backstops violations of this precondition.</p>
     *
     * @throws IllegalArgumentException if both endpoints are non-null and left &gt;= right
     */
    public static Index allocateBetween(Index left, Index right) {
        if (left == null && right == null) return new Index(0);
        if (left == null) return right.decrementFirst();
        if (right == null) return left.incrementFirst();
        if (left.compareTo(right) >= 0) throw new IllegalArgumentException("left must precede right");

        if (isNegativeDirection(left, right)) {
            Index previousRight = right.decrementLast();
            if (isStrictlyBetween(left, right, previousRight)) return previousRight;
        } else {
            Index next = left.incrementLast();
            if (isStrictlyBetween(left, right, next)) return next;
        }

        Index previousLeft = left.decrementLast();
        if (isStrictlyBetween(left, right, previousLeft)) return previousLeft;

        return left.extend(0);
    }

    private static boolean isNegativeDirection(Index left, Index right) {
        return left.segmentAt(left.segmentCount() - 1) < 0
                || (left.isPrefixOf(right) && right.segmentAt(1) == 0);
    }

    private static boolean isStrictlyBetween(Index left, Index right, Index candidate) {
        return left.compareTo(candidate) < 0 && candidate.compareTo(right) < 0;
    }

}
