package site.klade.simulation;

import site.klade.simulation.condition.Cond;
import site.klade.simulation.condition.ConditionParseException;
import site.klade.simulation.condition.ConditionParser;
import site.klade.simulation.condition.ConditionPrefix;
import site.klade.simulation.gene.ArgSpec;
import site.klade.simulation.gene.ElementType;
import site.klade.simulation.gene.ElementTypeArg;
import site.klade.simulation.gene.Gene;
import site.klade.simulation.gene.GeneAction;
import site.klade.simulation.gene.GeneArg;
import site.klade.simulation.gene.NumberArg;
import site.klade.simulation.gene.NumberKind;
import site.klade.simulation.gene.NumberSpec;
import site.klade.simulation.gene.ReferenceArg;
import site.klade.simulation.gene.ReferenceSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a DNA string into a {@link Genome}. The single {@code String -> Genome} implementation, shared
 * by the server and the client.
 *
 * <p><b>Why only this direction lives here.</b> The client never writes a genome; serialisation is the
 * backend's persistence concern. Keeping the writer out means this class does not have to know how to
 * lay the format out, and the backend keeps its freedom — while parsing, which the client <i>does</i>
 * need once ontogenesis runs there, is available to both.</p>
 *
 * <p><b>Robustness.</b> A malformed line is a hard {@link DnaParseException} carrying its line number and
 * text. Nothing is skipped silently.</p>
 *
 * <p><b>Section order is not significant.</b> Canonical output is {@code Meta genes, Genes, Morphogens},
 * but any order is accepted: morphogens are derived from gene references rather than authored, and
 * hand-written DNA may reasonably put the sections anywhere.</p>
 *
 * <p><b>GWT-safe</b>: no reflection anywhere. Meta-gene names resolve through {@link MetaGeneRegistry},
 * whose order is also what the backend writer emits.</p>
 */
public final class GenomeCodec {

    private static final String META_SECTION = "--- Meta genes";

    private static final String GENE_SECTION = "--- Genes";

    private static final String MORPHOGEN_SECTION = "--- Morphogens";

    /** Prefixes accepted for a morphogen reference; only the first is ever emitted. */
    private static final String[] MORPHOGEN_PREFIXES = {"Morphogen[", "Mrph[", "Mph["};

    /** Unit spellings accepted for an angle, mapped to the canonical degree sign. */
    private static final String[] DEGREE_WORDS = {"°", "degrees", "degree", "deg"};

    /** Unit spellings accepted for a percentage. */
    private static final String[] PERCENT_WORDS = {"%", "percent"};

    /**
     * Words that end a gene line's condition and begin its action. Includes both the full spellings and
     * the shorter "lay", so an action is recognised whichever form the DNA uses.
     */
    private static final String[] ACTION_STOP_WORDS = {"become", "lay_segment", "lay", "express"};

    /** The optional word permitted after an {@code if} condition; accepted and ignored. */
    private static final String THEN_WORD = "then";

    private final Genome genome;

    private int lineNumber;

    private GenomeCodec() {
        this.genome = new Genome(new MetaGenes(),
                new ArrayList<Morphogen>(), new ArrayList<Gene>());
    }

    /**
     * Parses DNA text into a genome.
     *
     * @param dna the DNA text
     * @return the parsed genome, with its morphogens materialised from its references
     * @throws IllegalArgumentException if the text is null or blank
     * @throws DnaParseException if any line is malformed
     */
    public static Genome parse(String dna) {
        if (dna == null) throw new IllegalArgumentException("DNA text is null");
        if (dna.trim().isEmpty()) throw new IllegalArgumentException("DNA text is empty");
        GenomeCodec codec = new GenomeCodec();
        codec.readAll(dna);
        // Morphogens are derived, not authored: whatever the --- Morphogens section said, the reference
        // set is the truth. This is also what makes that section optional.
        codec.genome.deriveMorphogens();
        return codec.genome;
    }

