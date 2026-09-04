package site.klade.simulation.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import site.klade.simulation.components.GenomeWrap;
import site.klade.simulation.components.Kinematics;
import site.klade.simulation.components.Specimen;

public class FitnessCalculation extends EntitySystem {

    private final Family specimenFamily = Family.all(GenomeWrap.class, Specimen.class).get();

    private final Vector2 avgPosition = new Vector2();

    public FitnessCalculation(int priority) {
        super(priority);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> specimens = getEngine().getEntitiesFor(specimenFamily);
        for (Entity specimen : specimens) {
            GenomeWrap genomeWrap = specimen.getComponent(GenomeWrap.class);
            Specimen specimenComponent = specimen.getComponent(Specimen.class);
            avgPosition.setZero();
            for (Entity node : specimenComponent.nodes) {
                Kinematics kinematics = node.getComponent(Kinematics.class);
                avgPosition.add(kinematics.position);
            }
            avgPosition.scl(1f / specimenComponent.nodes.size);
            genomeWrap.genome.setFitness(avgPosition.len());
        }
    }

}
