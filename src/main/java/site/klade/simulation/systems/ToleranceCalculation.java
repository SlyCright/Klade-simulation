package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.ToleranceStatus;
import site.klade.simulation.components.Kinematics;

public class ToleranceCalculation extends EntitySystem {

    // TODO: that's simulation setting. Should be moved there
    public static final float DISTANCE_TOLERANCE = 0.1f;

    private final Family family = Family.all(Kinematics.class, ToleranceStatus.class).get();

    public ToleranceCalculation(int priority) {
        super(priority);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (Entity entity : entities) {
            var kinematics = entity.getComponent(Kinematics.class);
            var tolerance = entity.getComponent(ToleranceStatus.class);
            Vector2 currentPosition = kinematics.getPosition();
            Vector2 previousPosition = tolerance.getPreviousPosition();
            float distance = currentPosition.dst(previousPosition);
            boolean toleranceReached = distance <= DISTANCE_TOLERANCE;
            tolerance.setToleranceReached(toleranceReached);
            tolerance.getPreviousPosition().set(currentPosition);
        }
    }
}
