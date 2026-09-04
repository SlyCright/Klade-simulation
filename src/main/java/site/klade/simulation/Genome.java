package site.klade.simulation;

import java.util.ArrayList;
import java.util.List;

public class Genome {

    private final MetaGenes metaGenes;

    private List<Morphogen> morphogens = new ArrayList<Morphogen>();

    private List<Gene> genes = new ArrayList<Gene>();

    private float fitness = Float.MAX_VALUE;

    public Genome(MetaGenes metaGenes, List<Morphogen> morphogens, List<Gene> genes) {
        // TODO: consider deep copies here
        this.metaGenes = metaGenes;
        this.morphogens = new ArrayList<Morphogen>(morphogens);
        this.genes = new ArrayList<Gene>(genes);
    }

    public Genome(Genome genome) {
        // TODO: consider deep copies here
        this.metaGenes = genome.getMetaGenes();
        this.morphogens = new ArrayList<Morphogen>(genome.getMorphogens());
        this.genes = new ArrayList<Gene>(genome.getGenes());
        this.fitness = genome.getFitness();
    }

    public MetaGenes getMetaGenes() {
        return metaGenes;
    }

    public float getInitialAngle() {
        return metaGenes.getInitialAngle();
    }

    public List<Morphogen> getMorphogens() {
        return morphogens;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    @Override
    public String toString() {
        return String.format(
            "{\"metaGenes\": %s, \"morphogens\": %s, \"genes\": %s, \"fitness\": %f}",
            metaGenes, morphogens, genes, fitness
        );
    }

}
