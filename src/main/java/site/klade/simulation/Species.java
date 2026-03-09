package site.klade.simulation;

import java.util.ArrayList;

public class Species {

    private  ArrayList<Genome> genomes = new ArrayList<>();

    public Species() {
        this.genomes = new ArrayList<>();
    }

    public Species(int specimensPerSpecies) {
        for (int i = 0; i < specimensPerSpecies; i++) {
            genomes.add(new Genome());
        }
    }

    public Species(ArrayList<Genome> genomes) {
        this.genomes.addAll(genomes);
    }

    public String toString() {
        return "Species(genomes=" + this.genomes + ")";
    }

    public ArrayList<Genome> getGenomes() {
        return this.genomes;
    }
}
