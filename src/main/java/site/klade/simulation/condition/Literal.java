package site.klade.simulation.condition;

/** A numeric literal in a condition, e.g. {@code 1.0}. Immutable. */
public final class Literal implements Expr {

    public final float value;

    public Literal(float value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Literal)
                && Float.floatToIntBits(value) == Float.floatToIntBits(((Literal) o).value);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }

    /**
     * Canonical form via {@link Float#toString(float)}, which is the shortest decimal string
     * that round-trips to the same exact float (§4 round-trip contract).
     */
    @Override
    public String toString() {
        return Float.toString(value);
    }
}