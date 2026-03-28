package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import site.klade.simulation.components.Fitness;
import site.klade.simulation.components.Kinematics;
import site.klade.simulation.components.ToleranceStatus;
import site.klade.simulation.systems.*;

import java.util.ArrayList;
import java.util.List;

import static site.klade.simulation.Genome.MIN_INITIAL_DISTANCE;

public class Arena {

    private final Engine engine = new Engine();

    private final ArrayList<Entity> specimens = new ArrayList<>();

    private boolean evaluationComplete = false;

    public Arena(Genome genome) {
        this(new ArrayList<>(List.of(genome)));
    }

    public Arena(ArrayList<Genome> genomes) {
        genomes.forEach(genome ->
                specimens.add(new Entity()
                        .add(new Kinematics(
                                genome.getInitialImpulse(),
                                genome.getStartPosition()))
                        .add(new ToleranceStatus())
                        .add(new Fitness())));
        specimens.forEach(engine::addEntity);
        engine.addSystem(new ToleranceExclusion());     // 5
        engine.addSystem(new Friction());               // 10
        engine.addSystem(new ElasticCollision());       // 15
        engine.addSystem(new Movement());               // 20
        engine.addSystem(new ToleranceCalculation());   // 30
        engine.addSystem(new FitnessCalculation());     // 100
        checkInitialDistanceFromCenter();
    }

    public ArrayList<Entity> getSpecimens() {
        return specimens;
    }

    public boolean isEvaluationComplete() {
        return evaluationComplete;
    }

    // that's for background simulation in Web-app
    public void run() {
        while (shouldContinueSimulation()) executeTick();
    }

    // that's for visualization in Stage
    public void update() {
        if (!shouldContinueSimulation()) return;
        executeTick();
    }

    private boolean shouldContinueSimulation() {
        if (evaluationComplete) return false;
        Family family = Family.all(ToleranceStatus.class).get();
        ImmutableArray<Entity> entities = engine.getEntitiesFor(family);
        boolean allReachedTolerance = true;
        for (Entity entity : entities) {
            if (entity.getComponent(ToleranceStatus.class).isToleranceReached()) continue;
            allReachedTolerance = false;
            break;
        }
        if (allReachedTolerance) evaluationComplete = true;
        return !allReachedTolerance;
    }

    private void executeTick() {
        engine.update(0f);
    }

    private void checkInitialDistanceFromCenter() {
        Family family = Family.all(
                Kinematics.class,
                Fitness.class,
                ToleranceStatus.class
        ).get();
        ImmutableArray<Entity> entities = engine.getEntitiesFor(family);
        for (Entity entity : entities) {
            var kinematics = entity.getComponent(Kinematics.class);
            float initialDistanceFromCenter = kinematics.getPosition().dst(0f, 0f);
            if (initialDistanceFromCenter < MIN_INITIAL_DISTANCE) {
                entity.getComponent(ToleranceStatus.class).setToleranceReached(true);
                entity.getComponent(Fitness.class).set(Float.MAX_VALUE);
            }
        }
    }
}
