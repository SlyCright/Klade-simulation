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
 */
public final class Index implements Comparable<Index> {
// TODO: too many logic in the class. Keep it with recurrency and data and comparison only. All logic move to Indexes.

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

    public int segmentCount() {
        return nested == null ? 1 : 1 + nested.segmentCount();
    }

    public int segmentAt(int position) {
        if (position < 0) {
            throw new IndexOutOfBoundsException("negative segment position: " + position);
        }
        Index current = this;
        for (int i = 0; i < position; i++) {
            current = current.nested;
            if (current == null) {
                throw new IndexOutOfBoundsException("segment position: " + position);
            }
        }
        return current.value;
    }

    public Index incrementLast() {
        Index last = this;
        while (last.nested != null) {
            last = last.nested;
        }
        if (last.value == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("index segment overflow");
        }
        return replaceLast(last.value + 1);
    }

    public Index decrementLast() {
        Index last = this;
        while (last.nested != null) {
            last = last.nested;
        }
        if (last.value == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("index segment underflow");
        }
        return replaceLast(last.value - 1);
    }

    public Index incrementFirst() {
        if (value == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("index segment overflow");
        }
        return new Index(value + 1);
    }

    public Index decrementFirst() {
        if (value == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("index segment underflow");
        }
        return new Index(value - 1);
    }

    public Index extend(int segment) {
        return new Index(value, appendSegment(nested, segment));
    }

    public boolean isPrefixOf(Index other) {
        if (other == null) {
            return false;
        }
        Index prefix = this;
        Index current = other;
        while (prefix != null) {
            if (current == null || prefix.value != current.value) {
                return false;
            }
            prefix = prefix.nested;
            current = current.nested;
        }
        return true;
    }

    public Index parent() {
        if (nested == null) {
            return null;
        }
        return new Index(value, nested.parent());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        appendTo(result);
        return result.toString();
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

    private Index replaceLast(int newValue) {
        if (nested == null) {
            return new Index(newValue);
        }
        return new Index(value, nested.replaceLast(newValue));
    }

    private void appendTo(StringBuilder result) {
        result.append(value).append('.');
        if (nested != null) {
            nested.appendTo(result);
        }
    }

    private static Index appendSegment(Index current, int segment) {
        return current == null
                ? new Index(segment)
                : new Index(current.value, appendSegment(current.nested, segment));
    }

}
