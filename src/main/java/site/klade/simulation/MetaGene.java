package site.klade.simulation;

public class MetaGene {
    private MetaGeneType type;
    private float parameter;

    public MetaGene(MetaGeneType type, float parameter) {
        this.type = type;
        this.parameter = parameter;
    }

    public MetaGeneType getType() {
        return type;
    }

    public void setType(MetaGeneType type) {
        this.type = type;
    }

    public float getParameter() {
        return parameter;
    }

    public void setParameter(float parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return "MetaGene(type=" + type + ", parameter=" + parameter + ")";
    }
}
