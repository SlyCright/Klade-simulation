package site.klade.simulation.condition;

import site.klade.simulation.Index;

/**
 * Recursive-descent parser for the condition grammar of spec §3.5.
 *
 * <p><b>Grammar</b> (lowest to highest precedence):</p>
 * <pre>
 * or         := xor ( OR xor )*
 * xor        := and ( XOR and )*
 * and        := unary ( AND unary )*
 * unary      := NOT unary | comparison
 * comparison := atom CmpOp atom | boolean-atom
 * atom       := NUMBER | Mrph[ID] | '(' or ')'
 * </pre>
 *
 * <p><b>Precedence note:</b> {@code NOT} binds <i>looser</i> than comparison, deliberately opposite
 * to C/Java. Comparison already yields a boolean, so {@code NOT Mrph[1] > 1.0} parses as
 * {@code Not(Compare(Mrph[1], GT, 1.0))} — the only reading that type-checks.</p>
 *
 * <p><b>Type safety:</b> the grammar admits parentheses at boolean level ({@code (A OR B) AND C}),
 * so an atom is parsed polymorphically and validated by context. Using a boolean where a numeric
 * operand is required (or vice versa) is a parse error, not a runtime surprise — this is what makes
 * §3.6 ("mutations must stay valid") hold by construction.</p>
 *
 * <p><b>Depth safety:</b> the AST is recursive, and every consumer of it — {@code toString},
 * {@code equals}, {@code hashCode} — is recursive too, so an unbounded tree overflows the stack
 * during *printing*, not just during parsing. The parser therefore enforces two independent bounds:</p>
 * <ul>
 *   <li>{@link #maxTreeDepth} — the depth of the <b>tree it builds</b>. This is the shared
 *       invariant; it must be enforced identically by structural mutation. It is checked
 *       incrementally while building, because a flat {@code a AND b AND c …} chain is assembled by
 *       the operator <i>loops</i> without any recursion, so a recursion-only guard would never see it.</li>
 *   <li>{@link #maxParenDepth} — the parser's own recursion depth. Parentheses do not add AST
 *       depth, so {@code ((((x))))} needs this second bound to keep the recursion itself in check.</li>
 * </ul>
 *
 * <p><b>GWT/Java 11 constraints:</b> no regex, no reflection, no records, no {@code BigDecimal}.
 * Only {@code String}/{@code Character}/{@code Float} from {@code java.lang} are used.</p>
 */
public final class ConditionParser {

    /**
     * Accepted spellings for a morphogen reference. Canonical output is always the full word
     * ({@code Morphogen[}), per the readability rule: no abbreviations of in-game terms. The short
     * forms remain accepted on input so pre-existing genomes keep loading.
     */
    private static final String[] MORPHOGEN_WORDS = {"Morphogen", "Mrph", "Mph"};

    /** No stop words: used by {@link #parse}, which requires the whole text to be a condition. */
    private static final String[] NO_STOP_WORDS = new String[0];

    /**
     * Maximum depth of the condition tree, counting a leaf as depth 1.
     *
     * <p>Shared invariant: structural mutation must enforce the same limit, otherwise it can build a
     * tree that the parser rejects — and whose {@code toString()} would overflow the stack before any
     * parser saw it. Chosen far below the observed overflow point (a ~2000-deep chain already overflows
     * a default stack at print time) while being far above any plausible evolved condition.</p>
     *
     * <p>This value can be configured at runtime via {@link #configureLimits(int, int)}.</p>
     */
    private static int maxTreeDepth = 64;

    /**
     * Maximum parenthesis nesting the parser will recurse through. Parentheses do not contribute to
     * AST depth, so this is a separate pathology guard for hand-written input only.
     *
     * <p>This value can be configured at runtime via {@link #configureLimits(int, int)}.</p>
     */
    private static int maxParenDepth = 256;

    private final String src;

    private int pos;

    private int parenDepth;

