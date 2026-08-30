package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import site.klade.simulation.components.Kinematics;
import site.klade.simulation.components.ToleranceStatus;

public class ToleranceExclusion extends EntitySystem {

    private final Family family = Family.all(Kinematics.class, ToleranceStatus.class).get();

    public ToleranceExclusion(int priority) {
        super(priority);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        
        for (Entity entity : entities) {
            var toleranceComponent = entity.getComponent(ToleranceStatus.class);
            var kinematics = entity.getComponent(Kinematics.class);
            
            if (toleranceComponent.isToleranceReached()) {
                kinematics.getVelocity().setZero();
                kinematics.getAcceleration().setZero();
            }
        }
    }
}
