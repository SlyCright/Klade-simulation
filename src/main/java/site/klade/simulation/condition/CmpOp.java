package site.klade.simulation.condition;

/**
 * Binary comparison operators. Each constant is the ASCII abbreviation of a mathematical
 * relation; the emitted symbol is what appears in the DSL.
 *
 * <pre>
 *   LT  ("less than")               a &lt;  b
 *   LE  ("less than or equal")      a &lt;= b
 *   GT  ("greater than")            a &gt;  b
 *   GE  ("greater than or equal")   a &gt;= b
 *   EQ  ("equal")                   a == b
 *   NE  ("not equal")               a != b
 * </pre>
 *
 * <p><b>Replacement-only mutation: no drift.</b> The listing order above is a conventional
 * enumeration, <i>not</i> an order that carries a metric, so adjacent constants are not
 * semantically close. Drift is only meaningful when "one step away" means "slightly
 * different"; here it does not:</p>
 *
 * <ul>
 *   <li>{@code EQ} and {@code GE} are near-duals for a float operand. Morphogen concentrations
 *       are summed and diffused every tick, so an exact bit match such as
 *       {@code Mrph[1] == 1.0} is effectively never true, while {@code Mrph[1] >= 1.0} is a
 *       broad threshold that is routinely true. Drifting {@code GE -> EQ} would therefore not
 *       be a small change but an annihilation of the condition's meaning.</li>
 *   <li>{@code GT -> GE} and {@code LT -> LE} *are* small relaxations, but they are the only
 *       such pairs; {@code EQ}/{@code NE} sit apart from the four inequalities, and
 *       {@code GT <-> LT} invert the condition outright. A total order cannot express this
 *       structure — it is a small lattice, not a line.</li>
 * </ul>
 *
 * <p>Consequently operator mutation must use <b>replacement only</b>: a uniform choice among
 * the other five constants. This is exactly what the rank-linear operator prescribes for an
 * <i>unordered</i> enum (rank-linear v2.2, section 4.3). Drift applies only to domains that
 * carry a genuine metric — e.g. a {@link Literal} float value.</p>
 */
public enum CmpOp {
    LT, LE, GT, GE, EQ, NE
}