    /**
     * Words that terminate a condition during {@link #parsePrefix}. Always non-null; empty for
     * {@link #parse}, which requires the whole text to be a condition.
     */
    private String[] stopWords = NO_STOP_WORDS;

    private ConditionParser(String src) {
        this.src = src;
    }

    /**
     * Configures the parser limits at runtime. This must be called before any parsing occurs,
     * typically at application startup.
     *
     * @param treeDepth the maximum AST depth
     * @param parenDepth the maximum parenthesis nesting depth
     */
    public static void configureLimits(int treeDepth, int parenDepth) {
        maxTreeDepth = treeDepth;
        maxParenDepth = parenDepth;
    }

    /**
     * Returns the current maximum tree depth limit. This is used by structural mutation
     * to enforce the same invariant.
     *
     * @return the maximum AST depth
     */
    public static int getMaxTreeDepth() {
        return maxTreeDepth;
    }

    /**
     * Parses a condition expression that must consume the whole text.
     *
     * @param text the condition text; may be {@code null} or blank
     * @return the parsed condition, or {@code null} when {@code text} is {@code null}/blank
     *         (meaning "unconditional" — this is a valid state, not an error)
     * @throws ConditionParseException if the text is non-blank but malformed, or too deeply nested
     */
    public static Cond parse(String text) {
        ConditionPrefix prefix = parsePrefix(text, NO_STOP_WORDS);
        if (prefix.condition == null) {
            // With no stop words, a null condition means the text was blank (or only comments).
            return null;
        }
        ConditionParser parser = new ConditionParser(text);
        parser.pos = prefix.end;
        parser.skipIgnorable();
        if (!parser.atEnd()) {
            throw new ConditionParseException("unexpected trailing input", parser.pos);
        }
        return prefix.condition;
    }

    /**
     * Parses a condition at the <b>start</b> of {@code src}, stopping before any of {@code stopWords}.
     *
     * <p>Needed because a gene line is a condition followed by an action
     * ({@code if Morphogen[1] > 1.0 become muscle}), while {@link #parse} rejects any trailing text. The
     * returned {@link ConditionPrefix#end} is where the caller should continue reading.</p>
     *
     * <p>A stop word is matched with the same word-boundary rule as the boolean keywords, so
     * {@code express} cannot be found inside a longer identifier. The stop word itself is
     * <b>not</b> consumed.</p>
     *
     * <p>When the text starts with a stop word, {@link ConditionPrefix#condition} is {@code null} and
     * {@code end} is the position of that word — meaning "unconditional". That is a valid state, not an
     * error, exactly as blank input is for {@link #parse}.</p>
     *
     * @param src       the text to read; {@code null} or blank yields a null condition at position 0
     * @param stopWords words that terminate the condition; never null, may be empty
     * @throws ConditionParseException if a condition begins but is malformed, or too deeply nested
     */
    public static ConditionPrefix parsePrefix(String src, String[] stopWords) {
        if (src == null) return new ConditionPrefix(null, 0);
        String[] stops = (stopWords == null) ? NO_STOP_WORDS : stopWords;
        ConditionParser parser = new ConditionParser(src);
        parser.stopWords = stops;
        parser.skipIgnorable();
        // A condition in DNA is introduced by `if`. It is optional here so that both the raw condition
        // ("Morphogen[1] > 1.0", as stored on a Gene) and a full DNA line are accepted by the same entry
        // point. `tryKeyword` handles the word boundary, so an identifier like "iffy" is not affected.
        parser.tryKeyword("if");
        parser.skipIgnorable();
        if (parser.atEnd() || parser.atStopWord()) {
            return new ConditionPrefix(null, parser.pos);
        }
        Cond result = (Cond) parser.parseOr().value;
        parser.skipIgnorable();
        return new ConditionPrefix(result, parser.pos);
    }

