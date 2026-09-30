package site.klade.simulation.gene;

/**
 * A numeric gene argument, e.g. the {@code 30} of {@code lay_segment 30°} or the {@code 40} of
 * {@code become muscle length 40%}.
 *
 * <p>Held as a {@code float} to match {@link site.klade.simulation.condition.Literal},
 * {@link site.klade.simulation.Morphogen} and {@link site.klade.simulation.MetaGenes}, so the whole
 * genome has a single numeric representation. What the number <i>means</i> — its label, unit and
 * whether it is continuous or integral — lives in the paired {@link NumberSpec}, not here.</p>
 *
 * <p><b>NaN and infinities are rejected at construction.</b> A non-finite value would silently poison
 * every later simulation step (a NaN position is hard to trace back to its origin), so it is refused at
 * the genome boundary rather than allowed to propagate. This mirrors the backend's
 * {@code MutationMechanics.requireFinite} rule.</p>
 */
public final class NumberArg implements GeneArg {

    public final float value;

    public NumberArg(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException("number argument must be finite: " + value);
        }
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof NumberArg)
                && Float.floatToIntBits(value) == Float.floatToIntBits(((NumberArg) other).value);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }

    /**
     * The canonical numeric form, shared with every other number the DNA emits — see
     * {@link site.klade.simulation.CanonicalNumber} for the rule and why it is centralised. Emitted value
     * and unit concatenate directly ({@code "30" + "°"}), so no separator logic is needed anywhere.
     */
    @Override
    public String toString() {
        return site.klade.simulation.CanonicalNumber.format(value);
    }
}
