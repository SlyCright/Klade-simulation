package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import static site.klade.simulation.Genome.MIN_INITIAL_DISTANCE;

public class Arena {

    // TODO: that's simulation setting. Should be moved there
    public static final float DISTANCE_TOLERANCE = 0.1f;

    private final Genome genome;

    private final Engine engine = new Engine();

    private final Entity specimen = new Entity();

    private final Kinematics kinematics;

    private final Vector2 center = new Vector2();

    private final Vector2 positionBefore = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);

    private final Vector2 positionAfter = new Vector2();

    private boolean evaluationComplete = false;

    public Arena(Genome genome) {
        this.genome = genome;
        kinematics = new Kinematics(genome);
        specimen.add(kinematics);
        engine.addEntity(specimen);
        setupEngineSystems();
        checkInitialDistanceFromCenter();
    }

    public boolean isEvaluationComplete() {
        return evaluationComplete;
    }

    public void run() {
        while (shouldContinueSimulation()) executeTick();
    }

    public void update() {
        if (!shouldContinueSimulation()) return;
        executeTick();
    }

    public Entity getSpecimen() {
        return specimen;
    }

    private void setupEngineSystems() {
        engine.addSystem(new Movement());
        engine.addSystem(new Friction());
    }

    private boolean shouldContinueSimulation() {
        if (evaluationComplete) return false;
        boolean shouldContinue = positionBefore.dst(positionAfter) > DISTANCE_TOLERANCE;
        if (!shouldContinue) evaluationComplete = true;
        return shouldContinue;
    }

    private void executeTick() {
        positionBefore.set(positionAfter);
        engine.update(0f);
        positionAfter.set(kinematics.getPosition());
        genome.setFitness(center.dst(positionAfter));
    }

    private void checkInitialDistanceFromCenter() {
        float initialDistanceFromCenter = center.dst(genome.getStartPosition());
        if (initialDistanceFromCenter < MIN_INITIAL_DISTANCE) {
            genome.setFitness(Float.MAX_VALUE);
            evaluationComplete = true;
        }
    }
}
