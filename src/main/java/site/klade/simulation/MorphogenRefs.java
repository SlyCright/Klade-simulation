package site.klade.simulation;

import site.klade.simulation.condition.BoolOp;
import site.klade.simulation.condition.Compare;
import site.klade.simulation.condition.Cond;
import site.klade.simulation.condition.MorphogenRef;
import site.klade.simulation.condition.Not;
import site.klade.simulation.gene.Gene;
import site.klade.simulation.gene.ReferenceArg;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Collects every morphogen index a genome refers to, across <b>both</b> places a reference can appear:
 * an {@code express} argument ({@link ReferenceArg}) and a condition ({@link MorphogenRef}).
 *
 * <p>Living in one class matters because the two spellings are separate types in separate packages; if
 * each caller walked the structures itself, a reference hidden inside a nested condition would sooner or
 * later be missed, and the morphogen would be silently deleted from a genome that still uses it.</p>
 */
public final class MorphogenRefs {

    private MorphogenRefs() {
    }

    /** Every morphogen index referenced by any gene, in no particular order. */
    public static Set<Index> collect(Genome genome) {
        Set<Index> referenced = new LinkedHashSet<Index>();
        if (genome == null) {
            return referenced;
        }
        for (Gene gene : genome.getGenes()) {
            collect(gene, referenced);
        }
        return referenced;
    }

    /** Every morphogen index referenced by one gene: its arguments and its condition. */
    public static Set<Index> collect(Gene gene) {
        Set<Index> referenced = new LinkedHashSet<Index>();
        collect(gene, referenced);
        return referenced;
    }

    private static void collect(Gene gene, Set<Index> into) {
        if (gene == null) {
            return;
        }
        for (Object argument : gene.getArguments()) {
            if (argument instanceof ReferenceArg) {
                into.add(((ReferenceArg) argument).morphogen);
            }
        }
        collect(gene.getConditions(), into);
    }

    /** Walks a condition tree, gathering every {@link MorphogenRef} it contains. */
    public static Set<Index> collectCondition(Cond condition) {
        Set<Index> referenced = new LinkedHashSet<Index>();
        collect(condition, referenced);
        return referenced;
    }

    private static void collect(Cond condition, Set<Index> into) {
        if (condition == null) {
            return;
        }
        if (condition instanceof Compare) {
            Compare compare = (Compare) condition;
            collectExpr(compare.left, into);
            collectExpr(compare.right, into);
            return;
        }
        if (condition instanceof Not) {
            collect(((Not) condition).inner, into);
            return;
        }
        if (condition instanceof BoolOp) {
            BoolOp boolOp = (BoolOp) condition;
            collect(boolOp.left, into);
            collect(boolOp.right, into);
            return;
        }
        throw new IllegalArgumentException(
                "unhandled condition node: " + condition.getClass().getName());
    }

    /**
     * Collects from a numeric operand of a comparison. A {@link MorphogenRef} is a reference; a
     * {@code Literal} contributes nothing. The parameter is typed {@code Object} because the condition
     * package exposes {@code Expr} only as a marker for its two implementations.
     */
    private static void collectExpr(Object expr, Set<Index> into) {
        if (expr instanceof MorphogenRef) {
            into.add(((MorphogenRef) expr).id);
        }
    }

    /** The same set, sorted by dotted index — the order morphogen definitions are kept in. */
    public static Set<Index> sorted(Set<Index> indices) {
        return new TreeSet<Index>(indices);
    }

    /** An empty, immutable set — handy for callers that must not allocate. */
    public static Set<Index> none() {
        return Collections.emptySet();
    }
}
