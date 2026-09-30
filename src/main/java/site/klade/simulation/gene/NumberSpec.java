package site.klade.simulation.gene;

/**
 * Signature slot for a numeric argument.
 *
 * <p>Carries everything needed to <i>read</i> and <i>write</i> the argument: an optional {@link #label},
 * an optional {@link #unit}, and the {@link NumberKind} the mutation operator dispatches on. It carries
 * no range — see {@link ArgSpec}.</p>
 *
 * <p>Exactly one of the two spellings is used on the DNA line, chosen by which field is present:</p>
 * <ul>
 *   <li>{@code label} present → {@code <label> <value><unit>}, e.g. {@code length 40%};</li>
 *   <li>{@code label} absent → {@code <value><unit>}, e.g. {@code 30°}.</li>
 * </ul>
 *
 * <p>Value equality is by all three fields, so specs can be compared in tests and looked up in maps.</p>
 */
public final class NumberSpec implements ArgSpec {

    /** Human-readable introducer, e.g. {@code "length"}, {@code "period"}, {@code "amount"}; or null. */
    public final String label;

    /** Display unit, e.g. {@code "°"}, {@code "%"}; never null, possibly empty. */
    public final String unit;

    /** Whether the value is a real quantity or a count. Never null. */
    public final NumberKind kind;

    public NumberSpec(String label, String unit, NumberKind kind) {
        if (kind == null) throw new IllegalArgumentException("number spec kind is null");
        String normalizedLabel = (label == null || label.isEmpty()) ? null : label;
        String normalizedUnit = (unit == null) ? "" : unit;
        if (normalizedLabel == null && normalizedUnit.isEmpty()) {
            throw new IllegalArgumentException(
                    "a number spec needs a label, a unit, or both; otherwise the argument is unreadable");
        }
        this.label = normalizedLabel;
        this.unit = normalizedUnit;
        this.kind = kind;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NumberSpec)) {
            return false;
        }
        NumberSpec spec = (NumberSpec) other;
        return kind == spec.kind
                && unit.equals(spec.unit)
                && (label == null ? spec.label == null : label.equals(spec.label));
    }

    @Override
    public int hashCode() {
        int result = kind.hashCode();
        result = 31 * result + unit.hashCode();
        result = 31 * result + (label == null ? 0 : label.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "NumberSpec(" + kind + ", label=" + label + ", unit=" + unit + ")";
    }
}
