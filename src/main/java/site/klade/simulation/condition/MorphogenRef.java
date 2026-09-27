package site.klade.simulation.condition;

/** Reference to a morphogen concentration by id, e.g. {@code Mrph[1]}. Immutable. */
public final class MorphogenRef implements Expr {

    public final int id;

    public MorphogenRef(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MorphogenRef) && id == ((MorphogenRef) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /** Canonical form is always {@code Mrph[<id>]} (§5.2). */
    @Override
    public String toString() {
        return "Mrph[" + id + "]";
    }
}