package site.klade.simulation;

import java.util.ArrayList;
import java.util.List;

public class Genome {

    private MetaGenes metaGenes = new MetaGenes();

    private List<Morphogen> morphogens = new ArrayList<Morphogen>();

    private List<Gene> genes = new ArrayList<Gene>();

    private float fitness = Float.MAX_VALUE;

    public Genome() {
    }

    public Genome(MetaGenes metaGenes, List<Morphogen> morphogens, List<Gene> genes, float fitness) {
        this.metaGenes = new MetaGenes(metaGenes);
        this.morphogens = new ArrayList<Morphogen>(morphogens.size());
        for (Morphogen m : morphogens) {
            this.morphogens.add(new Morphogen(m));
        }
        this.genes = new ArrayList<Gene>(genes.size());
        for (Gene g : genes) {
            this.genes.add(new Gene(g));
        }
        this.fitness = fitness;
    }

    public Genome(MetaGenes metaGenes, List<Morphogen> morphogens, List<Gene> genes) {
        this(metaGenes, morphogens, genes, Float.MAX_VALUE);
    }

    public Genome(Genome genome) {
        this(genome.getMetaGenes(), genome.getMorphogens(), genome.getGenes(), genome.getFitness());
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
