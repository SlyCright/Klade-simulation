package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import site.klade.simulation.components.*;
import site.klade.simulation.systems.Collision;
import site.klade.simulation.systems.FitnessCalculation;
import site.klade.simulation.systems.Friction;
import site.klade.simulation.systems.Movement;

import java.util.ArrayList;

public class Arena {

    private final Engine engine = new Engine();

    private final ArenaSettings settings;

    private final TickCounter tickCounter;

    public Arena(ArrayList<Genome> genomes, ArenaSettings settings) {
        this.settings = settings;
        this.tickCounter = new TickCounter(settings.getMaxTicks());
        addEntities(genomes);
        addSystems();
    }

    private void addEntities(ArrayList<Genome> genomes) {
        for (Genome genome : genomes) {
            createSpecimen(genome);
        }
    }

    private void createSpecimen(Genome genome) {
        Entity specimenEntity = engine.createEntity();
        specimenEntity.add(new GenomeWrap(genome));
        Entity stemNode = createStemNode(genome.getInitialAngle());
        Specimen specimen = new Specimen();
        specimen.nodes.add(stemNode);
        specimenEntity.add(specimen);
        engine.addEntity(specimenEntity);
        engine.addEntity(stemNode);
    }

    private Entity createStemNode(float angleDegrees) {
        Entity stemNode = engine.createEntity();
        Node node = new Node();
        node.nodeType = NodeType.STEM;
        stemNode.add(node);
        Kinematics kinematics = new Kinematics();
        float distance = settings.getInitialDistance();
        float angleRadians = (float) Math.toRadians(angleDegrees);
        kinematics.position.set(
                distance * (float) Math.cos(angleRadians),
                distance * (float) Math.sin(angleRadians));
        stemNode.add(kinematics);
        return stemNode;
    }

    private void addSystems() {
        PriorityGenerator priority = new PriorityGenerator();
        engine.addSystem(new Friction(priority.next(), settings.getFrictionFactor()));
        engine.addSystem(new Collision(priority.next(), settings.getNodeSize(), settings.getRepulsionFactor()));
        engine.addSystem(new Movement(priority.next()));
        engine.addSystem(new FitnessCalculation(priority.next()));
    }

    // that's for background simulation in Web-app
    public void run() {
        while (tickCounter.shouldContinue()) executeTick();
    }

    // that's for visualization in Stage
    public void update() {
        if (tickCounter.shouldContinue()) executeTick();
    }

    public boolean isDone() {
        return tickCounter.isDone();
    }

    public int getCurrentTick() {
        return tickCounter.getCurrentTick();
    }

    private void executeTick() {
        engine.update(0f);
        tickCounter.increment();
    }

    public ImmutableArray<Entity> getEntitiesFor(Family family) {
        return engine.getEntitiesFor(family);
    }

}
