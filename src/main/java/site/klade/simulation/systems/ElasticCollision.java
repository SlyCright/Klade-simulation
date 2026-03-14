package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.Kinematics;

public class ElasticCollision extends EntitySystem {

    // TODO: that's simulation setting. Should move there.
    //  Also it should determines how the Specimen Renderer from "Stage" represents a specimen
    public static final float SPECIMEN_SIZE = 18f;

    private final Family family = Family.all(Kinematics.class).get();

    private final Vector2 normalI = new Vector2();

    private final Vector2 normalJ = new Vector2();

    public ElasticCollision() {
        super(15);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(family);
        for (int i = 0; i < entities.size(); i++) {
            MotionVectors vectorsI = extractEntityData(entities.get(i));
            for (int j = i + 1; j < entities.size(); j++) {
                MotionVectors vectorsJ = extractEntityData(entities.get(j));
                float distance = vectorsI.position.dst(vectorsJ.position);
                if (distance > SPECIMEN_SIZE) continue;
                if (distance > 0) handleCollision(vectorsI, vectorsJ);
            }
        }
    }

    private MotionVectors extractEntityData(Entity entity) {
        var kinematics = entity.getComponent(Kinematics.class);
        return new MotionVectors(
                kinematics.getPosition(),
                kinematics.getVelocity());
    }

    private void handleCollision(MotionVectors vectorsI, MotionVectors vectorsJ) {
        normalI.set(vectorsI.position).sub(vectorsJ.position).nor();
        normalJ.set(normalI).scl(-1f);
        float relativeVelocity = vectorsI.velocity.dot(normalI) - vectorsJ.velocity.dot(normalJ);
        if (relativeVelocity < 0) return;
        float impulse = relativeVelocity / 2f;
        updateVelocity(vectorsI.velocity, normalI, impulse);
        updateVelocity(vectorsJ.velocity, normalJ, impulse);
    }

    private void updateVelocity(Vector2 velocity, Vector2 direction, float impulse) {
        velocity.sub(direction.scl(impulse));
    }

    private static class MotionVectors {

        final Vector2 position;

        final Vector2 velocity;

        MotionVectors(Vector2 position, Vector2 velocity) {
            this.position = position;
            this.velocity = velocity;
        }
    }
}
