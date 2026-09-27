package site.klade.simulation.condition;

/**
 * Boolean negation {@code NOT <cond>}. Its operand is boolean ({@code Cond}), so
 * {@code NOT 5.0} is unrepresentable.
 *
 * <p>{@code NOT} is intentionally looser than comparison (§3): {@code NOT Mrph[1] > 1.0}
 * means {@code NOT (Mrph[1] > 1.0)}. This implementation always parenthesizes the operand
 * for an unambiguous canonical form.</p>
 */
public final class Not implements Cond {

    public final Cond inner;

    public Not(Cond inner) {
        this.inner = inner;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Not) && inner.equals(((Not) o).inner);
    }

    @Override
    public int hashCode() {
        return 31 + inner.hashCode();
    }

    @Override
    public String toString() {
        return "NOT (" + inner + ")";
    }
}