package site.klade.simulation.condition;

import org.junit.jupiter.api.Test;
import site.klade.simulation.Index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Conformance suite for the condition grammar (task 0001, Piece B).
 *
 * <p>Covers the three contracts: precedence, structural type safety, and canonical round-trip.</p>
 */
class ConditionParserTest {

    // ------------------------------------------------------------ round-trip

    private static void assertRoundTrip(String text) {
        Cond parsed = ConditionParser.parse(text);
        assertThat(parsed).as("parse(%s)", text).isNotNull();
        Cond reparsed = ConditionParser.parse(parsed.toString());
        assertThat(reparsed).as("reparse of %s -> %s", text, parsed).isEqualTo(parsed);
    }

    @Test
    void roundTripSimpleComparison() {
        assertRoundTrip("Mrph[1] > 1.0");
        assertRoundTrip("Mrph[2] != 0.5");
        assertRoundTrip("-1.5 <= 2");
    }

    @Test
    void roundTripNestedBooleans() {
        assertRoundTrip("Mrph[1] > 1.0 AND Mrph[2] < 0.5 OR Mrph[3] >= 2.0");
        assertRoundTrip("Mrph[1] > 1.0 AND (Mrph[2] < 0.5 OR Mrph[3] >= 2.0)");
        assertRoundTrip("NOT Mrph[1] > 1.0");
        assertRoundTrip("NOT (Mrph[1] > 1.0 AND Mrph[2] < 0.5)");
        assertRoundTrip("Mrph[1] > 1.0 XOR Mrph[2] > 1.0");
    }

    /**
     * The regression that motivates the right-operand paren rule: these two trees have identical
     * left-associative flat renderings unless parens are emitted for equal-precedence right children.
     */
    @Test
    void associativityIsPreservedThroughRoundTrip() {
        Cond rightNested = new BoolOp(
                new Compare(new MorphogenRef(new Index(1)), CmpOp.GT, new Literal(1.0f)),
                BoolOpKind.AND,
                new BoolOp(
                        new Compare(new MorphogenRef(new Index(2)), CmpOp.GT, new Literal(1.0f)),
                        BoolOpKind.AND,
                        new Compare(new MorphogenRef(new Index(3)), CmpOp.GT, new Literal(1.0f))));
        Cond leftNested = new BoolOp(
                new BoolOp(
                        new Compare(new MorphogenRef(new Index(1)), CmpOp.GT, new Literal(1.0f)),
                        BoolOpKind.AND,
                        new Compare(new MorphogenRef(new Index(2)), CmpOp.GT, new Literal(1.0f))),
                BoolOpKind.AND,
                new Compare(new MorphogenRef(new Index(3)), CmpOp.GT, new Literal(1.0f)));

        assertThat(leftNested).isNotEqualTo(rightNested);
        assertThat(ConditionParser.parse(leftNested.toString())).isEqualTo(leftNested);
        assertThat(ConditionParser.parse(rightNested.toString())).isEqualTo(rightNested);
    }

    // ------------------------------------------------------------ precedence

    @Test
    void andBindsTighterThanOr() {
        Cond parsed = ConditionParser.parse("Mrph[1] > 1.0 OR Mrph[2] > 1.0 AND Mrph[3] > 1.0");
        Cond expected = new BoolOp(
                new Compare(new MorphogenRef(new Index(1)), CmpOp.GT, new Literal(1.0f)),
                BoolOpKind.OR,
                new BoolOp(
                        new Compare(new MorphogenRef(new Index(2)), CmpOp.GT, new Literal(1.0f)),
                        BoolOpKind.AND,
                        new Compare(new MorphogenRef(new Index(3)), CmpOp.GT, new Literal(1.0f))));
        assertThat(parsed).isEqualTo(expected);
    }

    /** NOT is looser than comparison here (opposite of C/Java): NOT (Mrph[1] > 1.0). */
    @Test
    void notAppliesToTheWholeComparison() {
        Cond parsed = ConditionParser.parse("NOT Mrph[1] > 1.0");
        Cond expected = new Not(new Compare(new MorphogenRef(new Index(1)), CmpOp.GT, new Literal(1.0f)));
        assertThat(parsed).isEqualTo(expected);
    }

    @Test
    void parenthesesOverridePrecedence() {
        Cond parsed = ConditionParser.parse("(Mrph[1] > 1.0 OR Mrph[2] > 1.0) AND Mrph[3] > 1.0");
        assertThat(parsed).isInstanceOf(BoolOp.class);
        assertThat(((BoolOp) parsed).kind).isEqualTo(BoolOpKind.AND);
        assertThat(((BoolOp) parsed).left).isInstanceOf(BoolOp.class);
    }

    // ------------------------------------------------------------ lexing

