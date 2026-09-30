package site.klade.simulation.gene;

import site.klade.simulation.Index;

import site.klade.simulation.condition.Cond;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One line of the DNA program: an {@link Index} (its identity and order), an optional
 * {@link Cond} guard, a {@link GeneAction}, and that action's typed arguments.
 *
 * <p><b>Why arguments are typed rather than a {@code String}.</b> The previous representation kept
 * parameters as raw text, which forced every consumer to re-parse them by string search and made it
 * impossible to mutate them: the rank-linear operator needs a typed value and a domain to operate on,
 * and an untyped blob cannot supply either. With typed arguments the mutation operator dispatches on the
 * argument kind, and {@link GeneSpecs#validate} guarantees at construction time that the arguments match
 * the action's declared signature — so no parse or mutation can produce a semantically impossible
 * command.</p>
 *
 * <p><b>An empty gene is a gene with {@link GeneAction#EMPTY}.</b> It consumes a tick and does nothing.
 * This is how "wait" is represented (see {@link GeneAction}), and it is a perfectly ordinary member of a
 * genome rather than a special case.</p>
 *
 * <p>Mutable in the same way as before (the structural and parameter mutators adjust genes in place),
 * but every mutator re-validates, so mutation cannot leave the genome inconsistent.</p>
 */
public class Gene {

    private Index index;

    /** Null means unconditional. Mirrors {@code ConditionParser.parse} returning null for blank input. */
    private Cond conditions;

    private GeneAction action;

    /** Immutable copy; never null. Validated against the action's signature on every assignment. */
    private List<GeneArg> arguments;

    public Gene(Index index, Cond conditions, GeneAction action, List<GeneArg> arguments) {
        this.index = index;
        this.conditions = conditions;
        this.action = action;
        setArguments(arguments);
    }

    /**
     * Convenience constructor for an empty gene: an index that does nothing this tick.
     */
    public Gene(Index index) {
        this(index, null, GeneAction.EMPTY, Collections.<GeneArg>emptyList());
    }

    /**
     * Deep-enough copy constructor. {@link Index} and {@link Cond} are immutable value objects, so they
     * are shared; the argument list is copied so the two genes cannot mutate each other's arguments.
     */
    public Gene(Gene other) {
        this.index = other.index;
        this.conditions = other.conditions;
        this.action = other.action;
        this.arguments = other.arguments;
    }

    public Cond getConditions() {
        return conditions;
    }

    public void setConditions(Cond conditions) {
        this.conditions = conditions;
    }

    public GeneAction getAction() {
        return action;
    }

    public void setAction(GeneAction action) {
        this.action = action;
        // The signature depends on the action, so the arguments must be re-checked against the new one.
        setArguments(this.arguments);
    }

    /** The action's arguments, in order. Never null; the list itself is not modifiable. */
    public List<GeneArg> getArguments() {
        return arguments;
    }

    /**
     * Replaces the argument list after validating it against the current action's signature.
     *
     * @throws IllegalArgumentException if the count or any argument's kind contradicts the signature
     */
    public void setArguments(List<GeneArg> arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("gene arguments are null; use an empty list for none");
        }
        List<GeneArg> copy = new ArrayList<GeneArg>(arguments);
        GeneSpecs.validate(this.action, copy);
        this.arguments = Collections.unmodifiableList(copy);
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    /** True when this gene does nothing this tick. */
    public boolean isEmpty() {
        return action == GeneAction.EMPTY;
    }

    /**
     * The canonical single-line DSL form:
     * <pre>
     *   &lt;index&gt;[ if &lt;condition&gt;] &lt;action&gt;[ &lt;arg&gt;]*
     * </pre>
     *
     * <p>An {@link GeneAction#EMPTY} gene emits only its index, which is how an empty line in the DNA
     * is written. {@link Index#toString()} already ends with a dot ({@code "1.12."}), so the index and
     * the following token are naturally separated by one space.</p>
     *
     * <p>Numeric arguments are spelled with their spec's label and unit ({@code length 40%},
     * {@code 30°}), which is why emission consults {@link GeneSpecs#specsOf} rather than calling
     * {@code toString()} on each argument in isolation: a {@link NumberArg} knows its value but its
     * spec is what knows how to make that value readable.</p>
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(index);
        if (action == GeneAction.EMPTY) {
            return result.toString();
        }
        if (conditions != null) {
            result.append(" if ").append(conditions);
        }
        result.append(' ').append(action.canonicalName());
        List<ArgSpec> specs = GeneSpecs.specsOf(this);
        for (int i = 0; i < arguments.size(); i++) {
            result.append(' ').append(render(specs.get(i), arguments.get(i)));
        }
        return result.toString();
    }

    /**
     * Renders one argument using its spec. Non-numeric arguments are self-describing; a number is
     * preceded by its label when it has one and followed by its unit when it has one.
     */
    private static String render(ArgSpec spec, GeneArg argument) {
        if (!(spec instanceof NumberSpec)) return argument.toString();
        NumberSpec numberSpec = (NumberSpec) spec;
        StringBuilder result = new StringBuilder();
        if (numberSpec.label != null) result.append(numberSpec.label).append(' ');
        result.append(argument);
        result.append(numberSpec.unit);
        return result.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Gene)) {
            return false;
        }
        Gene gene = (Gene) other;
        return action == gene.action
                && arguments.equals(gene.arguments)
                && (index == null ? gene.index == null : index.equals(gene.index))
                && (conditions == null ? gene.conditions == null : conditions.equals(gene.conditions));
    }

    @Override
    public int hashCode() {
        int result = index == null ? 0 : index.hashCode();
        result = 31 * result + (conditions == null ? 0 : conditions.hashCode());
        result = 31 * result + action.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }
}
