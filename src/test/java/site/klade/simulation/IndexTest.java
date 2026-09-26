package site.klade.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contract for {@link Index} — the hierarchical dotted index ("gene index") value type.
 *
 * <p>Key decisions encoded here (signed off before implementation):</p>
 * <ul>
 *   <li>Ordering is element-wise NUMERIC with shorter-prefix-first — NOT string lexicographic
 *       ("2.2" &lt; "2.10", "0." &lt; "0.-1."). Segment values may be 0 and negative.</li>
 *   <li>Canonical form is obtained via toString() and always ends with a dot ("2.1.");
 *       parse accepts both spellings.</li>
 *   <li>Indexes encode order only — no ownership/tree semantics, no tombstones.</li>
 *   <li>Pure immutable value. Structural derivations (segment access, parent/extend,
 *       increment/decrement, prefix relation) live in the web-app's {@code evolution}
 *       utilities; this test covers the value semantics + I/O only.</li>
 * </ul>
 */
@DisplayName("Index — hierarchical dotted gene index (value type)")
class IndexTest {

    private static Index idx(String s) {
        return Index.parse(s);
    }

    private static List<String> toStrings(List<Index> indexes) {
        List<String> out = new ArrayList<>();
        for (Index i : indexes) {
            out.add(i.toString());
        }
        return out;
    }

    // ---------------------------------------------------------------- construction

