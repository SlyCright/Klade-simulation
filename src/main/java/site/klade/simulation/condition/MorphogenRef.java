package site.klade.simulation.condition;

import site.klade.simulation.Index;
import site.klade.simulation.MorphogenText;

/**
 * Reference to a morphogen concentration by dotted index, e.g. {@code Morphogen[1.1]}.
 * Immutable.
 *
 * <p>Uses the same {@link Index} identity as genes and as {@code ReferenceArg}, so one concept covers
 * both places a morphogen can be named (a condition and an {@code express} argument). Rendering is
 * delegated to {@link MorphogenText} so the two spellings can never drift apart.</p>
 *
 * <p>Canonical form is always the full word: {@code Morphogen[1.1]}. {@code Mrph} and {@code Mph} are
 * accepted on input by {@link ConditionParser} but never emitted.</p>
 */
public final class MorphogenRef implements Expr {

    public final Index id;

    public MorphogenRef(Index id) {
        if (id == null) {
            throw new IllegalArgumentException("morphogen reference id is null");
        }
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MorphogenRef) && id.equals(((MorphogenRef) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Canonical form is always {@code Morphogen[<index>]}. */
    @Override
    public String toString() {
        return MorphogenText.reference(id);
    }
}
