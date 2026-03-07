package site.klade.simulation;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;

public class Friction extends EntitySystem {

    // TODO: that's simulation setting. Should move there
    public static final float FRICTION_FACTOR = 0.01f;

    private final Family family = Family.all(Kinematics.class).get();

    public Friction() {
        super(10);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            var physics = entity.getComponent(Kinematics.class);
            Vector2 friction = new Vector2();
            friction.set(physics.getVelocity()).scl(-FRICTION_FACTOR);
            physics.getAcceleration().add(friction);
        }
    }
}
