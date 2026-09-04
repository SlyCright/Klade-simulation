package site.klade.simulation.components;

import com.badlogic.ashley.core.Component;

public class Node implements Component {

    public NodeType nodeType;

    public int[] inputSegmentId;

    public int[] outputSegmentId;

}
