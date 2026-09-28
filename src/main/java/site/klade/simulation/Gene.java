package site.klade.simulation;

import site.klade.simulation.condition.Cond;

public class Gene {

    private Index index;

    private Cond conditions;

    private GeneAction action;

    private String parameters;

    public Gene(Index index, Cond conditions, GeneAction action, String parameters) {
        this.index = index;
        this.conditions = conditions;
        this.action = action;
        this.parameters = parameters;
    }

    public Gene(Gene other) {
        this.index = other.index;  // Index is immutable. So consider as deep copy constructor
        this.conditions = other.conditions;  // Cond is immutable. So consider as deep copy constructor
        this.action = other.action;
        this.parameters = other.parameters;
    }

    public Cond getConditions() {
        return conditions;
    }

    public void setConditions(Cond conditions) {
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

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Gene(" + index + " condition=" + (conditions == null ? "" : conditions) + ", action=" + action
                + ", parameters=" + parameters + ")";
    }

}
