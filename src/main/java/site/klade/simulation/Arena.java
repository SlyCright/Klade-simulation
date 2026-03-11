package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

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
                        .add(new Kinematics(genome))
                        .add(new ToleranceComponent())
                        .add(new FitnessComponent())));
        specimens.forEach(engine::addEntity);
        engine.addSystem(new Friction()); // 10
        engine.addSystem(new Movement()); // 20
        engine.addSystem(new ToleranceCalculation()); // 30
        engine.addSystem(new FitnessCalculation()); // 100
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

    private boolean shouldContinueSimulation() {
        if (evaluationComplete) return false;
        Family family = Family.all(ToleranceComponent.class).get();
        ImmutableArray<Entity> entities = engine.getEntitiesFor(family);
        boolean allReachedTolerance = true;
        for (Entity entity : entities) {
            if (entity.getComponent(ToleranceComponent.class).isToleranceReached()) continue;
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
                FitnessComponent.class,
                ToleranceComponent.class
        ).get();
        ImmutableArray<Entity> entities = engine.getEntitiesFor(family);
        for (Entity entity : entities) {
            var kinematics = entity.getComponent(Kinematics.class);
            float initialDistanceFromCenter = kinematics.getPosition().dst(0f, 0f);
            if (initialDistanceFromCenter < MIN_INITIAL_DISTANCE) {
                entity.getComponent(ToleranceComponent.class).setToleranceReached(true);
                entity.getComponent(FitnessComponent.class).set(Float.MAX_VALUE);
            }
        }
    }
}
