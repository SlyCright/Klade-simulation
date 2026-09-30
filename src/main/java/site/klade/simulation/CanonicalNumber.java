package site.klade.simulation;

/**
 * Canonical spelling of a number in DNA text.
 *
 * <p>The rule: {@link Float#toString(float)} — the shortest decimal string that round-trips exactly — with
 * a trailing {@code ".0"} dropped, so an integral value reads as {@code 40} rather than {@code 40.0}.
 * Removing the suffix loses no information, because re-parsing {@code "40"} yields the same float.</p>
 *
 * <p>This exists as one shared helper because the rule is needed in more than one place — a gene
 * argument, a morphogen ratio, a meta-gene value — and a second copy of it would eventually disagree with
 * the first, producing a genome whose text is not canonical.</p>
 *
 * <p>Note that a condition {@code Literal} deliberately does <b>not</b> use this: the grammar's own
 * examples write thresholds as {@code Morphogen[1] > 1.0}, so that context keeps {@code Float.toString}
 * verbatim. The distinction is intentional and visible here rather than accidental.</p>
 */
public final class CanonicalNumber {

    private CanonicalNumber() {
    }

    /** The canonical DNA spelling of a float, e.g. {@code 40}, {@code 0.8}, {@code -1.5}, {@code 30}. */
    public static String format(float value) {
        String text = Float.toString(value);
        if (text.endsWith(".0")) {
            return text.substring(0, text.length() - 2);
        }
        return text;
    }

    /** The canonical spelling of any numeric value, for callers holding a boxed {@link Number}. */
    public static String format(Object value) {
        if (value instanceof Number) {
            return format(((Number) value).floatValue());
        }
        return String.valueOf(value);
    }
}
