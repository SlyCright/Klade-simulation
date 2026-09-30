package site.klade.simulation.gene;

/**
 * Signature slot for a morphogen reference, e.g. the {@code Morphogen[1]} of
 * {@code express Morphogen[1] amount 2.0}.
 *
 * <p>The label is the full word, never an abbreviation: the DNA is meant to be readable by someone who
 * has never seen the project, and {@code Morphogen} is a core concept rather than a term to decode.
 * {@code Mrph} and {@code Mph} are still accepted on <i>input</i> by the condition parser, but are
 * never emitted.</p>
 *
 * <p>A stateless singleton: use {@link #INSTANCE}.</p>
 */
public final class ReferenceSpec implements ArgSpec {

    public static final ReferenceSpec INSTANCE = new ReferenceSpec();

    private ReferenceSpec() {
    }

    @Override
    public String label() {
        return "Morphogen";
    }

    @Override
    public String toString() {
        return "ReferenceSpec";
    }
}
