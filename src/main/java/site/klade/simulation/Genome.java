package site.klade.simulation;

import java.util.ArrayList;
import java.util.List;

public class Genome {

    private final MetaGenes metaGenes;

    private List<Morphogen> morphogens = new ArrayList<Morphogen>();

    private List<Gene> genes = new ArrayList<Gene>();

    private float currentFitness = Float.MAX_VALUE;  // Updated every battle tick

    private float accumulatedFitness = 0.0f;         // Summed after every battle

    public Genome(float hyperGene) {
        this.metaGenes = new MetaGenes(hyperGene);
    }

    public Genome(Genome other) {
        this.metaGenes = new MetaGenes(other.getMetaGenes());
        List<Morphogen> otherMorphogenes = other.getMorphogens();
        this.morphogens = new ArrayList<Morphogen>(otherMorphogenes.size());
        for (Morphogen m : otherMorphogenes) {
            this.morphogens.add(new Morphogen(m));
        }
        List<Gene> otherGenes = other.getGenes();
        this.genes = new ArrayList<Gene>(otherGenes.size());
        for (Gene g : otherGenes) {
            this.genes.add(new Gene(g));
        }
        currentFitness = Float.MAX_VALUE;
        accumulatedFitness = 0.0f;
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
