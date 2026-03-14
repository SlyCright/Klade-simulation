package site.klade.simulation.components;

import com.badlogic.ashley.core.Component;

public class FitnessComponent implements Component {

    float fitness = Float.MAX_VALUE;

    public void set(float distance) {
        fitness = distance;
    }
}
