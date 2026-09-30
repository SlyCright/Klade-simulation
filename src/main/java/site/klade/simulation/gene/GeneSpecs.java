package site.klade.simulation.gene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The single place that knows how an action's arguments are shaped.
 *
 * <p>Two jobs, both small but load-bearing:</p>
 * <ol>
 *   <li>{@link #specsOf} stitches an action's {@link GeneAction#leadingSpecs()} with the value-dependent
 *       tail contributed by its element type — this is what implements {@code become}'s dependent
 *       arity.</li>
 *   <li>{@link #validate} enforces that a concrete argument list matches that signature in count and
 *       kind.</li>
 * </ol>
 *
 * <p>Together they make the plan's Step 1 invariant true: <b>an invalid gene is unconstructable</b>.
 * {@link Gene} calls {@link #validate} in its constructor and in every setter, so no code path — parser,
 * mutation operator or hand-written test — can produce a gene whose arguments contradict its action.
 * "Mutations must keep the genome syntactically valid" therefore stops being a property to hope for and
 * becomes a property of construction.</p>
 */
public final class GeneSpecs {

    private GeneSpecs() {
    }

    /** The complete ordered signature of a gene, derived from its action and its own arguments. */
    public static List<ArgSpec> specsOf(Gene gene) {
        if (gene == null) throw new IllegalArgumentException("gene is null");
        return specsOf(gene.getAction(), gene.getArguments());
    }

    /**
     * The complete ordered signature for an action together with a candidate argument list.
     *
     * <p>Most actions have a fixed signature that does not depend on their arguments at all. Only
     * {@link GeneAction#BECOME} needs the list: its first argument selects the element type, and that
     * type contributes the remaining slots. When the first argument is missing or is not an element
     * type, the signature is the leading specs alone — {@link #validate} then reports the mismatch,
     * which is the desired outcome rather than a second error path.</p>
     */
    public static List<ArgSpec> specsOf(GeneAction action, List<GeneArg> arguments) {
        if (action == null) throw new IllegalArgumentException("action is null");
        List<ArgSpec> leading = action.leadingSpecs();
        if (action != GeneAction.BECOME) return leading;
        List<ArgSpec> specs = new ArrayList<ArgSpec>(leading);
        if (arguments != null && !arguments.isEmpty() && arguments.get(0) instanceof ElementTypeArg) {
            specs.addAll(((ElementTypeArg) arguments.get(0)).type.parameterSpecs());
        }
        return Collections.unmodifiableList(specs);
    }

    /**
     * Validates an argument list against a signature.
     *
     * <p>Reports the first mismatch with both the expected and the actual kind, so a failure inside a
     * mutation loop identifies the offending command rather than merely asserting "invalid genome".</p>
     *
     * @throws IllegalArgumentException if the counts differ, or any argument's type contradicts its spec
     */
    public static void validate(List<ArgSpec> specs, List<GeneArg> arguments) {
        if (specs == null) {
            throw new IllegalArgumentException("signature is null");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments are null");
        }
        if (specs.size() != arguments.size()) {
            throw new IllegalArgumentException(
                    "argument count mismatch: signature expects " + specs.size()
                            + " but got " + arguments.size());
        }
        for (int i = 0; i < specs.size(); i++) {
            ArgSpec spec = specs.get(i);
            GeneArg argument = arguments.get(i);
            if (!accepts(spec, argument)) {
                throw new IllegalArgumentException(
                        "argument " + i + " (" + spec + ") does not accept "
                                + describe(argument));
            }
        }
    }

    /** Convenience: validate an argument list against an action's own signature. */
    public static void validate(GeneAction action, List<GeneArg> arguments) {
        validate(specsOf(action, arguments), arguments);
    }

    /** True when this argument is the kind its slot demands. */
    private static boolean accepts(ArgSpec spec, GeneArg argument) {
        if (spec instanceof NumberSpec) return argument instanceof NumberArg;
        if (spec instanceof ElementTypeSpec) return argument instanceof ElementTypeArg;
        if (spec instanceof ReferenceSpec) return argument instanceof ReferenceArg;
        return false;
    }

    private static String describe(GeneArg argument) {
        if (argument == null) return "null";
        return argument.getClass().getSimpleName();
    }
}
