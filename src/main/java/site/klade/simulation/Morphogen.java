package site.klade.simulation;

/**
 * A morphogen definition: its identity ({@link Index}, the same dotted identity genes use), how fast it
 * spreads, and how fast it decays.
 *
 * <p><b>Identity is a dotted {@link Index}, and it encodes order only.</b> {@code 1}, {@code 1.1} and
 * {@code 2} are all legal ids, exactly as they are for genes. A dotted id is <i>not</i> a position in a
 * hierarchy of morphogens: there is no parent/child relationship here, no containment, and no depth
 * that means anything. {@code 1.1} is simply an id that sorts immediately after {@code 1}. See
 * {@link Index}, which states the same rule for genes.</p>
 *
 * <p><b>Why dotted ids at all, then.</b> Because the index-independent operator already knows how to
 * drift and insert within the gaps of a dotted id space, so giving morphogens the same identity as
 * genes lets them reuse that machinery unchanged. Ids stay stable when something is inserted elsewhere
 * in the id space, which is what keeps a reference to a morphogen meaning the same thing across
 * generations.</p>
 *
 * <p><b>The set of morphogens is derived, not authored.</b> A definition exists because some gene
 * references it; it disappears when the last reference goes away, and if the reference returns the
 * definition is re-created by copying the nearest defined id — "nearest" again meaning closest in the
 * dotted ordering the operator works in, not a parent. See {@code Genome#deriveMorphogens}.
 * Consequently this class has no notion of "deleted" or "reserved" ids: index reuse is intended.</p>
 *
 * <p>TODO(mvp-deferred): a morphogen's {@code spreadingConditions} (e.g. {@code everywhere}, {@code up})
 * is not modelled yet. When added, it is a fourth property of the definition and must be part of the
 * {@code --- Morphogens} line and of morphogen value mutation.</p>
 */
public class Morphogen {

    private Index id;

    private float diffusionRatio;

    private float decayRatio;

    public Morphogen(Index id, float diffusionRatio, float decayRatio) {
        this.id = id;
        this.diffusionRatio = diffusionRatio;
        this.decayRatio = decayRatio;
    }

    public Morphogen(Morphogen other) {
        this.id = other.id;  // Index is immutable. So consider as deep copy constructor
        this.diffusionRatio = other.diffusionRatio;
        this.decayRatio = other.decayRatio;
    }

    public Index getId() {
        return id;
    }

    public void setId(Index id) {
        this.id = id;
    }

    public float getDiffusionRatio() {
        return diffusionRatio;
    }

    public void setDiffusionRatio(float diffusionRatio) {
        this.diffusionRatio = diffusionRatio;
    }

    public float getDecayRatio() {
        return decayRatio;
    }

    public void setDecayRatio(float decayRatio) {
        this.decayRatio = decayRatio;
    }

    /**
     * The canonical <b>definition line</b>, exactly as it appears in the {@code --- Morphogens} section:
     * {@code Morphogen[1]: 0.8, 0.1}.
     *
     * <p>This is the same responsibility {@link site.klade.simulation.gene.Gene#toString()} has for a gene
     * line, so the writer emits both directly and neither format is defined twice. A genome's text is
     * therefore assembled from the canonical form of its parts, and a unit test can assert the parts
     * without going through the writer.</p>
     *
     * <p>It is <b>not</b> a debug dump. Use the JSON-ish form only if a debugger needs it; nothing in the
     * production path consumes it.</p>
     */
    @Override
    public String toString() {
        return MorphogenText.reference(id) + ": "
                + CanonicalNumber.format(diffusionRatio) + ", "
                + CanonicalNumber.format(decayRatio);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Morphogen)) {
            return false;
        }
        Morphogen morphogen = (Morphogen) other;
        return Float.floatToIntBits(diffusionRatio) == Float.floatToIntBits(morphogen.diffusionRatio)
                && Float.floatToIntBits(decayRatio) == Float.floatToIntBits(morphogen.decayRatio)
                && (id == null ? morphogen.id == null : id.equals(morphogen.id));
    }

    @Override
    public int hashCode() {
        int result = id == null ? 0 : id.hashCode();
        result = 31 * result + Float.hashCode(diffusionRatio);
        result = 31 * result + Float.hashCode(decayRatio);
        return result;
    }
}
