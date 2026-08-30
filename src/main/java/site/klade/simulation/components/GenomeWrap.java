package site.klade.simulation.components;

import com.badlogic.ashley.core.Component;
import site.klade.simulation.Genome;

public class GenomeWrap implements Component {

    public final Genome genome;

    public GenomeWrap(Genome genome) {
        this.genome = genome;
    }

}
