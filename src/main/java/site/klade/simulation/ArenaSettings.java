package site.klade.simulation;

public class ArenaSettings {

    private final float initialDistance;

    private final float frictionFactor;

    private final float nodeSize;

    private final float repulsionFactor;

    private final int maxTicks;

    public ArenaSettings(float initialDistance, float frictionFactor, float nodeSize, float repulsionFactor, int maxTicks) {
        this.initialDistance = initialDistance;
        this.frictionFactor = frictionFactor;
        this.nodeSize = nodeSize;
        this.repulsionFactor = repulsionFactor;
        this.maxTicks = maxTicks;
    }

    public float getInitialDistance() {
        return initialDistance;
    }

    public float getFrictionFactor() {
        return frictionFactor;
    }

    public float getNodeSize() {
        return nodeSize;
    }

    public float getRepulsionFactor() {
        return repulsionFactor;
    }

    public int getMaxTicks() {
        return maxTicks;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"initialDistance\": %f, \"frictionFactor\": %f, \"nodeSize\": %f, \"repulsionFactor\": %f, \"maxTicks\": %d}",
                initialDistance, frictionFactor, nodeSize, repulsionFactor, maxTicks
        );
    }

}
