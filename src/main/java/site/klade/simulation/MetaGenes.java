package site.klade.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MetaGenes {

    private float hyperGene; // (0.0, 1.0] Self-adaptive R_max hyper-gene: scales effective rank and mutation intensity

    private float initialAngle; // [0.0, 360.0] Initial spawn angle in degrees

    // Constructor for parser
    public MetaGenes() {
        this.hyperGene = 0.5f;
        this.initialAngle = 0.0f;
    }

    // Constructor for first generation initialization and loading from DTO
    private MetaGenes(float hyperGene, float initialAngle) {
        this.hyperGene = hyperGene;
        this.initialAngle = initialAngle;
    }

    // Constructor for offspring creation
    public MetaGenes(MetaGenes other) {
        this.hyperGene = other.hyperGene;
        this.initialAngle = other.initialAngle;
    }

    public static MetaGenes createWithHyperGene(float hyperGene) { // for first generation initialization
        return new MetaGenes(hyperGene, new Random().nextFloat() * 360.0f);
    }

    public static MetaGenes createWithInitialAngle(float initialAngle) { // for creation from DTO
        return new MetaGenes(0.5f, initialAngle);
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
