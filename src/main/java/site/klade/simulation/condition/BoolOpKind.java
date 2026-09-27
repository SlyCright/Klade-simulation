package site.klade.simulation.condition;

/**
 * Boolean combinators, with their mathematical readings:
 *
 * <pre>
 *   AND  conjunction     a &amp;&amp; b   true only if both operands are true
 *   OR   disjunction     a || b   true if at least one operand is true
 *   XOR  exclusive or    a ^  b   true if exactly one operand is true
 * </pre>
 *
 * <p>Note: the declaration order is <b>not</b> precedence; precedence is encoded in
 * {@link BoolOp} ({@code OR < XOR < AND}).</p>
 *
 * <p><b>Replacement-only mutation: no drift.</b> As with {@link CmpOp}, the listing order is a
 * conventional enumeration with no metric behind it, so no two constants are "one step apart"
 * in behaviour:</p>
 *
 * <ul>
 *   <li>{@code AND} and {@code OR} are De Morgan duals — swapping one for the other inverts the
 *       condition (given negated operands, {@code A AND B} becomes {@code NOT A OR NOT B}), so
 *       they are maximally distant rather than neighbouring.</li>
 *   <li>{@code XOR} is unrelated to both: it agrees with {@code OR} on the single-true case and
 *       with {@code AND} nowhere in particular, but differs from each in a way no single "step"
 *       describes.</li>
 * </ul>
 *
 * <p>Mutation must therefore be <b>replacement only</b>: a uniform choice among the other two
 * constants (rank-linear v2.2, section 4.3, unordered enum). Drift is reserved for domains with
 * a real metric.</p>
 */
public enum BoolOpKind {
    AND, OR, XOR
}
