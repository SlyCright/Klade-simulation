package site.klade.simulation.condition;

/**
 * Type tag for <b>boolean</b> conditions: {@link Compare}, {@link Not} and {@link BoolOp}.
 *
 * <p>A condition is exactly a value of type {@code Cond}. The separation from {@link Expr}
 * guarantees boolean-typed operands for {@code NOT} and {@code AND/OR/XOR} at construction
 * time, keeping every well-formed tree syntactically valid (§3.6).</p>
 */
public interface Cond {}