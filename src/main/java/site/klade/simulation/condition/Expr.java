package site.klade.simulation.condition;

/**
 * Type tag for <b>numeric</b> condition atoms: {@link Literal} and {@link MorphogenRef}.
 *
 * <p>Deliberately empty — the type itself is the semantic contract. Comparison operands
 * must be {@code Expr}; boolean structure lives in {@link Cond}. This makes malformed
 * trees such as {@code NOT 5.0} or a nested {@code Compare(Compare, GT, ...)} impossible to
 * construct, so no runtime type-check is needed after structural mutation (§3.6).</p>
 */
public interface Expr {}