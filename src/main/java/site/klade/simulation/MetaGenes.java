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

    // used in mutation
    public void setInitialAngle(float initialAngle) {this.initialAngle = initialAngle;
    }

    public float getInitialAngle() {
        return initialAngle;
    }

    /**
     * The canonical body of the {@code --- Meta genes} section, e.g.
     * {@code "hyperGene: 0.5 (Float)\ninitialAngle: 45 (Float)\n"}.
     *
     * <p>This is the same responsibility {@link site.klade.simulation.gene.Gene#toString()} has for a gene
     * line and {@link Morphogen#toString()} for a morphogen line: each part of the genome knows its own
     * canonical spelling, so the writer only assembles sections rather than defining formats. Delegating to
     * {@link MetaGeneRegistry} keeps the field names and their order in one place.</p>
     *
     * <p>The section marker and the explanatory comment are document assembly rather than meta-gene
     * spelling, so they belong to the writer and are not included here.</p>
     */
    @Override
    public String toString() {
        return MetaGeneRegistry.renderLines(this);
    }

}
