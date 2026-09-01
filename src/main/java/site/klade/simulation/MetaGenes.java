package site.klade.simulation;

public class MetaGenes {

    private float initialPositionAngleDegrees;

    public MetaGenes() {
    }

    public MetaGenes(float initialPositionAngleDegrees) {
        this.initialPositionAngleDegrees = initialPositionAngleDegrees;
    }

    public float getInitialPositionAngleDegrees() {
        return initialPositionAngleDegrees;
    }

    public void setInitialPositionAngleDegrees(float initialPositionAngleDegrees) {
        this.initialPositionAngleDegrees = initialPositionAngleDegrees;
    }

    @Override
    public String toString() {
        return "MetaGenes(initialPositionAngleDegrees=" + initialPositionAngleDegrees + ")";
    }
}
