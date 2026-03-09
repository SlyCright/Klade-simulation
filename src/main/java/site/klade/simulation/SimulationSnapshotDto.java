package site.klade.simulation;

import java.util.ArrayList;

public class SimulationSnapshotDto {

    private int generationNumber;

    private ArrayList<Species> speciesList;

    // Add no-arg constructor for Jackson
    public SimulationSnapshotDto() {
    }

    public SimulationSnapshotDto(int generationNumber, ArrayList<Species> speciesList) {
        this.generationNumber = generationNumber;
        this.speciesList = speciesList;
    }

    public int getGenerationNumber() {
        return this.generationNumber;
    }

    public ArrayList<Species> getSpeciesList() {
        return this.speciesList;
    }

    public String toString() {
        return "SimulationSnapshotDto(generationNumber=" + this.getGenerationNumber() + ", speciesList=" + this.getSpeciesList() + ")";
    }
}
