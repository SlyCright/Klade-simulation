package site.klade.simulation;

public class Gene {

    private String conditions;

    private GeneAction action;

    private String parameters;

    public Gene(String conditions, GeneAction action, String parameters) {
        this.conditions = conditions;
        this.action = action;
        this.parameters = parameters;
    }

    public Gene(Gene other) {
        this.conditions = other.conditions;
        this.action = other.action;
        this.parameters = other.parameters;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public GeneAction getAction() {
        return action;
    }

    public void setAction(GeneAction action) {
        this.action = action;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "Gene(condition=" + conditions + ", action=" + action + ", parameters=" + parameters + ")";
    }

}
