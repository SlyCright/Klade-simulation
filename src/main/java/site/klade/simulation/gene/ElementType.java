package site.klade.simulation.gene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The vocabulary of element types that a {@code become} command may name, covering both nodes and
 * segments in one enum.
 *
 * <p><b>Why one enum and not {@code NodeType}/{@code SegmentType}:</b> the DNA rarely cares which kind
 * a type belongs to — {@code become muscle} is a {@code become} either way, and the interpreter imposes
 * the kind (a node cannot become a segment). Splitting the vocabulary would push that distinction into
 * the parser, the spec table and the mutation domain for no gain. {@link #kind()} is available where
 * the distinction genuinely matters.</p>
 *
 * <p><b>{@code STEM_*} mean "no function yet".</b> They are the neutral default an element is created
 * with: {@code lay_segment} produces a {@code STEM_SEGMENT} and a {@code STEM_NODE}, which the program
 * then differentiates with {@code become}. They deliberately have no parameters, so a freshly created
 * element is valid without any further genes executing.</p>
 *
 * <p><b>MVP vocabulary only.</b> {@code NEURON}, {@code SPIKE}, {@code PROBE} and {@code NEURAL_LINK}
 * are intentionally absent: adding a type before its runtime behaviour exists would hand evolution
 * mutations that cannot pay off. Parameter ranges are also absent by design — they belong to the
 * backend mutation layer, since this library must stay GWT-compatible and free of mutation vocabulary.</p>
 *
 * <p>TODO(future): every node must eventually be able to transform into a segment and vice versa.
 * {@link #kind()} is therefore an interpretation-time guard for MVP only (see {@code Ontogenesis}),
 * not a permanent restriction on {@code become}.</p>
 */
public enum ElementType {

    /** A node with no function yet: the default produced by {@code lay_segment}. */
    STEM_NODE("stem_node", ElementKind.NODE),

    /** A segment with no function yet: the default produced by {@code lay_segment}. */
    STEM_SEGMENT("stem_segment", ElementKind.SEGMENT),

    /** Grip: raises its friction coefficient when active. No function parameter. */
    FRICTION_NODE("friction_node", ElementKind.NODE),

    /** Internal timer: emits a square wave with a genetically encoded period. */
    RHYTHM_NODE("rhythm_node", ElementKind.NODE),

    /** Locomotive segment: contracts to its encoded length when active. */
    MUSCLE("muscle", ElementKind.SEGMENT);

    private final String canonicalName;
    private final ElementKind kind;

    ElementType(String canonicalName, ElementKind kind) {
        this.canonicalName = canonicalName;
        this.kind = kind;
    }

    /** The DSL spelling, e.g. {@code "rhythm_node"}. Stored rather than derived so renaming the
     * constant cannot silently change the on-disk format. */
    public String canonicalName() {
        return canonicalName;
    }

    public ElementKind kind() {
        return kind;
    }

    /**
     * The value-dependent arguments {@code become} takes <i>after</i> the element type. Empty for
     * types without function genes; a single spec for {@code RHYTHM_NODE} and {@code MUSCLE}.
     *
     * <p>This is what gives {@code become} its dependent arity: {@code become muscle length 40%} is
     * valid while {@code become friction_node length 40%} is not.</p>
     *
     * <p>The returned list is immutable and freshly built per call, so callers cannot corrupt the
     * signature.</p>
     */
    public List<ArgSpec> parameterSpecs() {
        switch (this) {
            case RHYTHM_NODE:
                return Collections.singletonList(
                        new NumberSpec("period", "", NumberKind.INTEGER));
            case MUSCLE:
                return Collections.singletonList(
                        new NumberSpec("length", "%", NumberKind.CONTINUOUS));
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Resolves a DSL spelling to a type, case-insensitively. Returns {@code null} when unknown so the
     * caller can report a precise parse error with its own context (line number, position) instead of
     * this method guessing where the text came from.
     */
    public static ElementType parse(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (ElementType type : values()) {
            if (type.canonicalName.equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        return null;
    }

    /** All canonical spellings, for error messages. Order follows the declaration order. */
    public static List<String> allCanonicalNames() {
        List<String> names = new ArrayList<String>(values().length);
        for (ElementType type : values()) {
            names.add(type.canonicalName);
        }
        return names;
    }
}
