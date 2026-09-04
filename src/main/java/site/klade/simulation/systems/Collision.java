package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.Kinematics;

public class Collision extends EntitySystem {

    private static final float SHIFT_AMOUNT = 0.001f;

    private final float nodeSize;

    private final float repulsionFactor;

    private final Family family = Family.all(Kinematics.class).get();

    private final Vector2 forceVectorI = new Vector2();

    private final Vector2 forceVectorJ = new Vector2();

    public Collision(int priority, float nodeSize, float repulsionFactor) {
        super(priority);
        this.nodeSize = nodeSize;
        this.repulsionFactor = repulsionFactor;
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (int i = 0; i < entities.size(); i++) {
            Kinematics componentI = entities.get(i).getComponent(Kinematics.class);
            for (int j = i + 1; j < entities.size(); j++) {
                Kinematics componentJ = entities.get(j).getComponent(Kinematics.class);
                handleCollision(componentI, componentJ);
            }
        }
    }

    private void handleCollision(Kinematics componentI, Kinematics componentJ) {
        float distance = componentI.position.dst(componentJ.position);
        if (distance > nodeSize) return;
        if (distance > 0) {
            forceVectorI.set(componentI.position);
            forceVectorI.sub(componentJ.position);
            forceVectorI.nor();
            float repulsionStrength = (nodeSize - distance) / nodeSize;
            float forceValue = repulsionStrength * repulsionFactor;
            forceVectorI.scl(forceValue);
            forceVectorJ.set(forceVectorI).scl(-1f);
            componentI.acceleration.add(forceVectorI);
            componentJ.acceleration.add(forceVectorJ);
            return;
        }
        handleExactOverlap(componentI, componentJ);
    }

    private void handleExactOverlap(Kinematics componentI, Kinematics componentJ) {
        boolean bothStationary = componentI.velocity.isZero()
                && componentJ.velocity.isZero()
                && componentI.acceleration.isZero()
                && componentJ.acceleration.isZero();
        if (bothStationary) {
            componentI.position.add(MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT),
                    MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT));
            componentJ.position.add(MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT),
                    MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT));
        }
    }
}
