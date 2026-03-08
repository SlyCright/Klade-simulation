package site.klade.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static site.klade.simulation.Genome.START_POSITION_MUTATION_RATE;

class GenomeTest {

    @Test
    @DisplayName("Given a parent genome, when getting offspring, then offspring has mutated start position and initial impulse")
    void givenParentGenome_whenGettingOffspring_thenOffspringHasMutatedStartPositionAndInitialImpulse() {
        for (int i = 0; i < 100; i++) {
            // Given: a parent genome with initial values
            Genome parent = new Genome();
            // When: getting offspring
            Genome offspring = Genome.getMutatedAndFitnessMaxedCopyOf(parent);
            // Then: offspring is a new instance with fitness 0 and mutated positions within mutation radius
            assertThat(offspring).isNotSameAs(parent);
            assertThat(offspring.getFitness()).isEqualTo(Float.MAX_VALUE);
            float mutationRadius = START_POSITION_MUTATION_RATE / 2f;
            assertThat(offspring.getStartPosition().x)
                    .isBetween(
                            parent.getStartPosition().x - mutationRadius,
                            parent.getStartPosition().x + mutationRadius);
            assertThat(offspring.getStartPosition().y)
                    .isBetween(
                            parent.getStartPosition().y - mutationRadius,
                            parent.getStartPosition().y + mutationRadius);
            float impulseRate = Genome.INITIAL_IMPULSE_RANGE / 2f;
            assertThat(offspring.getInitialImpulse().x)
                    .isBetween(
                            parent.getInitialImpulse().x - impulseRate,
                            parent.getInitialImpulse().x + impulseRate);
            assertThat(offspring.getInitialImpulse().y)
                    .isBetween(
                            parent.getInitialImpulse().y - impulseRate,
                            parent.getInitialImpulse().y + impulseRate);
        }
    }

    @Test
    @DisplayName("Given no input, when creating genome, then genome has start position within initial distance bounds and initial impulse within mutation rate bounds and fitness Float.MAX_VALUE")
    void givenNoInput_whenCreatingGenome_thenGenomeHasStartPositionWithinInitialDistanceBoundsAndInitialImpulseWithinMutationRateBoundsAndFitnessFloatMaxValue() {
        for (int i = 0; i < 100; i++) {
            // Given: no input
            // When: creating genome
            Genome genome = new Genome();
            // Then: start position is within the circular area defined by min/max distance
            float distance = genome.getStartPosition().len();
            assertThat(distance)
                    .isBetween(Genome.MIN_INITIAL_DISTANCE, Genome.MAX_INITIAL_DISTANCE);
            float initialImpulseMutationRate = Genome.INITIAL_IMPULSE_RANGE / 2f;
            assertThat(genome.getInitialImpulse().x).
                    isBetween(-initialImpulseMutationRate, initialImpulseMutationRate);
            assertThat(genome.getInitialImpulse().y).
                    isBetween(-initialImpulseMutationRate, initialImpulseMutationRate);
            // Then: fitness is Float.MAX_VALUE
            assertThat(genome.getFitness()).isEqualTo(Float.MAX_VALUE);
        }
    }
}