    @Test
    void morphogenSpellingsAreAcceptedAndCanonicalised() {
        Cond expected = new Compare(new MorphogenRef(new Index(1)), CmpOp.GT, new Literal(1.0f));
        assertThat(ConditionParser.parse("Mrph[1] > 1.0")).isEqualTo(expected);
        assertThat(ConditionParser.parse("Morphogen[1] > 1.0")).isEqualTo(expected);
        assertThat(ConditionParser.parse("Mph[1] > 1.0")).isEqualTo(expected);
        assertThat(ConditionParser.parse("mrph[ 1 ] > 1.0")).isEqualTo(expected);
        assertThat(expected.toString()).contains("Morphogen[1]");
    }

    @Test
    void hierarchicalMorphogenIdsAreSupported() {
        Cond expected = new Compare(
                new MorphogenRef(Index.parse("1.1")), CmpOp.GT, new Literal(1.0f));
        assertThat(ConditionParser.parse("Morphogen[1.1] > 1.0")).isEqualTo(expected);
        assertThat(ConditionParser.parse("Mrph[1.1] > 1.0")).isEqualTo(expected);
        assertThat(expected.toString()).contains("Morphogen[1.1]");
        // Canonical form must round-trip, including a multi-level id with a negative segment.
        Cond deep = ConditionParser.parse("Morphogen[2.1.-3] > 0.5");
        assertThat(ConditionParser.parse(deep.toString())).isEqualTo(deep);
    }

    @Test
    void prefixParsingStopsBeforeTheAction() {
        String[] stops = {"become", "lay_segment", "express"};
        String line = "if Mrph[2] > 1.0 become muscle";
        ConditionPrefix prefix = ConditionParser.parsePrefix(line, stops);
        assertThat(prefix.condition).isNotNull();
        assertThat(line.substring(prefix.end).trim()).startsWith("become");
        assertThat(prefix.condition)
                .isEqualTo(ConditionParser.parse("Mrph[2] > 1.0"));
    }

    @Test
    void prefixParsingWithoutAConditionYieldsNullAtPositionZero() {
        String[] stops = {"become", "lay_segment", "express"};
        ConditionPrefix prefix = ConditionParser.parsePrefix("become muscle", stops);
        assertThat(prefix.condition).isNull();
        assertThat(prefix.end).isZero();
    }

    @Test
    void stopWordsAreMatchedAtWordBoundariesOnly() {
        String[] stops = {"express"};
        // "expression" must not be treated as the stop word "express".
        assertThatThrownBy(() -> ConditionParser.parsePrefix("expression > 1.0", stops))
                .isInstanceOf(ConditionParseException.class);
    }

    @Test
    void numericArgumentsShareTheConditionLexer() {
        // A literal argument, as in `become muscle length 40%`.
        ConditionParser.NumberPrefix literal = ConditionParser.parseNumber("40%");
        assertThat(literal.value).isEqualTo(new Literal(40f));
        assertThat(literal.end).isEqualTo(2);

        // A reference argument, as in `express Morphogen[1] amount 2.0`.
        ConditionParser.NumberPrefix reference = ConditionParser.parseNumber("Morphogen[1] amount 2.0");
        assertThat(reference.value).isEqualTo(new MorphogenRef(new Index(1)));
        assertThat("Morphogen[1] amount 2.0".substring(reference.end).trim())
                .isEqualTo("amount 2.0");

        // Signed and fractional forms.
        assertThat(ConditionParser.parseNumber("-1.5").value).isEqualTo(new Literal(-1.5f));

        // A boolean is not a number.
        assertThatThrownBy(() -> ConditionParser.parseNumber("(Morphogen[1] > 1.0)"))
                .isInstanceOf(ConditionParseException.class);
    }

    @Test
    void commentsAndNewlinesAreIgnored() {
        Cond parsed = ConditionParser.parse(
                "# muscule gate\nMrph[1] > 1.0  # trailing\nAND Mrph[2] < 0.5\n");
        assertThat(parsed).isInstanceOf(BoolOp.class);
    }

    @Test
    void nullAndBlankMeanUnconditional() {
        assertThat(ConditionParser.parse(null)).isNull();
        assertThat(ConditionParser.parse("")).isNull();
        assertThat(ConditionParser.parse("   \n\t ")).isNull();
        assertThat(ConditionParser.parse("# only a comment")).isNull();
    }

    // ------------------------------------------------------------ type safety

    @Test
    void notRejectsANumericOperand() {
        assertThatThrownBy(() -> ConditionParser.parse("NOT 5.0"))
                .isInstanceOf(ConditionParseException.class);
    }

    @Test
    void comparisonRejectsABooleanOperand() {
        assertThatThrownBy(() -> ConditionParser.parse("(Mrph[1] > 1.0) > 1.0"))
                .isInstanceOf(ConditionParseException.class);
        assertThatThrownBy(() -> ConditionParser.parse("Mrph[1] > (Mrph[2] > 1.0)"))
                .isInstanceOf(ConditionParseException.class);
    }

    @Test
    void bareMorphogenWithoutComparisonIsRejected() {
        assertThatThrownBy(() -> ConditionParser.parse("Mrph[1]"))
                .isInstanceOf(ConditionParseException.class);
    }

