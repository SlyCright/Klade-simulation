package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import site.klade.simulation.components.GenomeWrap;
import site.klade.simulation.components.Kinematics;

public class FitnessCalculation extends EntitySystem {

    private final Family family = Family.all(Kinematics.class, GenomeWrap.class).get();

    public FitnessCalculation() {
        super(100);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            var kinematics = entity.getComponent(Kinematics.class);
            var fitness = entity.getComponent(GenomeWrap.class);
            var position = kinematics.getPosition();
            fitness.setFitness(position.dst(0f, 0f));
        }
    }
}
