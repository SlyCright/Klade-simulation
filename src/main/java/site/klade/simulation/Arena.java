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
    private final ArenaSettingsDto settings;

    private boolean evaluationComplete = false;

    public Arena(ArrayList<Genome> genomes, ArenaSettingsDto settings) {
        this.settings = settings;
        addEntities(genomes);
        addSystems();
    }

    private void addEntities(ArrayList<Genome> genomes) {
        for (Genome genome : genomes) {
            Entity specimenEntity = createSpecimenEntity(genome);
            EntityId specimenId = specimenEntity.getComponent(EntityId.class);
            Entity stemNode = createStemNodeEntity(
                    specimenId.id, genome.getMetaGenes().getInitialPositionAngleDegrees());
            engine.addEntity(specimenEntity);
            engine.addEntity(stemNode);
        }
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

    private Entity createStemNodeEntity(int specimenId, float initialPositionAngleDegrees) {

        // Entity creation and registration
        Entity stemNode = engine.createEntity();
        idEngine.registerEntity(stemNode);

        // EntityId component
        EntityId stemNodeId = new EntityId();
        stemNodeId.id = idEngine.getIdByEntity(stemNode);
        stemNode.add(stemNodeId);

        // Node component
        Node nodeComponent = new Node();
        nodeComponent.specimenId = specimenId;
        nodeComponent.nodeType = NodeType.STEM;
        stemNode.add(nodeComponent);

        // Kinematics component with initial position calculation
        Kinematics kinematics = new Kinematics();
        float initialDistance = this.settings.getInitialDistance();
        float initialAngleRadians = (float) Math.toRadians(initialPositionAngleDegrees);
        float initialX = initialDistance * (float) Math.cos(initialAngleRadians);
        float initialY = initialDistance * (float) Math.sin(initialAngleRadians);
        kinematics.position.set(initialX, initialY);
        stemNode.add(kinematics);

        return stemNode;
    }

    private void addSystems() {
        PriorityGenerator priority = new PriorityGenerator();
        engine.addSystem(new Friction(priority.next(), settings.getFrictionFactor()));
        engine.addSystem(new Collision(priority.next(), settings.getSpecimenSize(), settings.getRepulsionForceMultiplier()));
        engine.addSystem(new Movement(priority.next()));
        engine.addSystem(new ToleranceCalculation(priority.next()));
        engine.addSystem(new FitnessCalculation(priority.next()));
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

}
