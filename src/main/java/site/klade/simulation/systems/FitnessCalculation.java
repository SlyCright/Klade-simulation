package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import site.klade.simulation.components.FitnessComponent;
import site.klade.simulation.components.Kinematics;

public class FitnessCalculation extends EntitySystem {

    private final Family family = Family.all(Kinematics.class, FitnessComponent.class).get();

    public FitnessCalculation() {
        super(100);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            var kinematics = entity.getComponent(Kinematics.class);
            var fitness = entity.getComponent(FitnessComponent.class);
            var position = kinematics.getPosition();
            fitness.set(position.dst(0f, 0f));
        }
    }
}
