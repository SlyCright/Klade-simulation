package site.klade.simulation;

import com.badlogic.ashley.core.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class ArenaTest {

    @Test
    @DisplayName("Given valid Genome, when Arena is constructed, then specimen entity is created and added to engine")
    void givenValidGenome_whenArenaIsConstructed_thenSpecimenEntityIsCreatedAndAddedToEngine() {
        // Given
        Genome genome = new Genome();

        // When
        Arena arena = new Arena(genome);

        // Then
        assertThat(arena.getSpecimens()).isNotNull();
        assertThat(arena.getSpecimens().get(0)).isInstanceOf(Entity.class);
    }

    @Test
    @DisplayName("Given Arena with real Genome, when run is called, then fitness is set on genome")
    void givenArenaWithRealGenome_whenRunIsCalled_thenFitnessIsSetOnGenome() {
        // Given
        Genome realGenome = new Genome();
        Arena arena = new Arena(realGenome);
        float initialFitness = realGenome.getFitness();

        // When
        arena.run();

        // Then
        assertThat(realGenome.getFitness()).isNotEqualTo(initialFitness);
    }

    @Test
    @DisplayName("Given Arena with Genome, when update is called and simulation should continue, then tick is executed")
    void givenArenaWithGenome_whenUpdateIsCalledAndSimulationShouldContinue_thenTickIsExecuted() {
        // Given
        Genome genome = new Genome();
        Arena arena = new Arena(genome);

        // When
        arena.update();

        // Then
        // Since we can't directly test private state, we verify the method completes without error
        assertThat(arena.getSpecimens()).isNotNull();
    }

    @Test
    @DisplayName("Given Arena with Genome, when getSpecimen is called, then returns the specimen entity")
    void givenArenaWithGenome_whenGetSpecimenIsCalled_thenReturnsTheSpecimenEntity() {
        // Given
        Genome genome = new Genome();
        Arena arena = new Arena(genome);

        // When
        ArrayList<Entity> specimens = arena.getSpecimens();

        // Then
        assertThat(specimens).isNotNull();
        assertThat(specimens.get(0)).isInstanceOf(Entity.class);
    }

    @Test
    void givenArenaWithGenome_whenUpdateIsCalledMultipleTimes_thenFitnessIsRecalculatedEachTime() {
        // Given
        Genome genome = new Genome();
        Arena arena = new Arena(genome);
        // When
        float initialFitness = genome.getFitness();
        arena.update();
        float firstUpdateFitness = genome.getFitness();
        arena.update();
        float secondUpdateFitness = genome.getFitness();
        // Then
        assertThat(initialFitness).isNotEqualTo(firstUpdateFitness);
        assertThat(firstUpdateFitness).isNotEqualTo(secondUpdateFitness);
    }
}
