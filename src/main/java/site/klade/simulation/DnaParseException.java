package site.klade.simulation;

/**
 * Thrown when a DNA string cannot be parsed.
 *
 * <p>Carries the 1-based {@link #getLineNumber() line number} and the offending
 * {@link #getLineText() text} so a malformed genome is reported precisely. This replaces the previous
 * behaviour of silently skipping unparsable lines: a genome that loses content without saying so is far
 * worse than one that refuses to load, because the loss surfaces only as inexplicable simulation
 * behaviour much later.</p>
 */
public class DnaParseException extends IllegalArgumentException {

    private final int lineNumber;

    private final String lineText;

    public DnaParseException(String message, int lineNumber, String lineText) {
        super("DNA line " + lineNumber + ": " + message + " — \"" + lineText + "\"");
        this.lineNumber = lineNumber;
        this.lineText = lineText;
    }

    public DnaParseException(String message, int lineNumber, String lineText, Throwable cause) {
        super("DNA line " + lineNumber + ": " + message + " — \"" + lineText + "\"", cause);
        this.lineNumber = lineNumber;
        this.lineText = lineText;
    }

    /** 1-based line number within the parsed DNA text. */
    public int getLineNumber() {
        return lineNumber;
    }

    /** The raw text of the offending line. */
    public String getLineText() {
        return lineText;
    }
}
