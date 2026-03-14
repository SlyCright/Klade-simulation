package site.klade.simulation.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.Genome;

public class Kinematics implements Component {

    private final Vector2 acceleration;

    private final Vector2 velocity;

    private final Vector2 position;

    public Kinematics(Genome genome) {
        this.acceleration = new Vector2(genome.getInitialImpulse());
        this.velocity = new Vector2(0f, 0f);
        this.position = new Vector2(genome.getStartPosition());
    }

    public Vector2 getAcceleration() {
        return this.acceleration;
    }

    public Vector2 getVelocity() {
        return this.velocity;
    }

    public Vector2 getPosition() {
        return this.position;
    }
}
