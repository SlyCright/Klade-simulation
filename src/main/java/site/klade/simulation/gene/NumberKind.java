package site.klade.simulation.gene;

/**
 * The domain shape of a numeric gene argument.
 *
 * <p>This is <b>not</b> a storage decision — every number is held as a {@code float} so that the whole
 * genome has one numeric representation (matching {@code Literal}, {@code Morphogen} and
 * {@code MetaGenes}). It tells the mutation operator which rank-linear rule applies:</p>
 *
 * <ul>
 *   <li>{@code CONTINUOUS} — drift over the real domain (via {@code DoubleValueMutator});</li>
 *   <li>{@code INTEGER} — a count, sampled/rounded to whole numbers (via {@code IntegerValueMutator}).</li>
 * </ul>
 *
 * <p>The <i>ranges</i> deliberately do not live here; they belong to the backend, which owns mutation
 * (see the plan's 3.1). A spec describes meaning, never a mutation domain.</p>
 */
public enum NumberKind {
    CONTINUOUS,
    INTEGER
}
