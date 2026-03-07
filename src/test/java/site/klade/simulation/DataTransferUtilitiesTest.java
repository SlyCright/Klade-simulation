package site.klade.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;

public class DataTransferUtilitiesTest {

    @Test
    @DisplayName("Given empty list, when getDeepCopyOf, then return empty list")
    public void givenEmptyList_whenGetDeepCopyOf_thenReturnEmptyList() {
        // Given
        ArrayList<Species> emptyList = new ArrayList<>();

        // When
        ArrayList<Species> result = DataTransferUtilities.getDeepCopyOf(emptyList);

        // Then
        Assertions.assertThat(result).isNotSameAs(emptyList);
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given list with one species with no genomes, when getDeepCopyOf, then return list with one new species with no genomes")
    public void givenListWithOneSpeciesWithNoGenomes_whenGetDeepCopyOf_thenReturnListWithOneNewSpeciesWithNoGenomes() {
        // Given
        ArrayList<Genome> genomes = new ArrayList<>();
        Species species = new Species(genomes);
        ArrayList<Species> speciesList = new ArrayList<>();
        speciesList.add(species);

        // When
        ArrayList<Species> result = DataTransferUtilities.getDeepCopyOf(speciesList);

        // Then
        Assertions.assertThat(result).isNotSameAs(speciesList);
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0)).isNotSameAs(species);
        Assertions.assertThat(result.get(0).getGenomes()).isNotSameAs(genomes);
        Assertions.assertThat(result.get(0).getGenomes()).isEmpty();
    }

    @Test
    @DisplayName("Given list with one species with one genome, when getDeepCopyOf, then return list with one new species with one new genome")
    public void givenListWithOneSpeciesWithOneGenome_whenGetDeepCopyOf_thenReturnListWithOneNewSpeciesWithOneNewGenome() {
        // Given
        ArrayList<Genome> genomes = new ArrayList<>();
        Genome genome = new Genome();
        genomes.add(genome);
        Species species = new Species(genomes);
        ArrayList<Species> speciesList = new ArrayList<>();
        speciesList.add(species);

        // When
        ArrayList<Species> result = DataTransferUtilities.getDeepCopyOf(speciesList);

        // Then
        Assertions.assertThat(result).isNotSameAs(speciesList);
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0)).isNotSameAs(species);
        Assertions.assertThat(result.get(0).getGenomes()).isNotSameAs(genomes);
        Assertions.assertThat(result.get(0).getGenomes()).hasSize(1);
        Assertions.assertThat(result.get(0).getGenomes().get(0)).isNotSameAs(genome);
    }

    @Test
    @DisplayName("Given list with multiple species, when getDeepCopyOf, then return list with same number of new species with new genomes")
    public void givenListWithMultipleSpecies_whenGetDeepCopyOf_thenReturnListWithSameNumberOfNewSpeciesWithNewGenomes() {
        // Given
        Species species1 = new Species(2); // has 2 genomes
        Species species2 = new Species(1); // has 1 genome
        ArrayList<Species> speciesList = new ArrayList<>();
        speciesList.add(species1);
        speciesList.add(species2);

        // When
        ArrayList<Species> result = DataTransferUtilities.getDeepCopyOf(speciesList);

        // Then
        Assertions.assertThat(result).isNotSameAs(speciesList);
        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.get(0)).isNotSameAs(species1);
        Assertions.assertThat(result.get(0).getGenomes()).isNotSameAs(species1.getGenomes());
        Assertions.assertThat(result.get(0).getGenomes()).hasSize(2);
        Assertions.assertThat(result.get(0).getGenomes().get(0)).isNotSameAs(species1.getGenomes().get(0));
        Assertions.assertThat(result.get(0).getGenomes().get(1)).isNotSameAs(species1.getGenomes().get(1));
        Assertions.assertThat(result.get(1)).isNotSameAs(species2);
        Assertions.assertThat(result.get(1).getGenomes()).isNotSameAs(species2.getGenomes());
        Assertions.assertThat(result.get(1).getGenomes()).hasSize(1);
        Assertions.assertThat(result.get(1).getGenomes().get(0)).isNotSameAs(species2.getGenomes().get(0));
    }
}
