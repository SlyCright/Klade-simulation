package site.klade.simulation;

public class MetaGenes {

    private float initialAngle;     // Initial spawn angle in degrees (0°-360°)

    public MetaGenes() {
    }

    public MetaGenes(float initialAngle) {
        this.initialAngle = initialAngle;
    }

    public float getInitialAngle() {
        return initialAngle;
    }

    public void setInitialAngle(float initialAngle) {
        this.initialAngle = initialAngle;
    }

    @Override
    public String toString() {
        return String.format("{\"initialAngle\": %f}", initialAngle);
    }

}