    @Test
    void singleEqualsIsDiagnosedAsLikelyTypo() {
        assertThatThrownBy(() -> ConditionParser.parse("Mrph[1] = 1.0"))
                .isInstanceOf(ConditionParseException.class)
                .hasMessageContaining("==");
    }

    // ------------------------------------------------------------ malformed

    @Test
    void malformedInputsThrowWithPosition() {
        String[] bad = {
                "Mrph[1] > 1.0 AND",
                "Mrph[1] > 1.0 AND (Mrph[2] < 0.5",
                "(Mrph[1] > 1.0))",
                "Mrph[] > 1.0",
                "Mrph[1 > 1.0",
                "Mrph[1] > ",
                "1.0 > 0.5 junk",
                ">> 1.0",
                "Mrph[1] @ 2.0",
                "Unknown[1] > 0.0",
        };
        for (String text : bad) {
            assertThatThrownBy(() -> ConditionParser.parse(text))
                    .as("should reject: %s", text)
                    .isInstanceOf(ConditionParseException.class);
        }
    }

    @Test
    void malformedNumberIsRejected() {
        assertThatThrownBy(() -> ConditionParser.parse("Mrph[1] > 1.0.5e"))
                .isInstanceOf(ConditionParseException.class);
    }

    @Test
    void excessiveParenthesisNestingIsRejectedNotStackOverflow() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) sb.append('(');
        sb.append("Mrph[1] > 1.0");
        for (int i = 0; i < 500; i++) sb.append(')');
        assertThatThrownBy(() -> ConditionParser.parse(sb.toString()))
                .isInstanceOf(ConditionParseException.class)
                .hasMessageContaining("too deep");
    }

    /**
     * The tree-depth invariant. A flat {@code a AND b AND c …} chain is assembled by the operator
     * loops without recursion, so a recursion-only guard never sees it — this is the regression that
     * produced a {@code StackOverflowError} during {@code toString()} of a tree the parser accepted.
     */
    @Test
    void tooDeepAnAndChainIsRejected() {
        ConditionParser.parse(andChainText(62));   // depth 64 == MAX_TREE_DEPTH, accepted
        assertThatThrownBy(() -> ConditionParser.parse(andChainText(63)))
                .isInstanceOf(ConditionParseException.class)
                .hasMessageContaining("too deep");
    }

    /**
     * Self-consistency: any tree the parser <i>produces</i> must re-parse from its own canonical
     * emission. {@code Not} and right operands parenthesize unconditionally, so a parser depth limit
     * that counted those parens as tree depth would reject its own output — the bug this pins down.
     */
    @Test
    void parserOutputAlwaysReparses() {
        String[] inputs = {
                "Mrph[1] > 1.0",
                "NOT Mrph[1] > 1.0",
                "NOT (Mrph[1] > 1.0 AND Mrph[2] < 0.5)",
                "Mrph[1] > 1.0 AND (Mrph[2] > 1.0 AND Mrph[3] > 1.0)",
                "(Mrph[1] > 1.0 AND Mrph[2] > 1.0) AND Mrph[3] > 1.0",
                "Mrph[1] > 1.0 XOR Mrph[2] != 0.5 OR Mrph[3] == 2.0",
        };
        for (String input : inputs) {
            Cond parsed = ConditionParser.parse(input);
            assertThat(parsed).as("parse(%s)", input).isNotNull();
            assertThat(ConditionParser.parse(parsed.toString()))
                    .as("reparse of %s (%s)", input, parsed)
                    .isEqualTo(parsed);
        }
    }

    /** A left-associative AND chain of {@code ands} operators. A leaf {@code Compare} has depth 2. */
    private static String andChainText(int ands) {
        Cond chain = new Compare(new MorphogenRef(new Index(1)), CmpOp.GT, new Literal(1.0f));
        for (int i = 0; i < ands; i++) {
            chain = new BoolOp(chain, BoolOpKind.AND,
                    new Compare(new MorphogenRef(new Index(2)), CmpOp.GT, new Literal(2.0f)));
        }
        return chain.toString();
    }

    @Test
    void exceptionCarriesPosition() {
        try {
            ConditionParser.parse("Mrph[1] > ");
            org.junit.jupiter.api.Assertions.fail("expected ConditionParseException");
        } catch (ConditionParseException e) {
            assertThat(e.getPosition()).isGreaterThanOrEqualTo(0);
        }
    }

    // ------------------------------------------------------------ structure

    @Test
    void nodesAreValueEqualAndImmutable() {
        Cond a = ConditionParser.parse("Mrph[1] > 1.0 AND Mrph[2] < 0.5");
        Cond b = ConditionParser.parse("Mrph[1] > 1.0 AND Mrph[2] < 0.5");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(ConditionParser.parse("Mrph[1] >= 1.0 AND Mrph[2] < 0.5")).isNotEqualTo(a);
    }
}
