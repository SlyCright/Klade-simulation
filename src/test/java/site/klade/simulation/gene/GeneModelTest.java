package site.klade.simulation.gene;

import org.junit.jupiter.api.Test;
import site.klade.simulation.condition.ConditionParser;
import site.klade.simulation.condition.Cond;
import site.klade.simulation.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Conformance suite for the typed gene-argument model (plan Step 1).
 *
 * <p>Pins the step's central invariant: <b>an invalid gene is unconstructable</b>. Every way of
 * producing a gene — direct construction, setter, action change — must reject arguments that contradict
 * the action's declared signature.</p>
 */
class GeneModelTest {

    private static Gene become(String typeName, GeneArg... tail) {
        List<GeneArg> args = new ArrayList<GeneArg>();
        args.add(new ElementTypeArg(ElementType.parse(typeName)));
        args.addAll(Arrays.asList(tail));
        return new Gene(new Index(1), null, GeneAction.BECOME, args);
    }

    // ------------------------------------------------------------ signatures

    @Test
    void emptyGeneHasNoArgumentsAndEmitsOnlyItsIndex() {
        Gene gene = new Gene(new Index(12));
        assertThat(gene.isEmpty()).isTrue();
        assertThat(gene.getAction()).isEqualTo(GeneAction.EMPTY);
        assertThat(gene.toString()).isEqualTo("12.");
    }

    @Test
    void becomeHasDependentArityFollowingTheElementType() {
        assertThat(become("friction_node").toString())
                .isEqualTo("1. become friction_node");
        assertThat(become("muscle", new NumberArg(40f)).toString())
                .isEqualTo("1. become muscle length 40%");
        assertThat(become("rhythm_node", new NumberArg(40f)).toString())
                .isEqualTo("1. become rhythm_node period 40");
    }

    @Test
    void laySegmentAndExpressRenderTheirUnitsAndLabels() {
        Gene lay = new Gene(new Index(2), null, GeneAction.LAY_SEGMENT,
                Collections.<GeneArg>singletonList(new NumberArg(30f)));
        assertThat(lay.toString()).isEqualTo("2. lay_segment 30°");

        Gene express = new Gene(new Index(5), null, GeneAction.EXPRESS,
                Arrays.<GeneArg>asList(new ReferenceArg(Index.parse("1.1")), new NumberArg(2f)));
        // Integral values are emitted without a trailing ".0" (plan 2.1 rule 9), so 2.0f renders as "2"
        // and 0.8f keeps its fraction.
        assertThat(express.toString()).isEqualTo("5. express Morphogen[1.1] amount 2");
        assertThat(new NumberArg(0.8f).toString()).isEqualTo("0.8");
    }

    @Test
    void conditionIsEmittedBetweenIndexAndAction() {
        Cond condition = ConditionParser.parse("Morphogen[1] > 1.0");
        Gene gene = new Gene(new Index(3), condition, GeneAction.BECOME,
                Arrays.<GeneArg>asList(new ElementTypeArg(ElementType.MUSCLE), new NumberArg(40f)));
        assertThat(gene.toString())
                .isEqualTo("3. if Morphogen[1] > 1.0 become muscle length 40%");
    }

    // ------------------------------------------------------------ the invariant

    @Test
    void argumentCountMustMatchTheSignature() {
        // muscle requires a length
        assertThatThrownBy(() -> become("muscle"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count mismatch");
        // friction_node takes none
        assertThatThrownBy(() -> become("friction_node", new NumberArg(1f)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count mismatch");
    }

    @Test
    void argumentKindMustMatchTheSpec() {
        // A reference where a number belongs.
        assertThatThrownBy(() -> become("muscle", new ReferenceArg(new Index(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not accept");
        // A number where the element type belongs.
        assertThatThrownBy(() -> new Gene(new Index(1), null, GeneAction.BECOME,
                Collections.<GeneArg>singletonList(new NumberArg(1f))))
                .isInstanceOf(IllegalArgumentException.class);
        // An element type where express's morphogen reference belongs.
        assertThatThrownBy(() -> new Gene(new Index(1), null, GeneAction.EXPRESS,
                Arrays.<GeneArg>asList(new ElementTypeArg(ElementType.MUSCLE), new NumberArg(1f))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changingTheActionRevalidatesTheArguments() {
        Gene gene = become("muscle", new NumberArg(40f));
        // BECOME(muscle, number) is valid, but EMPTY takes no arguments.
        assertThatThrownBy(() -> gene.setAction(GeneAction.EMPTY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count mismatch");
    }

    @Test
    void changingTheElementTypeRevalidatesTheTail() {
        Gene gene = become("muscle", new NumberArg(40f));
        // Replace the type with one that takes no parameters, keeping the stale tail.
        assertThatThrownBy(() -> gene.setArguments(Arrays.<GeneArg>asList(
                new ElementTypeArg(ElementType.FRICTION_NODE), new NumberArg(40f))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count mismatch");
    }

    @Test
    void nonFiniteNumbersAreRejectedAtConstruction() {
        assertThatThrownBy(() -> new NumberArg(Float.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NumberArg(Float.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void argumentListIsNotModifiableFromOutside() {
        Gene gene = become("muscle", new NumberArg(40f));
        assertThatThrownBy(() -> gene.getArguments().add(new NumberArg(1f)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ------------------------------------------------------------ value semantics

    @Test
    void genesAreValueEqualAndCopyIndependently() {
        Gene a = become("muscle", new NumberArg(40f));
        Gene b = become("muscle", new NumberArg(40f));
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(become("muscle", new NumberArg(41f)));

        Gene copy = new Gene(a);
        copy.setArguments(Arrays.<GeneArg>asList(
                new ElementTypeArg(ElementType.MUSCLE), new NumberArg(10f)));
        assertThat(a.getArguments()).isNotEqualTo(copy.getArguments());
    }

    @Test
    void indexCanonicalFormKeepsItsTrailingDotButReferencesDoNot() {
        assertThat(new Index(12).toString()).isEqualTo("12.");
        assertThat(new ReferenceArg(Index.parse("1.1")).toString()).isEqualTo("Morphogen[1.1]");
        assertThat(new Index(12).toString()).endsWith(".");
    }
}