    /**
     * Parses a <b>numeric</b> expression at the start of {@code src}: either a literal or a morphogen
     * reference. Used for gene arguments such as {@code express Morphogen[1] amount 2.0}, where the
     * argument is an {@code Expr} rather than a full condition.
     *
     * <p>Sharing the parser's number and reference reading is what keeps {@code 30°} in
     * {@code lay_segment 30°} and {@code 1.0} in {@code Morphogen[1] > 1.0} lexed by exactly the same
     * code, so the two syntaxes cannot drift.</p>
     */
    public static NumberPrefix parseNumber(String src) {
        if (src == null) {
            throw new ConditionParseException("expected a numeric argument", 0);
        }
        ConditionParser parser = new ConditionParser(src);
        parser.skipIgnorable();
        if (parser.atEnd()) {
            throw new ConditionParseException("expected a numeric argument", parser.pos);
        }
        Built built = parser.parseAtom();
        if (built.value instanceof Cond) {
            throw new ConditionParseException(
                    "expected a numeric argument, found a condition", parser.pos);
        }
        parser.skipIgnorable();
        return new NumberPrefix((Expr) built.value, parser.pos);
    }

    /** A numeric expression parsed from the start of a text, plus where it ended. */
    public static final class NumberPrefix {

        public final Expr value;

        public final int end;

        NumberPrefix(Expr value, int end) {
            this.value = value;
            this.end = end;
        }
    }

    /** A parsed value plus the depth of the tree it represents; a leaf has depth 1. */
    private static final class Built {

        final Object value;

        final int depth;

        Built(Object value, int depth) {
            this.value = value;
            this.depth = depth;
        }
    }

    // ---------------------------------------------------------------- grammar

    private Built parseOr() {
        Built left = parseXor();
        while (tryKeyword("OR")) {
            left = combine(left, parseXor(), BoolOpKind.OR);
        }
        return left;
    }

    private Built parseXor() {
        Built left = parseAnd();
        while (tryKeyword("XOR")) {
            left = combine(left, parseAnd(), BoolOpKind.XOR);
        }
        return left;
    }

    private Built parseAnd() {
        Built left = parseUnary();
        while (tryKeyword("AND")) {
            left = combine(left, parseUnary(), BoolOpKind.AND);
        }
        return left;
    }

    private Built parseUnary() {
        if (tryKeyword("NOT")) {
            Built inner = parseUnary();
            return build(new Not((Cond) inner.value), inner.depth + 1);
        }
        return parseComparison();
    }

    /**
     * Parses a comparison, or a bare boolean atom such as a parenthesized group.
     *
     * <p>The left side is parsed as an untyped atom first: it is either a numeric expression
     * (which must then be followed by a comparison operator) or an already-boolean group.</p>
     */
    private Built parseComparison() {
        int leftPos = skipIgnorable();
        Built left = parseAtom();
        if (left.value instanceof Cond) {
            if (peekComparisonOperator() != null) {
                throw new ConditionParseException(
                        "boolean condition used as a numeric comparison operand", leftPos);
            }
            return left;
        }
        CmpOp op = readComparisonOperator();
        if (op == null) {
            throw new ConditionParseException(
                    "expected a comparison operator after a numeric operand", pos);
        }
        int rightPos = skipIgnorable();
        Built right = parseAtom();
        if (right.value instanceof Cond) {
            throw new ConditionParseException(
                    "boolean condition used as a numeric comparison operand", rightPos);
        }
        return build(new Compare((Expr) left.value, op, (Expr) right.value),
                Math.max(left.depth, right.depth) + 1);
    }

