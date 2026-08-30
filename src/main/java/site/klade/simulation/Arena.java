package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import site.klade.simulation.components.*;
import site.klade.simulation.systems.*;

import java.util.ArrayList;

public class Arena {

    private final Engine engine = new Engine();
    private final IdEngine idEngine = new IdEngine();

    private boolean evaluationComplete = false;

    public Arena(ArrayList<Genome> genomes) {
        for (Genome genome : genomes) {
            Entity specimenEntity = createSpecimenEntity(genome);
            EntityId specimenId = specimenEntity.getComponent(EntityId.class);
            Entity stemNode = createStemNodeEntity(specimenId.id);
            engine.addEntity(specimenEntity);
            engine.addEntity(stemNode);
        }
        addSystems();
        checkInitialDistanceFromCenter();
    }

    private Entity createSpecimenEntity(Genome genome) {
        Entity specimenEntity = engine.createEntity();
        idEngine.registerEntity(specimenEntity);
        specimenEntity.add(new GenomeWrap(genome));

        EntityId specimenId = new EntityId();
        specimenId.id = idEngine.getIdByEntity(specimenEntity);
        specimenEntity.add(specimenId);
        specimenEntity.add(new Specimen());

        return specimenEntity;
    }

    private Entity createStemNodeEntity(int specimenId) {
        Entity stemNode = engine.createEntity();
        idEngine.registerEntity(stemNode);

        EntityId stemNodeId = new EntityId();
        stemNodeId.id = idEngine.getIdByEntity(stemNode);
        stemNode.add(stemNodeId);

        Node nodeComponent = new Node();
        nodeComponent.specimenId = specimenId;
        stemNode.add(nodeComponent);
        stemNode.add(new Kinematics());

        return stemNode;
    }

    private void addSystems() {
        engine.addSystem(new ToleranceExclusion(10));
        engine.addSystem(new Friction(20));
        engine.addSystem(new Collision(30));
        engine.addSystem(new Movement(40));
        engine.addSystem(new ToleranceCalculation(50));
        engine.addSystem(new FitnessCalculation(100));
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
                GenomeWrap.class,
                ToleranceStatus.class
        ).get();
        ImmutableArray<Entity> entities = engine.getEntitiesFor(family);
//        for (Entity entity : entities) {
//            var kinematics = entity.getComponent(Kinematics.class);
//            float initialDistanceFromCenter = kinematics.getPosition().dst(0f, 0f);
//            if (initialDistanceFromCenter < MIN_INITIAL_DISTANCE) {
//                entity.getComponent(ToleranceStatus.class).setToleranceReached(true);
//                entity.getComponent(GenomeWrap.class).setFitness(Float.MAX_VALUE);
//            }
//        }
    }
}
