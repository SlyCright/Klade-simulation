package site.klade.simulation;

import lombok.Getter;
import lombok.Setter;

public class Genome {

    @Setter
    @Getter
    private float fitness;

    public Genome() {
    }

    public Genome(Genome winner) {
        // TODO: implement
    }
}
