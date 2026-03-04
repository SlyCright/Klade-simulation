package site.klade.simulation;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class SpecimenPhysics implements Component {

    private final Vector2 acceleration;

    private final Vector2 velocity;

    private final Vector2 positon;

    public SpecimenPhysics(Genome genome) {
        this.acceleration = genome.getInitialImpulse();
        this.velocity = new Vector2(0f, 0f);
        this.positon = genome.getStartPositon();
    }

    public Vector2 getAcceleration() {
        return this.acceleration;
    }

    public Vector2 getVelocity() {
        return this.velocity;
    }

    public Vector2 getPositon() {
        return this.positon;
    }
}