    @Test
    @DisplayName("terminal node: value only, no nested part")
    void terminalNode() {
        Index i = new Index(3);
        assertThat(i.getValue()).isEqualTo(3);
        assertThat(i.getNested()).isNull();
        assertThat(i.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("nested node: value + nested, e.g. new Index(2, new Index(1)) == \"2.1.\"")
    void nestedNode() {
        Index i = new Index(2, new Index(1));
        assertThat(i.getValue()).isEqualTo(2);
        assertThat(i.getNested()).isNotNull();
        assertThat(i.getNested().getValue()).isEqualTo(1);
        assertThat(i.getNested().isTerminal()).isTrue();
        assertThat(i.isTerminal()).isFalse();
    }

    // ---------------------------------------------------------------- string form

    @Test
    @DisplayName("terminal toString ends with a dot: \"3.\"")
    void terminalStringForm() {
        assertThat(new Index(3).toString()).isEqualTo("3.");
    }

    @Test
    @DisplayName("nested toString: value + nested, e.g. \"2.1.\"")
    void nestedStringForm() {
        assertThat(new Index(2, new Index(1)).toString()).isEqualTo("2.1.");
    }

    @Test
    @DisplayName("deep string form \"1.33.15.4.\" round-trips through parse")
    void deepStringRoundTrip() {
        String canonical = "1.33.15.4.";
        Index constructed = new Index(1, new Index(33, new Index(15, new Index(4))));
        assertThat(constructed.toString()).isEqualTo(canonical);
        assertThat(idx(canonical).toString()).isEqualTo(canonical);
        assertThat(idx("1.33.15.4").toString()).isEqualTo(canonical);
        assertThat(idx(canonical)).isEqualTo(constructed);
    }

    @Test
    @DisplayName("negative segments print with minus: \"0.-1.\"")
    void negativeStringForm() {
        assertThat(new Index(0, new Index(-1)).toString()).isEqualTo("0.-1.");
        assertThat(new Index(-1).toString()).isEqualTo("-1.");
    }

    @Test
    @DisplayName("zero segments print normally: \"1.0.\" (not \"1.0\")")
    void zeroSegmentStringForm() {
        assertThat(new Index(1, new Index(0)).toString()).isEqualTo("1.0.");
        assertThat(idx("1.0").toString()).isEqualTo("1.0.");
    }

    // ---------------------------------------------------------------- parse: valid

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @CsvSource({
            "0,           0.",
            "0.,          0.",
            "-1,          -1.",
            "3,           3.",
            "3.,          3.",
            "1.33.15.4,   1.33.15.4.",
            "1.33.15.4.,  1.33.15.4.",
            "0.-1.0,      0.-1.0.",
            "0.-1.0.,     0.-1.0.",
            "0.-1,        0.-1.",
            "2147483647,  2147483647."    // Integer.MAX_VALUE is the largest legal segment
    })
    @DisplayName("parse accepts with/without trailing dot and yields canonical form")
    void parseValid(String input, String expectedCanonical) {
        Index parsed = idx(input);
        assertThat(parsed).isNotNull();
        assertThat(parsed.toString()).isEqualTo(expectedCanonical);
    }

    @Test
    @DisplayName("parse trims whitespace around segments: \" 1. 2 \" == \"1.2.\"")
    void parseTrimsSegments() {
        assertThat(idx(" 1. 2 ").toString()).isEqualTo("1.2.");
        assertThat(idx(" 1. 2 ")).isEqualTo(idx("1.2"));
    }

    @Test
    @DisplayName("parse builds the correct chain: 1.33.15.4")
    void parseBuildsChain() {
        Index parsed = idx("1.33.15.4");
        assertThat(parsed.getValue()).isEqualTo(1);
        assertThat(parsed.getNested().getValue()).isEqualTo(33);
        assertThat(parsed.getNested().getNested().getValue()).isEqualTo(15);
        assertThat(parsed.getNested().getNested().getNested().getValue()).isEqualTo(4);
        assertThat(parsed.getNested().getNested().getNested().isTerminal()).isTrue();
        assertThat(parsed.isTerminal()).isFalse();
    }

    // ---------------------------------------------------------------- parse: invalid

    @Test
    @DisplayName("parse(null) throws IllegalArgumentException, never returns null")
    void parseNull() {
        assertThatThrownBy(() -> Index.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // "2147483648" = Integer.MAX_VALUE (2147483647) + 1 — out of int range; Integer.parseInt
    // rejects it with NumberFormatException, which IS-A IllegalArgumentException (pure JDK rule).
    @ParameterizedTest(name = "[{index}] rejects \"{0}\"")
    @ValueSource(strings = {"", " ", ".", "..", "...", "1..2", "1..", ".1", "abc",
            "1.2.x", "1.-", "1 2", "2147483648"})
    @DisplayName("parse rejects malformed input with IllegalArgumentException")
    void parseRejectsMalformed(String input) {
        assertThatThrownBy(() -> Index.parse(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------------------------------------------------------------- equality

    @Test
    @DisplayName("trailing dot is irrelevant for equality: parse(\"2.1\") == parse(\"2.1.\")")
    void trailingDotIrrelevantForEquality() {
        Index withDot = idx("2.1.");
        Index withoutDot = idx("2.1");
        assertThat(withDot).isEqualTo(withoutDot);
        assertThat(withDot.hashCode()).isEqualTo(withoutDot.hashCode());
    }

    @Test
    @DisplayName("constructor result equals parsed result")
    void constructedEqualsParsed() {
        Index constructed = new Index(0, new Index(-1));
        assertThat(constructed).isEqualTo(idx("0.-1"));
        assertThat(constructed.hashCode()).isEqualTo(idx("0.-1").hashCode());
    }

    @Test
    @DisplayName("different chains are not equal")
    void differentChainsNotEqual() {
        assertThat(idx("2.1.")).isNotEqualTo(idx("2.10."));
        assertThat(idx("2.1.")).isNotEqualTo(idx("2.1.1."));
        assertThat(idx("0.-1.")).isNotEqualTo(idx("0.1."));
        assertThat(idx("2.1.")).isNotEqualTo(idx("1.2."));
    }

    @Test
    @DisplayName("equality against null and foreign types is false")
    void equalityAgainstOtherTypes() {
        assertThat(idx("2.1.")).isNotEqualTo(null);
        assertThat(idx("2.1.")).isNotEqualTo("2.1.");
        assertThat(idx("2.1.")).isNotEqualTo(2);
    }

    // ---------------------------------------------------------------- ordering

    @Test
    @DisplayName("GOLDEN: sorts exactly as the user's step-3 example rows 1..8")
    void goldenStepThreeOrder() {
        // user's example (rows as written, "1.0" normalized to canonical "1.0."):
        // 1: 0. | 2: 0.-1. | 3: 0.0. | 4: 0.1. | 5: 1. | 6: 1.0. | 7: 2. | 8: 3.
        List<Index> indexes = new ArrayList<>();
        Collections.addAll(indexes, idx("1."), idx("0.0."), idx("3."), idx("0.-1."),
                idx("2."), idx("0."), idx("1.0."), idx("0.1."), idx("-1."), idx("-1.2."));
        Collections.sort(indexes);
        assertThat(toStrings(indexes)).containsExactly(
                "-1.", "-1.2.", "0.", "0.-1.", "0.0.", "0.1.", "1.", "1.0.", "2.", "3.");
    }

    @Test
    @DisplayName("GOLDEN: C3 option 1 — after deleting 0.-1. the survivor 0.-1.0. keeps well-defined order")
    void goldenC3OptionOneAfterDeletion() {
        // C3 original sequence: 1: 0. | 2: 0.-1. | 3: 0.-1.0. | 4: 0.0.
        List<Index> sequence = new ArrayList<>();
        Collections.addAll(sequence, idx("0.0."), idx("0.-1.0."), idx("0."), idx("0.-1."));
        Collections.sort(sequence);
        assertThat(toStrings(sequence)).containsExactly("0.", "0.-1.", "0.-1.0.", "0.0.");

        // option 1: delete the single item "0.-1." — no subtree semantics, "0.-1.0." survives.
        // (Real deletion belongs to Genome; here plain list removal on Index values is enough
        // to show ordering stays total for the orphan.)
        assertThat(sequence.remove(idx("0.-1."))).isTrue();

        assertThat(toStrings(sequence)).containsExactly("0.", "0.-1.0.", "0.0.");
    }

    @Test
    @DisplayName("numeric segment order, not string lexicographic: 2.2 < 2.10")
    void numericNotStringOrder() {
        assertThat(idx("2.2.")).isLessThan(idx("2.10."));
        assertThat(idx("2.10.")).isGreaterThan(idx("2.2."));
        // for reference, plain String would get this WRONG:
        assertThat("2.10.".compareTo("2.2.")).isNegative();
    }

    @Test
    @DisplayName("shorter prefix sorts first: 0. < 0.-1. < 0.-1.0.  and  1. < 1.0.")
    void shorterPrefixSortsFirst() {
        assertThat(idx("0.")).isLessThan(idx("0.-1."));
        assertThat(idx("0.-1.")).isLessThan(idx("0.-1.0."));
        assertThat(idx("1.")).isLessThan(idx("1.0."));
        assertThat(idx("2.1.")).isLessThan(idx("2.1.1."));
    }

    @Test
    @DisplayName("negative segments sort below zero: -1. < 0. < 0.-1. < 0.0.")
    void negativeSegmentsOrder() {
        assertThat(idx("-1.")).isLessThan(idx("0."));
        assertThat(idx("0.")).isLessThan(idx("0.-1."));
        assertThat(idx("0.-1.")).isLessThan(idx("0.0."));
        assertThat(idx("-2.")).isLessThan(idx("-1."));
    }

    @Test
    @DisplayName("compareTo == 0 exactly when equals; antisymmetric for distinct values")
    void compareToConsistentWithEquals() {
        assertThat(idx("2.1.").compareTo(idx("2.1"))).isZero();
        assertThat(idx("1.").compareTo(idx("2."))).isNegative();
        assertThat(idx("2.").compareTo(idx("1."))).isPositive();
        assertThat(idx("2.2.").compareTo(idx("2.10."))).isNegative();
    }

    @Test
    @DisplayName("ordering is antisymmetric across the golden set")
    void orderingAntisymmetric() {
        String[] golden = {"0.", "0.-1.", "0.-1.0.", "0.0.", "0.1.", "1.", "1.0.", "2.", "3."};
        for (String a : golden) {
            for (String b : golden) {
                assertThat(idx(a).compareTo(idx(b)))
                        .as("compare(%s, %s) must equal -compare(%s, %s)", a, b, b, a)
                        .isEqualTo(-idx(b).compareTo(idx(a)));
            }
        }
    }

}