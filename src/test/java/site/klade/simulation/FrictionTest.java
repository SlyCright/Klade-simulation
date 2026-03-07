package site.klade.simulation;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FrictionTest {

    @Test
    void givenAnEntityWithSpecimenKinematics_whenUpdateIsCalled_thenFrictionIsAppliedToAcceleration() {
        // Given
        Engine engine = new Engine();
        Friction friction = new Friction();
        engine.addSystem(friction);
        Entity entity = new Entity();
        Genome genome = new Genome();
        Kinematics kinematics = new Kinematics(genome);
        kinematics.getVelocity().set(10f, 20f);
        kinematics.getAcceleration().set(1f, 2f);
        entity.add(kinematics);
        engine.addEntity(entity);
        // When
        engine.update(1f);
        // Then
        Vector2 expectedFriction =
                new Vector2(kinematics.getVelocity()).scl(-Friction.FRICTION_FACTOR);
        Vector2 expectedAcceleration = new Vector2(1f, 2f).add(expectedFriction);
        assertThat(kinematics.getAcceleration()).isEqualTo(expectedAcceleration);
    }
}
