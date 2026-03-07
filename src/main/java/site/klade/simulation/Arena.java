package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
// import static site.klade.simulation.Genome.MIN_INITIAL_DISTANCE;

public class Arena {

    // TODO: that's simulation setting. Should be moved there
    public static final float DISTANCE_TOLERANCE = 0.001f;
    // private final Genome genome;

    private final Engine engine = new Engine();

    private final Entity specimen = new Entity();

    private final Kinematics kinematics;

    private final Genome genome;

    private float fitness;

    public Arena(Genome genome) {
        this.genome = genome;
        kinematics = new Kinematics(new Genome());
        specimen.add(kinematics);
        engine.addEntity(specimen);
        engine.addSystem(new Movement());
        engine.addSystem(new Friction());
    }

    public void run() {
        // float distanceFromCenter = center.dst(genome.getStartPositon());
        // if (distanceFromCenter < MIN_INITIAL_DISTANCE) {
        //     fitness = Float.MAX_VALUE;
        //     genome.setFitness(fitness);
        //     return;
        // }
        // var positionBefore = new Vector2(10_000f, 10_000f);
        // var positionAfter = new Vector2();
        // while (positionBefore.dst(positionAfter) > DISTANCE_TOLERANCE) {
        //     positionBefore.set(positionAfter);
        //     engine.update(0f);
        //     positionAfter.set(specimenPhysics.getPositon());
        //     this.fitness = center.dst(positionAfter);
        // }
        // genome.setFitness(fitness);
    }

    @SuppressWarnings("unused")
    public void update() {
        engine.update(0f);
    }

    public Entity getSpecimen() {
        return specimen;
    }
}
