package site.klade.simulation;

public class Morphogen {

    private int id;

    private float diffusionRatio;

    private float decayRatio;

    private String spreadingConditions;

    public Morphogen(int id, float diffusionRatio, float decayRatio, String spreadingConditions) {
        this.id = id;
        this.diffusionRatio = diffusionRatio;
        this.decayRatio = decayRatio;
        this.spreadingConditions = spreadingConditions;
    }

    public Morphogen(Morphogen other) {
        this.id = other.id;
        this.diffusionRatio = other.diffusionRatio;
        this.decayRatio = other.decayRatio;
        this.spreadingConditions = other.spreadingConditions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getDiffusionRatio() {
        return diffusionRatio;
    }

    public void setDiffusionRatio(float diffusionRatio) {
        this.diffusionRatio = diffusionRatio;
    }

    public float getDecayRatio() {
        return decayRatio;
    }

    public void setDecayRatio(float decayRatio) {
        this.decayRatio = decayRatio;
    }

    public String getSpreadingConditions() {
        return spreadingConditions;
    }

    public void setSpreadingConditions(String spreadingConditions) {
        this.spreadingConditions = spreadingConditions;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"id\": %d, \"diffusionRatio\": %f, \"decayRatio\": %f, \"spreadingConditions\": %s}",
                id, diffusionRatio, decayRatio, spreadingConditions
        );
    }

}
