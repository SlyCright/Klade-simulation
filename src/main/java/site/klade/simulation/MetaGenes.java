package site.klade.simulation;

import java.util.Random;

public class MetaGenes {

    private float hyperGene; // (0.0, 1.0] Self-adaptive R_max hyper-gene: scales effective rank and mutation intensity

    private float initialAngle; // [0.0, 360.0] Initial spawn angle in degrees

    public MetaGenes(float hyperGene) {
        this.hyperGene = hyperGene;
        this.initialAngle = new Random().nextFloat() * 360.0f;
    }

    public MetaGenes(MetaGenes other) {
        this.hyperGene = other.hyperGene;
        this.initialAngle = other.initialAngle;
    }

    public void setHyperGene(float hyperGene) {
        this.hyperGene = hyperGene;
    }

    public float getHyperGene() {
        return hyperGene;
    }

    public float getInitialAngle() {
        return initialAngle;
    }

    public void setInitialAngle(float initialAngle) {
        this.initialAngle = initialAngle;
    }

    @Override
    public String toString() {
        return "{\"hyperGene\": " + hyperGene + ", \"initialAngle\": " + initialAngle + "}";
    }

}
