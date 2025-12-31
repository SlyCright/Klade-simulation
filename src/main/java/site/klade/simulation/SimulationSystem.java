package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

public class SimulationSystem extends EntitySystem {

    private ImmutableArray<Entity> simulationEntities;

    private ImmutableArray<Entity> rhythmNodeEntities;

    @Override
    public void addedToEngine(Engine engine) {
        var simulationFamily = Family.all(SimulationStateComponent.class).get();
        simulationEntities = engine.getEntitiesFor(simulationFamily);
        var rhythmNodeFamily = Family.all(RhythmNodeComponent.class).get();
        rhythmNodeEntities = engine.getEntitiesFor(rhythmNodeFamily);
    }

    @Override
    public void update(float deltaTime) {
        for (Entity simulationEntity : simulationEntities) {
            for (Entity rhythmNodeEntity : rhythmNodeEntities) {
                var rhythmNode = rhythmNodeEntity.getComponent(RhythmNodeComponent.class);
                var simulation = simulationEntity.getComponent(SimulationStateComponent.class);
                rhythmNode.tick++;
                long currentTick = rhythmNode.tick;
                simulation.totalTicks = currentTick;
                if (currentTick % RhythmNodeComponent.PERIOD_TICKS == 0) {
                    rhythmNode.isActive = !rhythmNode.isActive;
                    simulation.isRhymeNodeCurrentlyActive = rhythmNode.isActive;
                    simulation.rhymeNodeStateChanges++;
                }
            }
        }
    }
}
