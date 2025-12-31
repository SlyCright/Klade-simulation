package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

@SuppressWarnings("unused")
public class Simulation {

    private final Engine engine = new Engine();

    private Entity stateEntity;

    public Simulation() {
        initialize();
    }

    public void reset() {
        initialize();
    }

    private void initialize() {
        stateEntity = EntityFactory.getStateEntity();
        engine.addEntity(stateEntity);
        engine.addEntity(EntityFactory.getRhythmNodeEntity());
        engine.addSystem(new SimulationSystem());
    }

    public void update() {
        engine.update(0.0f);
    }


    public SimulationDto getSimulationDto() {
        SimulationStateComponent stateComponent =
                stateEntity.getComponent(SimulationStateComponent.class);
        return new SimulationDto(stateComponent.totalTicks, stateComponent.rhymeNodeStateChanges, stateComponent.isRhymeNodeCurrentlyActive);
    }
}
