package site.klade.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArenaTest {

    @Test
    @DisplayName("Given an arena with genome, when run is called multiple times, then fitness is recalculated each time")
    void givenArenaWithGenome_whenRunIsCalledMultipleTimes_thenFitnessIsRecalculatedEachTime() {
        // Given
        Genome genome = new Genome();
        Arena arena = new Arena(genome);
        // When
        float initialFitness = genome.getFitness();
        arena.run();
        float firstRunFitness = genome.getFitness();
        arena.run();
        float secondRunFitness = genome.getFitness();
        // Then
        assertThat(initialFitness).isNotEqualTo(firstRunFitness);
        assertThat(firstRunFitness).isNotEqualTo(secondRunFitness);
    }
}
