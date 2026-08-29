package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;
import site.klade.simulation.components.Kinematics;
import site.klade.simulation.components.ToleranceStatus;
import site.klade.simulation.systems.Collision;
import site.klade.simulation.systems.Movement;

import static org.assertj.core.api.Assertions.assertThat;

public class CollisionTest {

    @Test
    void givenTwoEntitiesColliding_whenUpdateIsCalled_thenVelocitiesChange() {
        // Given
        Engine engine = new Engine();
        Collision collision = new Collision();
        engine.addSystem(collision);
        engine.addSystem(new Movement());

        Entity entity1 = new Entity();
        Kinematics kinematics1 = new Kinematics();
        kinematics1.getPosition().set(0f, 0f);
        kinematics1.getVelocity().set(5f, 0f);
        entity1.add(kinematics1);
        entity1.add(new ToleranceStatus());
        engine.addEntity(entity1);

        Entity entity2 = new Entity();
        Kinematics kinematics2 = new Kinematics();
        kinematics2.getPosition().set(10f, 0f);
        kinematics2.getVelocity().set(-5f, 0f);
        entity2.add(kinematics2);
        entity2.add(new ToleranceStatus());
        engine.addEntity(entity2);

        // When
        for (int i = 0; i < 100; i++) {
            engine.update(1f);
        }

        // Then
        assertThat(kinematics1.getVelocity()).isNotEqualTo(new Vector2(5f, 0f));
        assertThat(kinematics2.getVelocity()).isNotEqualTo(new Vector2(-5f, 0f));
    }

    @Test
    void givenTwoEntitiesOverlapped_whenUpdateIsCalled_thenTheyRepel() {
        // Given
        Engine engine = new Engine();
        Collision collision = new Collision();
        engine.addSystem(collision);
        engine.addSystem(new Movement());

        Entity entity1 = new Entity();
        Kinematics kinematics1 = new Kinematics();
        kinematics1.getPosition().set(0f, 0f);
        kinematics1.getVelocity().set(0f, 0f);
        entity1.add(kinematics1);
        entity1.add(new ToleranceStatus());
        engine.addEntity(entity1);

        Entity entity2 = new Entity();
        Kinematics kinematics2 = new Kinematics();
        kinematics2.getPosition().set(10f, 0f);
        kinematics2.getVelocity().set(-1f, 0f);
        entity2.add(kinematics2);
        entity2.add(new ToleranceStatus());
        engine.addEntity(entity2);

        // When
        for (int i = 0; i < 20; i++) {
            engine.update(1f);
            System.out.println("Iteration " + i + ":");
            System.out.println("  Entity1 - Position: " + kinematics1.getPosition() + ", Velocity: " + kinematics1.getVelocity());
            System.out.println("  Entity2 - Position: " + kinematics2.getPosition() + ", Velocity: " + kinematics2.getVelocity());
        }

        // Then
        assertThat(kinematics1.getVelocity()).isNotEqualTo(new Vector2(0f, 0f));
        assertThat(kinematics2.getVelocity()).isNotEqualTo(new Vector2(0f, 0f));
        assertThat(kinematics1.getVelocity().x).isNegative();
        assertThat(kinematics2.getVelocity().x).isPositive();
    }
}
