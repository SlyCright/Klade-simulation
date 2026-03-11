package site.klade.simulation;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;

public class Friction extends EntitySystem {

    // TODO: that's simulation setting. Should move there
    public static final float FRICTION_FACTOR = 0.01f;

    private final Family family = Family.all(Kinematics.class, ToleranceComponent.class).get();

    private final Vector2 friction = new Vector2();

    public Friction() {
        super(10);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            var toleranceComponent = entity.getComponent(ToleranceComponent.class);
            if (toleranceComponent.isToleranceReached()) continue;
            var kinematics = entity.getComponent(Kinematics.class);
            friction.set(kinematics.getVelocity()).scl(-FRICTION_FACTOR);
            kinematics.getAcceleration().add(friction);
        }
    }
}
