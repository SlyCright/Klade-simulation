package site.klade.simulation.condition;

/**
 * Thrown when a condition expression cannot be parsed.
 *
 * <p>Deliberately <b>unchecked</b> (extends {@link IllegalArgumentException}): conditions are parsed
 * deep inside genome deserialization, and a checked exception would force {@code throws} clauses up
 * through {@code Gene}/{@code Genome} and every GWT call site for no benefit. Callers that want to
 * tolerate bad input already guard with try/catch per line (see {@code GenomeParser.parseGenes}).</p>
 *
 * <p>Carries the character {@link #getPosition() position} of the offending token so malformed
 * genomes can be reported precisely instead of being silently dropped.</p>
 */
public class ConditionParseException extends IllegalArgumentException {

    private final int position;

    public ConditionParseException(String message, int position) {
        super(message + " (at position " + position + ")");
        this.position = position;
    }

    /** 0-based character offset in the parsed text where the failure was detected. */
    public int getPosition() {
        return position;
    }
}
