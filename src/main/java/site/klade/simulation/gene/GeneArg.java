package site.klade.simulation.gene;

/**
 * Type tag for a single <b>gene argument value</b>: {@link NumberArg}, {@link ElementTypeArg} or
 * {@link ReferenceArg}.
 *
 * <p>Deliberately empty — the type itself is the semantic contract, exactly as {@code Expr} and
 * {@code Cond} are in {@code site.klade.simulation.condition}. A {@link Gene} carries an ordered list
 * of arguments, and pairing that list with the action's declared {@link ArgSpec} signature is what
 * makes a malformed command unconstructable (see {@link GeneSpecs}).</p>
 *
 * <p>Arguments carry a <b>value</b> and describe nothing about themselves. Everything the codec and
 * the mutation operator need to know — a human-readable label, a display unit, whether the number is
 * continuous or integral, whether it is a reference — lives in the matching {@link ArgSpec}. That
 * split is what keeps this package free of mutation ranges and rendering rules.</p>
 */
public interface GeneArg {
}
