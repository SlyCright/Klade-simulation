package site.klade.simulation;

public class ArenaSettingsDto {
    
    private float initialDistance;

    private float frictionFactor; // TODO: set 0.01f as default in simulation settings in Main project;

    private float specimenSize; // TODO: set 18f as default in simulation settings in Main project;

    private float repulsionForceMultiplier; // TODO: set 10f as default in simulation settings in Main project;

    public ArenaSettingsDto() {
    }

    public ArenaSettingsDto(float initialDistance, float frictionFactor, float specimenSize, float repulsionForceMultiplier) {
        this.initialDistance = initialDistance;
        this.frictionFactor = frictionFactor;
        this.specimenSize = specimenSize;
        this.repulsionForceMultiplier = repulsionForceMultiplier;
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

    public float getSpecimenSize() {
        return specimenSize;
    }

    public void setSpecimenSize(float specimenSize) {
        this.specimenSize = specimenSize;
    }

    public float getRepulsionForceMultiplier() {
        return repulsionForceMultiplier;
    }

    public void setRepulsionForceMultiplier(float repulsionForceMultiplier) {
        this.repulsionForceMultiplier = repulsionForceMultiplier;
    }

    @Override
    public String toString() {
        return "ArenaSettingsDto(initialDistance=" + initialDistance + ", frictionFactor=" + frictionFactor + ", specimenSize=" + specimenSize + ", repulsionForceMultiplier=" + repulsionForceMultiplier + ")";
    }
}
