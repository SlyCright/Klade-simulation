package site.klade.simulation.gene;

/**
 * Whether an {@link ElementType} is realised as a node or as a segment.
 *
 * <p>This is the <b>interpretation-time</b> guard that lets one {@code become} command serve both
 * kinds: when the executing element's kind does not match, the command is ignored (the ontogenesis
 * spec's "a command executed on a segment is ignored" rule). It is deliberately <i>not</i> a
 * restriction on the genome — the genome may order {@code become} across both kinds freely.</p>
 */
public enum ElementKind {
    NODE,
    SEGMENT
}
