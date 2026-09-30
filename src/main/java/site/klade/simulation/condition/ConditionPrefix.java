package site.klade.simulation.condition;

/**
 * A condition parsed from the beginning of a text, plus where it ended.
 *
 * <p>Exists because a gene line begins with a condition and continues with an action
 * ({@code if Morphogen[1] > 1.0 become muscle}), while {@link ConditionParser#parse} requires the whole
 * input to be a condition. {@code parsePrefix} reports what it consumed so the caller can carry on with
 * the action at {@link #end}.</p>
 */
public final class ConditionPrefix {

    /**
     * The parsed condition, or {@code null} when the text begins with a stop word — meaning the gene is
     * unconditional. Null is a normal, valid state here, exactly as it is for
     * {@link ConditionParser#parse}.
     */
    public final Cond condition;

    /** Index of the first character not part of the condition; the action starts here. */
    public final int end;

    public ConditionPrefix(Cond condition, int end) {
        this.condition = condition;
        this.end = end;
    }

    @Override
    public String toString() {
        return "ConditionPrefix(" + (condition == null ? "<none>" : condition.toString())
                + ", end=" + end + ")";
    }
}
