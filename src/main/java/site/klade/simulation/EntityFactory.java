package site.klade.simulation;

import com.badlogic.ashley.core.Entity;

public class EntityFactory {

    public static Entity getStateEntity() {
        return new Entity().add(new SimulationStateComponent());
    }

    public static Entity getRhythmNodeEntity() {
        return new Entity().add(new RhythmNodeComponent());
    }
}
