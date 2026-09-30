package site.klade.simulation.gene;

/**
 * Signature slot for the element-type argument of {@code become}, e.g. the {@code muscle} in
 * {@code become muscle length 40%}.
 *
 * <p>Has no label of its own: the value <i>is</i> a word ({@link ElementType#canonicalName()}), so
 * anything introduced before it would only add noise. It is the only argument slot that can be
 * followed by further, value-dependent arguments — the chosen type contributes its own
 * {@link ElementType#parameterSpecs()}, which is how {@code become} gets dependent arity.</p>
 *
 * <p>A stateless singleton: use {@link #INSTANCE}.</p>
 */
public final class ElementTypeSpec implements ArgSpec {

    public static final ElementTypeSpec INSTANCE = new ElementTypeSpec();

    private ElementTypeSpec() {
    }

    @Override
    public String label() {
        return null;
    }

    @Override
    public String toString() {
        return "ElementTypeSpec";
    }
}