    private void readAll(String dna) {
        Section section = Section.NONE;
        String[] lines = dna.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            lineNumber = i + 1;
            String raw = lines[i];
            String line = stripComment(raw).trim();
            if (line.isEmpty()) {
                continue;
            }
            Section header = sectionOf(line);
            if (header != null) {
                section = header;
                continue;
            }
            switch (section) {
                case META_GENES:
                    readMetaGene(line, raw);
                    break;
                case GENES:
                    readGene(line, raw);
                    break;
                case MORPHOGENS:
                    readMorphogen(line, raw);
                    break;
                case NONE:
                default:
                    throw new DnaParseException("content before any section marker", lineNumber, raw);
            }
        }
        // Genes are stored in dotted order so every consumer — interpreter, crossover, writer — sees one
        // deterministic layout.
        sortGenes();
    }

    /** Removes a {@code #} comment. {@code #} never occurs inside a token, so a plain split is exact. */
    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash < 0 ? line : line.substring(0, hash);
    }

    private static Section sectionOf(String line) {
        if (META_SECTION.equals(line)) return Section.META_GENES;
        if (GENE_SECTION.equals(line)) return Section.GENES;
        if (MORPHOGEN_SECTION.equals(line)) return Section.MORPHOGENS;
        return null;
    }

    // ------------------------------------------------------------------ meta genes

    private void readMetaGene(String line, String raw) {
        int colon = line.indexOf(':');
        if (colon < 0) {
            throw new DnaParseException("expected '<name>: <value>'", lineNumber, raw);
        }
        String name = line.substring(0, colon).trim();
        String valuePart = line.substring(colon + 1).trim();
        // Strip an optional "(Type)" annotation. The registry knows the declared type, so the annotation
        // is documentation for a human reader rather than something to trust.
        int typeIndex = valuePart.indexOf('(');
        if (typeIndex >= 0) {
            valuePart = valuePart.substring(0, typeIndex).trim();
        }
        MetaGeneRegistry.MetaGene metaGene = MetaGeneRegistry.byDslName(name);
        if (metaGene == null) {
            throw new DnaParseException("unknown meta-gene '" + name + "'; expected one of "
                    + MetaGeneRegistry.allDslNames(), lineNumber, raw);
        }
        if (valuePart.isEmpty()) {
            throw new DnaParseException("missing value for meta-gene '" + name + "'", lineNumber, raw);
        }
        try {
            MetaGeneRegistry.write(metaGene, genome.getMetaGenes(),
                    Float.valueOf(normalizeNumber(valuePart)));
        } catch (NumberFormatException e) {
            throw new DnaParseException("malformed number '" + valuePart + "'", lineNumber, raw, e);
        }
    }

    // ------------------------------------------------------------------ morphogens

    private void readMorphogen(String line, String raw) {
        int colon = line.indexOf(':');
        if (colon < 0) {
            throw new DnaParseException(
                    "expected 'Morphogen[<index>]: <diffusion>, <decay>'", lineNumber, raw);
        }
        Index id = parseBracketedIndex(line.substring(0, colon), raw);
        String[] numbers = line.substring(colon + 1).split(",", -1);
        if (numbers.length != 2) {
            throw new DnaParseException("expected exactly two numbers (diffusion, decay) but found "
                    + numbers.length, lineNumber, raw);
        }
        float diffusion = parseRatio(numbers[0], "diffusion", raw);
        float decay = parseRatio(numbers[1], "decay", raw);
        // This section only seeds values. `deriveMorphogens` then keeps the definitions whose ids are
        // actually referenced and drops the rest, so a line here for an unused id is harmless.
        if (genome.findMorphogen(id) == null) {
            genome.getMorphogens().add(new Morphogen(id, diffusion, decay));
        }
    }

    private float parseRatio(String text, String what, String raw) {
        try {
            return Float.parseFloat(normalizeNumber(text));
        } catch (NumberFormatException e) {
            throw new DnaParseException("malformed " + what + " ratio '" + text + "'",
                    lineNumber, raw, e);
        }
    }

    // ------------------------------------------------------------------ genes

    private void readGene(String line, String raw) {
        Index index = readLeadingIndex(line);
        String rest;
        if (index != null) {
            rest = afterIndex(line).trim();
        } else {
            index = nextImplicitIndex();
            rest = line;
        }

        Cond condition = null;
        if (startsWithWord(rest, "if")) {
            String afterIf = rest.substring(2).trim();
            ConditionPrefix prefix;
            try {
                prefix = ConditionParser.parsePrefix(afterIf, ACTION_STOP_WORDS);
            } catch (ConditionParseException e) {
                throw new DnaParseException("malformed condition: " + e.getMessage(),
                        lineNumber, raw, e);
            }
            condition = prefix.condition;
            rest = afterIf.substring(prefix.end).trim();
            if (startsWithWord(rest, THEN_WORD)) {
                rest = rest.substring(THEN_WORD.length()).trim();
            }
        }

        if (rest.isEmpty()) {
            // An index with no action is an empty gene — valid, and how "wait" is written.
            genome.getGenes().add(new Gene(index, condition, GeneAction.EMPTY,
                    new ArrayList<GeneArg>()));
            return;
        }
        readAction(index, condition, rest, raw);
    }

    private void readAction(Index index, Cond condition, String text, String raw) {
        GeneAction action = matchAction(text);
        if (action == null) {
            throw new DnaParseException("unknown action; expected one of "
                    + GeneAction.allCanonicalNames(), lineNumber, raw);
        }
        String arguments = text.substring(action.canonicalName().length()).trim();
        List<GeneArg> args = new ArrayList<GeneArg>();
        if (action == GeneAction.BECOME) {
            readBecomeArguments(arguments, args, raw);
        } else {
            readArguments(action.leadingSpecs(), arguments, args, raw);
        }
        genome.getGenes().add(new Gene(index, condition, action, args));
    }

    /**
     * Reads {@code become}'s arguments. The first token is the element type, and that type contributes
     * the remaining specs — which is how {@code become} gets its dependent arity.
     */
    private void readBecomeArguments(String arguments, List<GeneArg> args, String raw) {
        String[] parts = splitFirstToken(arguments);
        if (parts[0].isEmpty()) {
            throw new DnaParseException("become requires an element type", lineNumber, raw);
        }
        ElementType type = ElementType.parse(parts[0]);
        if (type == null) {
            throw new DnaParseException("unknown element type '" + parts[0] + "'; expected one of "
                    + ElementType.allCanonicalNames(), lineNumber, raw);
        }
        args.add(new ElementTypeArg(type));
        readArguments(type.parameterSpecs(), parts[1], args, raw);
    }

    /**
     * Reads a whitespace-separated argument list against a signature, using the specs as the authority on
     * how many tokens each argument consumes.
     */
    private void readArguments(List<ArgSpec> specs, String text, List<GeneArg> args, String raw) {
        List<String> tokens = tokenizeArguments(text);
        int cursor = 0;
        for (int i = 0; i < specs.size(); i++) {
            ArgSpec spec = specs.get(i);
            if (cursor >= tokens.size()) {
                throw new DnaParseException("missing argument " + i + " (" + spec + ")",
                        lineNumber, raw);
            }
            if (spec instanceof NumberSpec) {
                cursor = readNumberArgument((NumberSpec) spec, tokens, cursor, args, raw);
            } else if (spec instanceof ReferenceSpec) {
                args.add(new ReferenceArg(parseMorphogenToken(tokens.get(cursor), raw)));
                cursor++;
            } else {
                throw new DnaParseException("unexpected argument kind " + spec, lineNumber, raw);
            }
        }
        if (cursor < tokens.size()) {
            throw new DnaParseException("unexpected extra argument '" + tokens.get(cursor) + "'",
                    lineNumber, raw);
        }
    }

    /** Reads one numeric argument: its optional label, then its value with its optional unit. */
    private int readNumberArgument(NumberSpec spec, List<String> tokens, int cursor,
            List<GeneArg> args, String raw) {
        if (spec.label != null) {
            String label = tokens.get(cursor);
            if (!label.equalsIgnoreCase(spec.label)) {
                throw new DnaParseException("expected '" + spec.label + "' but found '" + label + "'",
                        lineNumber, raw);
            }
            cursor++;
            if (cursor >= tokens.size()) {
                throw new DnaParseException("missing value after '" + spec.label + "'",
                        lineNumber, raw);
            }
        }
        String token = tokens.get(cursor);
        cursor++;
        String numberText = token;
        String unit = "";
        String peeled = peelAttachedUnit(token);
        if (peeled != null) {
            numberText = peeled;
            unit = attachedUnitOf(token);
        }
        float value;
        try {
            value = Float.parseFloat(normalizeNumber(numberText));
        } catch (NumberFormatException e) {
            throw new DnaParseException("malformed number '" + token + "'", lineNumber, raw, e);
        }
        if (spec.kind == NumberKind.INTEGER) {
            value = Math.round(value);
        }
        if (!unit.isEmpty() && !spec.unit.isEmpty() && !unit.equals(spec.unit)) {
            throw new DnaParseException("expected unit '" + spec.unit + "' but found '" + unit + "'",
                    lineNumber, raw);
        }
        args.add(new NumberArg(value));
        return cursor;
    }

    /**
     * Strips a unit that is written directly against a number, e.g. {@code "30°"} or {@code "40%"}.
     * Returns the numeric part, or {@code null} when the token carries no unit.
     */
    private static String peelAttachedUnit(String token) {
        String unit = attachedUnitOf(token);
        if (unit.isEmpty()) {
            return null;
        }
        return token.substring(0, token.length() - rawUnitSuffix(token).length());
    }

    /** The canonical unit symbol attached to this token, or {@code ""}. */
    private static String attachedUnitOf(String token) {
        String suffix = rawUnitSuffix(token);
        if (suffix.isEmpty()) {
            return "";
        }
        for (String word : PERCENT_WORDS) {
            if (word.equalsIgnoreCase(suffix)) {
                return "%";
            }
        }
        return "°";
    }

    /** The raw (possibly non-canonical) unit suffix of a token, or {@code ""}. */
    private static String rawUnitSuffix(String token) {
        for (String word : PERCENT_WORDS) {
            if (token.length() > word.length() && token.regionMatches(true,
                    token.length() - word.length(), word, 0, word.length())) {
                return token.substring(token.length() - word.length());
            }
        }
        for (String word : DEGREE_WORDS) {
            if (token.length() > word.length() && token.regionMatches(true,
                    token.length() - word.length(), word, 0, word.length())) {
                return token.substring(token.length() - word.length());
            }
        }
        return "";
    }

    /** Splits {@code "muscle length 40%"} into {@code {"muscle", "length 40%"}}. */
    private static String[] splitFirstToken(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return new String[]{"", ""};
        }
        int space = indexOfWhitespace(trimmed);
        if (space < 0) {
            return new String[]{trimmed, ""};
        }
        return new String[]{trimmed.substring(0, space), trimmed.substring(space + 1).trim()};
    }

    /**
     * Tokenizes an argument list, joining a separated unit word to the number before it so the
     * spec-driven reader sees one token per argument value ({@code "30 degrees"} becomes
     * {@code "30degrees"}).
     */
    private static List<String> tokenizeArguments(String text) {
        List<String> tokens = new ArrayList<String>();
        for (String piece : text.split("\\s+")) {
            String token = piece.trim();
            if (token.isEmpty()) {
                continue;
            }
            if (isUnitWord(token) && !tokens.isEmpty()) {
                int last = tokens.size() - 1;
                tokens.set(last, tokens.get(last) + token);
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private static boolean isUnitWord(String token) {
        for (String word : PERCENT_WORDS) {
            if (token.equalsIgnoreCase(word)) {
                return true;
            }
        }
        for (String word : DEGREE_WORDS) {
            if (token.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ token helpers

    private Index parseMorphogenToken(String token, String raw) {
        String text = token;
        for (String prefix : MORPHOGEN_PREFIXES) {
            if (text.regionMatches(true, 0, prefix, 0, prefix.length())) {
                text = text.substring(prefix.length());
                break;
            }
        }
        if (!text.endsWith("]")) {
            throw new DnaParseException(
                    "expected a morphogen reference like Morphogen[1] but found '" + token + "'",
                    lineNumber, raw);
        }
        try {
            return Index.parse(text.substring(0, text.length() - 1));
        } catch (IllegalArgumentException e) {
            throw new DnaParseException("invalid morphogen index in '" + token + "'",
                    lineNumber, raw, e);
        }
    }

    private Index parseBracketedIndex(String text, String raw) {
        int open = text.indexOf('[');
        int close = text.indexOf(']');
        if (open < 0 || close < open) {
            throw new DnaParseException("expected '[<index>]' in '" + text.trim() + "'",
                    lineNumber, raw);
        }
        try {
            return Index.parse(text.substring(open + 1, close));
        } catch (IllegalArgumentException e) {
            throw new DnaParseException("invalid index in '" + text.trim() + "'", lineNumber, raw, e);
        }
    }

    /**
     * Recognises a leading dotted index such as {@code 1.}, {@code 1.2.} or {@code 1.2}, or returns
     * {@code null} when the line does not begin with one — which is the input leniency that lets
     * hand-written DNA omit indices.
     *
     * <p>The index token must be followed by whitespace or end-of-line, so a number that begins an
     * argument is never mistaken for an index. Note this is why the scan must consume internal dots: a
     * nested index like {@code 1.2} contains a dot that is part of the token rather than a terminator.</p>
     */
    private static Index readLeadingIndex(String line) {
        int end = scanIndexToken(line, 0);
        if (end == 0) {
            return null;
        }
        try {
            return Index.parse(line.substring(0, end));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Strips the leading index token from a gene line; call only when {@link #readLeadingIndex} matched. */
    private static String afterIndex(String line) {
        return line.substring(scanIndexToken(line, 0));
    }

    /**
     * Scans a leading index token from {@code from} and returns the index just past it, or {@code from}
     * when the line does not begin with one.
     *
     * <p>An index is a sign, digits, and any number of {@code .<sign><digits>} groups — so internal dots
     * <b>are</b> consumed. The optional trailing dot of the canonical form ({@code 1.2.}) is consumed too.
     * The token is only accepted when what follows is whitespace or end-of-line; otherwise this is an
     * ordinary number, not an index.</p>
     *
     * <p>Getting this wrong is subtle: treating the dot as a terminator silently makes every nested index
     * look like an index-less line, which renumbers it instead of failing.</p>
     */
    private static int scanIndexToken(String line, int from) {
        int cursor = scanNumberGroup(line, from);
        if (cursor == from) {
            return from;
        }
        // Consume further `.group` segments, but only when a group actually follows the dot.
        while (cursor < line.length() && line.charAt(cursor) == '.') {
            int afterDot = scanNumberGroup(line, cursor + 1);
            if (afterDot == cursor + 1) {
                break;
            }
            cursor = afterDot;
        }
        // Consume the canonical trailing dot when it is the last character before whitespace.
        if (cursor < line.length() && line.charAt(cursor) == '.') {
            cursor++;
        }
        if (cursor < line.length() && !Character.isWhitespace(line.charAt(cursor))) {
            return from;
        }
        return cursor;
    }

    /** Scans an optional sign followed by one or more digits; returns where it stops. */
    private static int scanNumberGroup(String line, int from) {
        int cursor = from;
        if (cursor < line.length() && (line.charAt(cursor) == '-' || line.charAt(cursor) == '+')) {
            cursor++;
        }
        int digitsStart = cursor;
        while (cursor < line.length() && Character.isDigit(line.charAt(cursor))) {
            cursor++;
        }
        return cursor == digitsStart ? from : cursor;
    }

    /**
     * The smallest unused positive root index, per the grammar's input leniency for index-less lines.
     */
    private Index nextImplicitIndex() {
        for (int candidate = 1; candidate < Integer.MAX_VALUE; candidate++) {
            if (genome.findGene(new Index(candidate)) == null) {
                return new Index(candidate);
            }
        }
        throw new IllegalStateException("no free root index available");
    }

    private GeneAction matchAction(String text) {
        GeneAction best = null;
        for (GeneAction action : GeneAction.values()) {
            if (action == GeneAction.EMPTY) {
                continue;
            }
            String name = action.canonicalName();
            if (startsWithWord(text, name)
                    && (best == null || name.length() > best.canonicalName().length())) {
                best = action;
            }
        }
        return best;
    }

    private static boolean startsWithWord(String text, String word) {
        if (text.length() < word.length()
                || !text.regionMatches(true, 0, word, 0, word.length())) {
            return false;
        }
        return text.length() == word.length() || !isWordChar(text.charAt(word.length()));
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static int indexOfWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Accepts both {@code .} and {@code ,} as the decimal separator. */
    private static String normalizeNumber(String text) {
        return text.trim().replace(',', '.');
    }

    private void sortGenes() {
        Collections.sort(genome.getGenes(), new Comparator<Gene>() {
            @Override
            public int compare(Gene left, Gene right) {
                return left.getIndex().compareTo(right.getIndex());
            }
        });
    }

    private enum Section {
        NONE, META_GENES, GENES, MORPHOGENS
    }
}
