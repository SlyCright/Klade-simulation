package site.klade.simulation;

import java.util.ArrayList;
import java.util.List;

public class Genome {

    private MetaGenes metaGenes = new MetaGenes();

    private List<Morphogen> morphogens = new ArrayList<Morphogen>();

    private List<Gene> genes = new ArrayList<Gene>();

    private float currentFitness = Float.MAX_VALUE;  // Updated every battle tick

    private float accumulatedFitness = 0.0f;         // Summed after every battle

    public Genome() {
    }

    public Genome(
            MetaGenes metaGenes,
            List<Morphogen> morphogens,
            List<Gene> genes,
            float currentFitness,
            float accumulatedFitness
    ) {
        this.metaGenes = new MetaGenes(metaGenes);
        this.morphogens = new ArrayList<Morphogen>(morphogens.size());
        for (Morphogen m : morphogens) {
            this.morphogens.add(new Morphogen(m));
        }
        this.genes = new ArrayList<Gene>(genes.size());
        for (Gene g : genes) {
            this.genes.add(new Gene(g));
        }
        this.currentFitness = currentFitness;
        this.accumulatedFitness = accumulatedFitness;
    }

    public Genome(MetaGenes metaGenes, List<Morphogen> morphogens, List<Gene> genes) {
        this(metaGenes, morphogens, genes, Float.MAX_VALUE, 0.0f);
    }

    public Genome(Genome genome) {
        this(
                genome.getMetaGenes(),
                genome.getMorphogens(),
                genome.getGenes(),
                genome.getCurrentFitness(),
                genome.getAccumulatedFitness());
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

    public float getCurrentFitness() {
        return currentFitness;
    }

    public void setCurrentFitness(float currentFitness) {
        this.currentFitness = currentFitness;
    }

    public float getAccumulatedFitness() {
        return accumulatedFitness;
    }

    public void setAccumulatedFitness(float accumulatedFitness) {
        this.accumulatedFitness = accumulatedFitness;
    }

    public void resetCurrentFitness() {
        this.currentFitness = Float.MAX_VALUE;
    }

    public void resetFitnesses() {
        this.currentFitness = Float.MAX_VALUE;
        this.accumulatedFitness = 0.0f;
    }

    public void updateAccumulatedFitness() {
        accumulatedFitness += currentFitness;
    }

    @Override
    public String toString() {
        return "{\"metaGenes\": " + metaGenes + ", \"morphogens\": " + morphogens + ", \"genes\": " + genes + ", \"currentFitness\": " + currentFitness + ", \"accumulatedFitness\": " + accumulatedFitness + "}";
    }

}
