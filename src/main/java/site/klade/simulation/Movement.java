package site.klade.simulation;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;

public class Movement extends EntitySystem {

    private final Family family = Family.all(Kinematics.class).get();

    public Movement() {
        super(20);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            var physics = entity.getComponent(Kinematics.class);
            Vector2 acceleration = physics.getAcceleration();
            Vector2 velocity = physics.getVelocity();
            velocity.add(acceleration);
            physics.getPosition().add(velocity);
            physics.getAcceleration().set(0f, 0f);
        }
    }
}
