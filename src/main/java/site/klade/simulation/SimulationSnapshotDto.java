package site.klade.simulation;

import lombok.Getter;

import java.util.ArrayList;

public class SimulationSnapshotDto {

    @Getter
    private final int generationNumber;

    @Getter
    private final ArrayList<Species> speciesList;

    public SimulationSnapshotDto(int generationNumber, ArrayList<Species> speciesList) {
        this.generationNumber = generationNumber;
        this.speciesList = speciesList;
    }
}
