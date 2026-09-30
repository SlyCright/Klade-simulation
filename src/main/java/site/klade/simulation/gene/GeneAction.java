package site.klade.simulation.gene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The vocabulary of DNA commands, plus each command's <b>leading</b> argument signature.
 *
 * <p>An action describes what a gene does for one tick; the arguments it needs are declared here as
 * {@link ArgSpec}s and validated by {@link GeneSpecs}. Because the signature is declarative, the codec
 * is generic and the mutation operator can regenerate a valid argument list for any action without
 * knowing what the action means.</p>
 *
 * <p><b>{@code WAIT} is deliberately absent.</b> Waiting is not a command — it is the <i>absence</i> of
 * one. An empty gene is an index with no action ({@link #EMPTY}), which is what the agent's
 * instruction requires ("no need to handle 'wait'... it will be just empty line in dna like
 * {@code 1.12. [...]}"), and it means mutations create and consume waiting naturally by deleting or
 * filling an action rather than by targeting a special constant.</p>
 *
 * <p><b>{@code BECOME} has dependent arity.</b> Its leading spec is the element type; the chosen type
 * then contributes its own {@link ElementType#parameterSpecs()}. That is how the vision's function
 * genes get a home — {@code become muscle length 40%} — without a separate command per element type.</p>
 *
 * <p>TODO(future): the action set is expected to grow (graph commands such as
 * {@code connect_to_nearest_node}, symmetry commands such as {@code lay_two_segments}, conditional
 * {@code wait until ...}). Keep {@link #leadingSpecs()} declarative — adding a command must be one
 * constant plus one spec list.</p>
 */
public enum GeneAction {

    /** An empty gene: the element does nothing for one tick. Serialises as the index alone. */
    EMPTY(""),

    /** {@code become <element type> [<that type's function parameters>]}. */
    BECOME("become"),

    /** {@code lay_segment <angle>}. */
    LAY_SEGMENT("lay_segment"),

    /** {@code express <morphogen> amount <value>}. */
    EXPRESS("express");

    private final String canonicalName;

    GeneAction(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    /**
     * The DSL spelling. Empty for {@link #EMPTY}, because an empty gene emits nothing after its index.
     * Stored rather than derived from {@code name()} so that renaming a constant cannot silently change
     * the on-disk format.
     */
    public String canonicalName() {
        return canonicalName;
    }

    /**
     * The arguments that <b>precede</b> any value-dependent arguments, in order.
     *
     * <p>For {@link #BECOME} this is just the element type; the remaining arguments depend on which
     * type was chosen and are appended by {@link GeneSpecs#specsOf}. For every other action this list is
     * the complete signature.</p>
     *
     * <p>The returned list is immutable.</p>
     */
    public List<ArgSpec> leadingSpecs() {
        switch (this) {
            case BECOME:
                return Collections.singletonList(ElementTypeSpec.INSTANCE);
            case LAY_SEGMENT:
                // A bare angle: the degree sign describes it, so no label is needed.
                return Collections.singletonList(
                        new NumberSpec(null, "°", NumberKind.CONTINUOUS));
            case EXPRESS:
                return Arrays.asList(
                        ReferenceSpec.INSTANCE,
                        new NumberSpec("amount", "", NumberKind.CONTINUOUS));
            case EMPTY:
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Resolves a DSL spelling to an action, case-insensitively. Returns {@code null} when unknown, so
     * the caller can report the error with its own line context.
     *
     * <p>Spaces and hyphens are normalised to underscores, so spellings such as {@code lay segment} or
     * {@code lay-segment} from hand-written DNA are tolerated without every command listing its own
     * aliases.</p>
     */
    public static GeneAction parse(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim().toLowerCase().replace(' ', '_').replace('-', '_');
        for (GeneAction action : values()) {
            if (action.canonicalName.equals(normalized)) {
                return action;
            }
        }
        return null;
    }

    /** All canonical spellings except {@link #EMPTY}, for error messages. */
    public static List<String> allCanonicalNames() {
        List<String> names = new ArrayList<String>();
        for (GeneAction action : values()) {
            if (action != EMPTY) names.add(action.canonicalName);
        }
        return names;
    }
}