    /** Parses a numeric literal, a morphogen reference, or a parenthesized sub-condition. */
    private Built parseAtom() {
        skipIgnorable();
        if (atEnd()) throw new ConditionParseException("expected an expression", pos);
        char c = src.charAt(pos);
        if (c == '(') {
            int open = pos;
            if (++parenDepth > maxParenDepth) {
                throw new ConditionParseException(
                        "condition nesting too deep (max " + maxParenDepth + ")", open);
            }
            pos++;
            Built inner = parseOr();
            skipIgnorable();
            if (atEnd() || src.charAt(pos) != ')') {
                throw new ConditionParseException("unbalanced parenthesis", open);
            }
            pos++;
            parenDepth--;
            return inner;
        }
        if (isLetter(c)) {
            int wordStart = pos;
            String word = readWord();
            for (String morphogenWord : MORPHOGEN_WORDS) {
                if (word.equalsIgnoreCase(morphogenWord)) {
                    return build(new MorphogenRef(readMorphogenId(word, wordStart)), 1);
                }
            }
            if (isBooleanKeyword(word)) {
                throw new ConditionParseException("unexpected operator '" + word + "'", wordStart);
            }
            throw new ConditionParseException("unknown identifier '" + word + "'", wordStart);
        }
        if (Character.isDigit(c) || c == '-' || c == '+') {
            return build(new Literal(readNumber()), 1);
        }
        throw new ConditionParseException("unexpected character '" + c + "'", pos);
    }

    private Built combine(Built left, Built right, BoolOpKind kind) {
        return build(new BoolOp((Cond) left.value, kind, (Cond) right.value),
                Math.max(left.depth, right.depth) + 1);
    }

    /** Applies the shared tree-depth invariant at the point of construction. */
    private Built build(Object value, int depth) {
        if (depth > maxTreeDepth) {
            throw new ConditionParseException(
                    "condition too deep (max " + maxTreeDepth + ")", pos);
        }
        return new Built(value, depth);
    }

    // ---------------------------------------------------------------- terminals

    /**
     * Reads the dotted index inside {@code Morphogen[...]}, e.g. {@code 1} or {@code 1.1}.
     *
     * <p>Scans the index characters and delegates to {@link Index#parse}, which already owns the
     * canonical spelling rules (it accepts both {@code 1.1} and {@code 1.1.} and rejects empty or
     * malformed input). Sharing it means a morphogen id and a gene id can never disagree about what a
     * legal index looks like.</p>
     */
    private Index readMorphogenId(String word, int wordStart) {
        skipIgnorable();
        if (atEnd() || src.charAt(pos) != '[') {
            throw new ConditionParseException("expected '[' after '" + word + "'", pos);
        }
        pos++;
        skipIgnorable();
        int start = pos;
        while (!atEnd() && isIndexChar(src.charAt(pos))) pos++;
        if (start == pos) {
            throw new ConditionParseException("expected a morphogen id", pos);
        }
        Index id;
        try {
            id = Index.parse(src.substring(start, pos));
        } catch (IllegalArgumentException e) {
            throw new ConditionParseException("invalid morphogen id", start);
        }
        skipIgnorable();
        if (atEnd() || src.charAt(pos) != ']') {
            throw new ConditionParseException("expected ']' to close the morphogen id", pos);
        }
        pos++;
        return id;
    }

    private float readNumber() {
        int start = pos;
        if (src.charAt(pos) == '+' || src.charAt(pos) == '-') pos++;
        boolean anyDigit = false;
        while (!atEnd() && Character.isDigit(src.charAt(pos))) {
            pos++;
            anyDigit = true;
        }
        if (!atEnd() && src.charAt(pos) == '.') {
            pos++;
            while (!atEnd() && Character.isDigit(src.charAt(pos))) {
                pos++;
                anyDigit = true;
            }
        }
        if (!anyDigit) {
            throw new ConditionParseException("malformed number", start);
        }
        // Optional exponent; rolled back if it is not well formed.
        if (!atEnd() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            int save = pos;
            pos++;
            if (!atEnd() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
            if (!atEnd() && Character.isDigit(src.charAt(pos))) {
                while (!atEnd() && Character.isDigit(src.charAt(pos))) pos++;
            } else {
                pos = save;
            }
        }
        String lexeme = src.substring(start, pos);
        float value;
        try {
            value = Float.parseFloat(lexeme);
        } catch (NumberFormatException e) {
            throw new ConditionParseException("invalid number '" + lexeme + "'", start);
        }
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new ConditionParseException("non-finite number '" + lexeme + "'", start);
        }
        return value;
    }

