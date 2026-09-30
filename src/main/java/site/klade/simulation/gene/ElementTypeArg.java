package site.klade.simulation.gene;

/**
 * The element-type argument of {@code become}, e.g. the {@code muscle} of
 * {@code become muscle length 40%}.
 *
 * <p>Unlike {@link NumberArg} and {@link ReferenceArg}, this argument is not self-describing: it is a
 * keyword, and {@link #toString()} emits {@link ElementType#canonicalName()} so the line reads as
 * English.</p>
 *
 * <p>It is also the only argument that <b>determines the arity of the gene</b>: {@code become}
 * contributes {@link ElementTypeSpec}, and the chosen type then contributes its own
 * {@link ElementType#parameterSpecs()}. See {@link GeneSpecs#specsOf}.</p>
 */
public final class ElementTypeArg implements GeneArg {

    public final ElementType type;

    public ElementTypeArg(ElementType type) {
        if (type == null) throw new IllegalArgumentException("element type argument is null");
        this.type = type;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ElementTypeArg) && type == ((ElementTypeArg) other).type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return type.canonicalName();
    }
}
