package site.klade.simulation;

public class ArenaSettingsDto {

    private float initialDistance;

    private float frictionFactor; // TODO: set 0.01f as default in simulation settings in Main project;

    private float nodeSize; // TODO: set 18f as default in simulation settings in Main project;

    private float repulsionFactor; // TODO: set 10f as default in simulation settings in Main project;

    private int maxTicks; // TODO: set 3000 as default in simulation settings in Main project

    public ArenaSettingsDto() {
    }

    public ArenaSettingsDto(float initialDistance, float frictionFactor, float nodeSize, float repulsionFactor, int maxTicks) {
        this.initialDistance = initialDistance;
        this.frictionFactor = frictionFactor;
        this.nodeSize = nodeSize;
        this.repulsionFactor = repulsionFactor;
        this.maxTicks = maxTicks;
    }

    public float getInitialDistance() {
        return initialDistance;
    }

    public void setInitialDistance(float initialDistance) {
        this.initialDistance = initialDistance;
    }

    public float getFrictionFactor() {
        return frictionFactor;
    }

    public void setFrictionFactor(float frictionFactor) {
        this.frictionFactor = frictionFactor;
    }

    public float getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(float nodeSize) {
        this.nodeSize = nodeSize;
    }

    public float getRepulsionFactor() {
        return repulsionFactor;
    }

    public void setRepulsionFactor(float repulsionFactor) {
        this.repulsionFactor = repulsionFactor;
    }

    public int getMaxTicks() {
        return maxTicks;
    }

    public void setMaxTicks(int maxTicks) {
        this.maxTicks = maxTicks;
    }

    @Override
    public String toString() {
        return String.format(
            "{\"initialDistance\": %f, \"frictionFactor\": %f, \"nodeSize\": %f, \"repulsionFactor\": %f, \"maxTicks\": %d}",
            initialDistance, frictionFactor, nodeSize, repulsionFactor, maxTicks
        );
    }

}
