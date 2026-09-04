package site.klade.simulation;

public class Gene {

    private String condition;

    private GeneAction action;

    private String parameters;

    public Gene(String condition, GeneAction action, String parameters) {
        this.condition = condition;
        this.action = action;
        this.parameters = parameters;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
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
        return "Gene(condition=" + condition + ", action=" + action + ", parameters=" + parameters + ")";
    }

}
