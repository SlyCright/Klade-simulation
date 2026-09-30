package site.klade.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The one place that knows how a meta-gene is named in the DNA and what type it holds.
 *
 * <p><b>Why a registry rather than reflection.</b> The previous parser used
 * {@code MetaGenes.class.getDeclaredField(textName)} and the writer used
 * {@code getDeclaredFields()}. Both are problems:</p>
 *
 * <ul>
 *   <li><b>Reflection silently lost data.</b> The DSL text {@code InitialAngle} and the Java field
 *       {@code initialAngle} differ in case, and {@code getDeclaredField} is case-sensitive — so parsing
 *       the canonical example threw {@code NoSuchFieldException}, hit a {@code catch { continue }}, and
 *       discarded the initial angle without any error. Deriving both directions from one registry makes
 *       that class of mismatch impossible.</li>
 *   <li><b>Reflection made output non-canonical.</b> {@code getDeclaredFields()} does not guarantee
 *       order, so the emitted DNA could vary between JVMs. This project requires deterministic
 *       simulation, and a genome string that changes shape across environments undermines it.
 *       {@link #all()} fixes the order explicitly.</li>
 *   <li><b>Reflection is not GWT-compatible.</b> This class lives in the shared simulation library,
 *       which is compiled to JavaScript for the client.</li>
 * </ul>
 *
 * <p>Adding a meta-gene is now one entry here — which is the convenience reflection was being used
 * for — and the parser and the writer cannot disagree about a field.</p>
 */
public final class MetaGeneRegistry {

    /**
     * One meta-gene: its DSL name, its declared type, and how to read and write it.
     *
     * <p>Read/write are an explicit switch on the key in {@link MetaGeneRegistry}, so this class stays a
     * plain immutable descriptor with no functional interfaces — keeping it friendly to GWT and to Java
     * 11.</p>
     */
    public static final class MetaGene {

        /** The name as it appears in the {@code --- Meta genes} section, e.g. {@code initialAngle}. */
        public final String dslName;

        /** The declared type, used to parse the value and to emit the {@code (Type)} annotation. */
        public final Class<?> type;

        MetaGene(String dslName, Class<?> type) {
            this.dslName = dslName;
            this.type = type;
        }

        @Override
        public String toString() {
            return "MetaGene(" + dslName + ", " + type.getSimpleName() + ")";
        }
    }

    /**
     * The registry, in canonical emission order.
     *
     * <p>The order of this list <b>is</b> the order of the emitted {@code --- Meta genes} section, so it
     * must not be reordered casually: it would change every stored genome's canonical form.</p>
     *
     * <p>Names are written in the canonical lowercase-first form ({@code initialAngle}), matching the
     * Java field names. Parsing is case-insensitive so hand-written DNA that spells them differently
     * still loads, but emission always uses these spellings.</p>
     */
    private static final List<MetaGene> ALL = Collections.unmodifiableList(Arrays.asList(
            new MetaGene("hyperGene", float.class),
            new MetaGene("initialAngle", float.class)
    ));

    private MetaGeneRegistry() {
    }

    /** Every registered meta-gene, in canonical emission order. */
    public static List<MetaGene> all() {
        return ALL;
    }

    /**
     * Resolves a DSL name to its meta-gene, case-insensitively. Returns {@code null} when unknown, so
     * the caller can report a precise parse error with its line context.
     */
    public static MetaGene byDslName(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        for (MetaGene metaGene : ALL) {
            if (metaGene.dslName.equalsIgnoreCase(trimmed)) {
                return metaGene;
            }
        }
        return null;
    }

    /** Reads a meta-gene from a genome. The key must come from {@link #all()}. */
    public static Object read(MetaGene metaGene, MetaGenes metaGenes) {
        if (metaGene == ALL.get(0)) return Float.valueOf(metaGenes.getHyperGene());
        if (metaGene == ALL.get(1)) return Float.valueOf(metaGenes.getInitialAngle());
        throw new IllegalArgumentException("unregistered meta-gene: " + metaGene);
    }

    /**
     * Writes a meta-gene onto a genome. The key must come from {@link #all()}.
     *
     * @throws IllegalArgumentException if the value does not match the registered type
     */
    public static void write(MetaGene metaGene, MetaGenes metaGenes, Object value) {
        if (metaGene == ALL.get(0)) {
            metaGenes.setHyperGene(requireFloat(value, metaGene));
            return;
        }
        if (metaGene == ALL.get(1)) {
            metaGenes.setInitialAngle(requireFloat(value, metaGene));
            return;
        }
        throw new IllegalArgumentException("unregistered meta-gene: " + metaGene);
    }

    private static float requireFloat(Object value, MetaGene metaGene) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException(
                "meta-gene " + metaGene.dslName + " expects a number but got " + value);
    }

    /** All DSL names, for error messages. */
    public static List<String> allDslNames() {
        List<String> names = new ArrayList<String>(ALL.size());
        for (MetaGene metaGene : ALL) {
            names.add(metaGene.dslName);
        }
        return names;
    }

    /** The {@code (Type)} annotation emitted after a value, e.g. {@code "(Float)"}. */
    public static String typeAnnotation(MetaGene metaGene) {
        if (metaGene.type == float.class || metaGene.type == Float.class) return "(Float)";
        if (metaGene.type == double.class || metaGene.type == Double.class) return "(Double)";
        if (metaGene.type == int.class || metaGene.type == Integer.class) return "(Integer)";
        if (metaGene.type == boolean.class || metaGene.type == Boolean.class) return "(Boolean)";
        if (metaGene.type == String.class) return "(String)";
        return "";
    }

    /**
     * The canonical body of the {@code --- Meta genes} section: one line per registry entry, in registry
     * order, <b>without</b> the section marker or the explanatory comment.
     *
     * <p>The registry owns this because it already owns the names and types, so putting the spelling here
     * keeps the parser, the writer and any human-facing dump from each deriving it separately. The section
     * marker and the comment header are document assembly, and stay with the writer.</p>
     */
    public static String renderLines(MetaGenes metaGenes) {
        StringBuilder out = new StringBuilder();
        for (MetaGene metaGene : ALL) {
            out.append(metaGene.dslName).append(": ")
                    .append(CanonicalNumber.format(read(metaGene, metaGenes))).append(' ')
                    .append(typeAnnotation(metaGene)).append('\n');
        }
        return out.toString();
    }
}
