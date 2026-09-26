package site.klade.simulation;

/**
 * Hierarchical dotted index for genes, e.g. new Index(2, new Index(1)) == "2.1.".
 *
 * <p>Indexes encode ORDER ONLY — they never represent ownership or a tree structure.
 * Deleting an index never invalidates longer indices that share its prefix.</p>
 *
 * <p>{@link #toString()} yields the canonical form which always ends with a dot ("2.1.");
 * {@link #parse(String)} accepts both "2.1" and "2.1.".</p>
 *
 * <p>This is a pure immutable <b>value object</b> + I/O (parse/toString). Structural
 * derivations (segment access, parent/extend, increment/decrement, prefix relation) and gap
 * allocation live in the backend web-app's {@code evolution} package (IndexOps / IndexAllocator).
 */
public final class Index implements Comparable<Index> {

    private final int value;

    private final Index nested;

    public Index(int value) {
        this(value, null);
    }

    public Index(int value, Index nested) {
        this.value = value;
        this.nested = nested;
    }

    public int getValue() {
        return value;
    }

    public Index getNested() {
        return nested;
    }

    public boolean isTerminal() {
        return nested == null;
    }

    public static Index parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("index is null");
        }
        String text = s.trim();
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("index is empty: '" + s + "'");
        }
        String[] parts = text.split("\\.", -1);
        Index result = null;
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                result = new Index(Integer.parseInt(parts[i].trim()), result);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid index: '" + s + "'", e);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        appendTo(result);
        return result.toString();
    }

    private void appendTo(StringBuilder result) {
        result.append(value).append('.');
        if (nested != null) {
            nested.appendTo(result);
        }
    }

    @Override
    public int compareTo(Index other) {
        Index left = this;
        Index right = other;
        while (left != null && right != null) {
            if (left.value != right.value) {
                return left.value < right.value ? -1 : 1;
            }
            left = left.nested;
            right = right.nested;
        }
        if (left == null && right == null) {
            return 0;
        }
        return left == null ? -1 : 1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Index && compareTo((Index) other) == 0;
    }

    @Override
    public int hashCode() {
        int result = 1;
        Index current = this;
        while (current != null) {
            result = 31 * result + current.value;
            current = current.nested;
        }
        return result;
    }

}