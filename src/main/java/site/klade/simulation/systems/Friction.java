package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.Kinematics;

public class Friction extends EntitySystem {

    private final float frictionFactor;

    private final Family family = Family.all(Kinematics.class).get();

    private final Vector2 friction = new Vector2();

    public Friction(int priority, float frictionFactor) {
        super(priority);
        this.frictionFactor = frictionFactor;
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : getEngine().getEntitiesFor(family)) {
            Kinematics kinematics = entity.getComponent(Kinematics.class);
            friction.set(kinematics.velocity).scl(-frictionFactor);
            kinematics.acceleration.add(friction);
        }
    }
}
