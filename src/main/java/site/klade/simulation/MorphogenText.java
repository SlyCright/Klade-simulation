package site.klade.simulation;

/**
 * The one place that formats a morphogen reference for the DNA text.
 *
 * <p>Both spellings of a reference exist — {@link ReferenceArg} as a {@code express} argument and
 * {@code site.klade.simulation.condition.MorphogenRef} inside a condition — and they must render
 * identically, or the same reference written in two places would round-trip differently. Sharing this
 * helper is what guarantees that.</p>
 *
 * <p>Canonical form is the full word: {@code Morphogen[1.1]}. Abbreviations are never emitted (see
 * {@link ReferenceSpec}).</p>
 */
public final class MorphogenText {

    private MorphogenText() {
    }

    /**
     * The canonical reference text for a morphogen index, e.g. {@code Morphogen[1.1]}.
     *
     * <p>{@link Index#toString()} always ends with a dot ({@code "1.1."}), which is correct as a gene
     * line prefix but wrong inside brackets, so the trailing dot is stripped here rather than by each
     * caller.</p>
     */
    public static String reference(Index morphogen) {
        if (morphogen == null) {
            throw new IllegalArgumentException("morphogen index is null");
        }
        return "Morphogen[" + withoutTrailingDot(morphogen.toString()) + "]";
    }

    /** Strips {@link Index}'s canonical trailing dot, if present. */
    public static String withoutTrailingDot(String indexText) {
        if (indexText.endsWith(".")) {
            return indexText.substring(0, indexText.length() - 1);
        }
        return indexText;
    }
}
