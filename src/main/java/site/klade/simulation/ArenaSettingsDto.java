package site.klade.simulation;

public class ArenaSettingsDto {
    
    private float initialDistance;

    public ArenaSettingsDto() {
    }

    public ArenaSettingsDto(float initialDistance) {
        this.initialDistance = initialDistance;
    }

    public float getInitialDistance() {
        return initialDistance;
    }

    public void setInitialDistance(float initialDistance) {
        this.initialDistance = initialDistance;
    }

    @Override
    public String toString() {
        return "ArenaSettingsDto(initialDistance=" + initialDistance + ")";
    }
}
