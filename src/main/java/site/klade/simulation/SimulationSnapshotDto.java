package site.klade.simulation;

import java.util.ArrayList;

public class SimulationSnapshotDto {

    private final int generationNumber;

    private final ArrayList<Species> speciesList;

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
