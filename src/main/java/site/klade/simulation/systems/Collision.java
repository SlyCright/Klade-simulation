package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.Kinematics;

public class Collision extends EntitySystem {

    // TODO: that's simulation setting. Should move there.
    //  Also it should determines how the Specimen Renderer from "Stage" represents a specimen
    public static final float SPECIMEN_SIZE = 18f;

    public static final float SHIFT_AMOUNT = 0.001f;

    public static final float REPULSION_FORCE_MULTIPLIER = 10f;

    private final Family family = Family.all(Kinematics.class).get();

    private final Vector2 forceVectorI = new Vector2();
    private final Vector2 forceVectorJ = new Vector2();

    public Collision(int priority) {
        super(priority);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (int i = 0; i < entities.size(); i++) {
            Kinematics componentI = entities.get(i).getComponent(Kinematics.class);
            Vector2 positionI = componentI.getPosition();
            for (int j = i + 1; j < entities.size(); j++) {
                Kinematics componentJ = entities.get(j).getComponent(Kinematics.class);
                Vector2 positionJ = componentJ.getPosition();
                float distance = positionI.dst(positionJ);
                if (distance > SPECIMEN_SIZE) continue;
                if (distance > 0) { // "distance > 0" guard against zero divide
                    handleCollision(componentI, componentJ, distance);
                    continue;
                }
                handleExactOverlap(componentI, componentJ);
            }
        }
    }

    private void handleCollision(Kinematics componentI, Kinematics componentJ, float distance) {
        Vector2 positionI = componentI.getPosition();
        Vector2 positionJ = componentJ.getPosition();

        forceVectorI.set(positionI);
        forceVectorI.sub(positionJ);
        forceVectorI.nor();

        float repulsionStrength = (SPECIMEN_SIZE - distance) / SPECIMEN_SIZE;
        float forceValue = repulsionStrength * REPULSION_FORCE_MULTIPLIER;
        forceVectorI.scl(forceValue);
        forceVectorJ.set(forceVectorI).scl(-1f);
        componentI.getAcceleration().add(forceVectorI);
        componentJ.getAcceleration().add(forceVectorJ);
    }

    private void handleExactOverlap(Kinematics componentI, Kinematics componentJ) {
        boolean bothStationary = componentI.getVelocity().isZero()
                && componentJ.getVelocity().isZero()
                && componentI.getAcceleration().isZero()
                && componentJ.getAcceleration().isZero();

        if (bothStationary) {
            componentI.getPosition().add(MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT),
                    MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT));
            componentJ.getPosition().add(MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT),
                    MathUtils.random(-SHIFT_AMOUNT, SHIFT_AMOUNT));
        }
    }
}
