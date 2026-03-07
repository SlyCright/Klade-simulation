package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import static site.klade.simulation.Genome.MIN_INITIAL_DISTANCE;

public class Arena {

    // TODO: that's simulation setting. Should be moved there
    public static final float DISTANCE_TOLERANCE = 0.001f;

    private final Genome genome;

    private final Engine engine = new Engine();

    private final Entity specimen = new Entity();

    private final Kinematics kinematics;

    private final Vector2 center = new Vector2();

    public Arena(Genome genome) {
        this.genome = genome;
        kinematics = new Kinematics(genome);
        specimen.add(kinematics);
        engine.addEntity(specimen);
        engine.addSystem(new Movement());
        engine.addSystem(new Friction());
    }

    public void run() {
        float fitness = Float.MAX_VALUE;
        float distanceFromCenter = center.dst(genome.getStartPosition());
        if (distanceFromCenter < MIN_INITIAL_DISTANCE) {
            genome.setFitness(fitness);
            return;
        }
        var positionBefore = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
        var positionAfter = new Vector2();
        while (positionBefore.dst(positionAfter) > DISTANCE_TOLERANCE) {
            positionBefore.set(positionAfter);
            engine.update(0f);
            positionAfter.set(kinematics.getPosition());
            fitness = center.dst(positionAfter);
        }
        genome.setFitness(fitness);
    }

    @SuppressWarnings("unused")
    public void update() {
        engine.update(0f);
    }

    public Entity getSpecimen() {
        return specimen;
    }
}
