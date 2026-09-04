package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.Kinematics;

public class Movement extends EntitySystem {

    private final Family family = Family.all(Kinematics.class).get();

    public Movement(int priority) {
        super(priority);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            Kinematics kinematics = entity.getComponent(Kinematics.class);
            Vector2 acceleration = kinematics.acceleration;
            Vector2 velocity = kinematics.velocity;
            velocity.add(acceleration);
            kinematics.position.add(velocity);
            kinematics.acceleration.set(0f, 0f);
        }
    }
}
