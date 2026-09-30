package site.klade.simulation.gene;

import site.klade.simulation.MorphogenText;

import site.klade.simulation.Index;

/**
 * A reference to a morphogen by dotted index, e.g. the {@code Morphogen[1.1]} of
 * {@code express Morphogen[1.1] amount 2.0}.
 *
 * <p>Uses the same {@link Index} identity as genes, so the project has <b>one</b> identity concept
 * rather than a flat integer one here and a dotted one for genes. That unification is
 * what lets a morphogen be duplicated with divergence through the ordinary index drift the
 * index-independent operator already provides.</p>
 *
 * <p>Canonical spelling is {@link MorphogenText#reference}: the full word with no trailing dot, e.g.
 * {@code Morphogen[1.1]}. Abbreviated spellings ({@code Mrph}, {@code Mph}) are accepted on input by
 * {@code site.klade.simulation.condition.ConditionParser} but never emitted.</p>
 */
public final class ReferenceArg implements GeneArg {

    public final Index morphogen;

    public ReferenceArg(Index morphogen) {
        if (morphogen == null) throw new IllegalArgumentException("reference argument is null");
        this.morphogen = morphogen;
    }

    @Override
    public String toString() {
        return MorphogenText.reference(morphogen);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ReferenceArg)
                && morphogen.equals(((ReferenceArg) other).morphogen);
    }

    @Override
    public int hashCode() {
        return morphogen.hashCode();
    }
}
