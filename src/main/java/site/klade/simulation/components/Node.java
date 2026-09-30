package site.klade.simulation.components;

import com.badlogic.ashley.core.Component;
import site.klade.simulation.gene.ElementType;

public class Node implements Component {

    /**
     * The node's current element type. Defaults to {@link ElementType#STEM_NODE} — "no function yet" —
     * which is what {@code lay_segment} produces; {@code become} differentiates it during ontogenesis.
     */
    public ElementType elementType = ElementType.STEM_NODE;

    public int[] inputSegmentId;

    public int[] outputSegmentId;

}
