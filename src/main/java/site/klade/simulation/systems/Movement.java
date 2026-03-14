package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.ToleranceComponent;
import site.klade.simulation.components.Kinematics;

public class Movement extends EntitySystem {

    private final Family family = Family.all(Kinematics.class).get();

    public Movement() {
        super(20);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            var toleranceComponent = entity.getComponent(ToleranceComponent.class);
            if (toleranceComponent.isToleranceReached()) continue;
            var kinematics = entity.getComponent(Kinematics.class);
            Vector2 acceleration = kinematics.getAcceleration();
            Vector2 velocity = kinematics.getVelocity();
            velocity.add(acceleration);
            kinematics.getPosition().add(velocity);
            kinematics.getAcceleration().set(0f, 0f);
        }
    }
}