    private CmpOp readComparisonOperator() {
        skipIgnorable();
        if (pos + 1 < src.length()) {
            String two = src.substring(pos, pos + 2);
            CmpOp twoChar = twoCharOperator(two);
            if (twoChar != null) {
                pos += 2;
                return twoChar;
            }
        }
        if (!atEnd()) {
            char c = src.charAt(pos);
            if (c == '>') {
                pos++;
                return CmpOp.GT;
            }
            if (c == '<') {
                pos++;
                return CmpOp.LT;
            }
            if (c == '=') {
                throw new ConditionParseException("unexpected '='; did you mean '=='?", pos);
            }
            if (c == '!') {
                throw new ConditionParseException("unexpected '!'; did you mean '!='?", pos);
            }
        }
        return null;
    }

    private CmpOp peekComparisonOperator() {
        int save = pos;
        try {
            return readComparisonOperator();
        } catch (ConditionParseException e) {
            return null;
        } finally {
            pos = save;
        }
    }

    private static CmpOp twoCharOperator(String two) {
        if (">=".equals(two)) return CmpOp.GE;
        if ("<=".equals(two)) return CmpOp.LE;
        if ("==".equals(two)) return CmpOp.EQ;
        if ("!=".equals(two)) return CmpOp.NE;
        return null;
    }

    // ---------------------------------------------------------------- scanning

    /** Consumes whitespace and {@code #} comments; returns the position where the next token starts. */
    private int skipIgnorable() {
        while (!atEnd()) {
            char c = src.charAt(pos);
            if (c == '#') {
                while (!atEnd() && src.charAt(pos) != '\n') pos++;
            } else if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                pos++;
            } else {
                break;
            }
        }
        return pos;
    }

    private boolean tryKeyword(String keyword) {
        int save = pos;
        skipIgnorable();
        int start = pos;
        if (!src.regionMatches(true, start, keyword, 0, keyword.length())) {
            pos = save;
            return false;
        }
        int end = start + keyword.length();
        if (end < src.length() && isWordChar(src.charAt(end))) {
            pos = save;
            return false;
        }
        pos = end;
        return true;
    }

    /**
     * True when the next token is one of {@link #stopWords}, matched at a word boundary so that a stop
     * word can never be found inside a longer identifier. Leaves {@link #pos} unchanged — the caller
     * wants the stop word left in place, since the action parser reads it next.
     */
    private boolean atStopWord() {
        for (String stop : stopWords) {
            int start = pos;
            if (!src.regionMatches(true, start, stop, 0, stop.length())) {
                continue;
            }
            int end = start + stop.length();
            if (end < src.length() && isWordChar(src.charAt(end))) {
                continue;
            }
            return true;
        }
        return false;
    }

    private String readWord() {
        int start = pos;
        while (!atEnd() && isLetter(src.charAt(pos))) pos++;
        return src.substring(start, pos);
    }

    private boolean atEnd() {
        return pos >= src.length();
    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * Characters that may appear inside a dotted index: digits, the dot separator, and a sign for
     * negative segments ({@code Index} permits them). Validation is left to {@link Index#parse}, so this
     * only decides where the token ends.
     */
    private static boolean isIndexChar(char c) {
        return Character.isDigit(c) || c == '.' || c == '-' || c == '+';
    }

    private static boolean isWordChar(char c) {
        return isLetter(c) || Character.isDigit(c) || c == '_';
    }

    private static boolean isBooleanKeyword(String word) {
        return "AND".equalsIgnoreCase(word)
                || "OR".equalsIgnoreCase(word)
                || "XOR".equalsIgnoreCase(word)
                || "NOT".equalsIgnoreCase(word);
    }
}
