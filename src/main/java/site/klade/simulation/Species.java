package site.klade.simulation;

import lombok.Getter;

import java.util.ArrayList;

public class Species {

    @Getter
    private final ArrayList<Genome> genomes = new ArrayList<>();

    public Species(int specimensPerSpecies) {
        for (int i = 0; i < specimensPerSpecies; i++) {
            genomes.add(new Genome());
        }
    }

    public Species(ArrayList<Genome> genomes) {
        this.genomes.addAll(genomes);
    }
}
