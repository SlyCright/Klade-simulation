package site.klade.simulation.gene;

/**
 * One argument <b>slot</b> in an action's signature: what the argument means, not what it holds.
 *
 * <p>Implementations are the three argument kinds — {@link NumberSpec}, {@link ElementTypeSpec} and
 * {@link ReferenceSpec}. A signature is an ordered {@code List<ArgSpec>}; a gene's arguments must match
 * it one-to-one in count and kind, which {@link GeneSpecs#validate} enforces at construction time.</p>
 *
 * <p><b>No mutation range is carried here.</b> That is a deliberate decoupling: the shared simulation
 * library must compile to GWT and must not depend on the backend's mutation vocabulary
 * ({@code LoopMode}, drift windows). The backend resolves a spec to a domain by looking at its label
 * and unit.</p>
 */
public interface ArgSpec {

    /**
     * The human-readable introducer of the argument on the DNA line, e.g. {@code "length"} in
     * {@code become muscle length 40%}. Returns {@code null} when the argument is described by its unit
     * alone instead (e.g. the angle in {@code lay_segment 30°}).
     *
     * <p>At least one of {@link #label()} and the spec's unit must be present: a numeric argument that
     * is neither labelled nor united would be unreadable on the line, which the DNA grammar forbids.</p>
     */
    String label();
}
