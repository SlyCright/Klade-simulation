package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.klade.simulation.components.Kinematics;
import site.klade.simulation.components.ToleranceStatus;
import site.klade.simulation.systems.Movement;

import static org.assertj.core.api.Assertions.assertThat;

public class MovementTest {

    @Test
    @DisplayName("Given multiple entities, when update is called, then all entities are moved accordingly")
    void givenMultipleEntities_whenUpdateIsCalled_thenAllEntitiesAreMovedAccordingly() {
        // Given
        Engine engine = new Engine();
        Movement movement = new Movement(20);
        engine.addSystem(movement);
        // Entity 1
        Entity entity1 = new Entity();
        Genome genome1 = new Genome();
        Kinematics kinematics1 = new Kinematics();
        kinematics1.getVelocity().set(1f, 0f);
        kinematics1.getPosition().set(0f, 0f);
        kinematics1.getAcceleration().set(1f, 0f);
        entity1.add(kinematics1);
        entity1.add(new ToleranceStatus());
        engine.addEntity(entity1);
        // Entity 2
        Entity entity2 = new Entity();
        Genome genome2 = new Genome();
        Kinematics kinematics2 = new Kinematics();
        kinematics2.getVelocity().set(0f, 1f);
        kinematics2.getPosition().set(5f, 5f);
        kinematics2.getAcceleration().set(0f, 1f);
        entity2.add(kinematics2);
        entity2.add(new ToleranceStatus());
        engine.addEntity(entity2);
        // When
        engine.update(1f);
        // Then
        // Assert Entity 1/
        assertThat(kinematics1.getVelocity()).isEqualTo(new Vector2(2f, 0f));
        assertThat(kinematics1.getPosition()).isEqualTo(new Vector2(2f, 0f));
        assertThat(kinematics1.getAcceleration()).isEqualTo(new Vector2(0f, 0f));
        // Assert Entity 2
        assertThat(kinematics2.getVelocity()).isEqualTo(new Vector2(0f, 2f));
        assertThat(kinematics2.getPosition()).isEqualTo(new Vector2(5f, 7f));
        assertThat(kinematics2.getAcceleration()).isEqualTo(new Vector2(0f, 0f));
    }
}
