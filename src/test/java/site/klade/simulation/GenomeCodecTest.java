package site.klade.simulation;

import org.junit.jupiter.api.Test;
import site.klade.simulation.gene.Gene;
import site.klade.simulation.gene.GeneAction;
import site.klade.simulation.gene.NumberArg;
import site.klade.simulation.gene.ReferenceArg;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Conformance suite for the DNA reader (plan §2.3).
 *
 * <p>Covers the grammar's input leniency (units, index-less lines, section order), the round-trip through
 * canonical {@code Gene.toString()}, and — most importantly — that a malformed line is reported rather
 * than silently skipped.</p>
 */
class GenomeCodecTest {

    @Test
    void readsEveryActionInTheCanonicalGrammar() {
        String dna = "--- Genes\n"
                + "1.    become rhythm_node period 40\n"
                + "2.    lay_segment 30°\n"
                + "3.    express Morphogen[1] amount 2\n"
                + "4.    if Morphogen[1] > 1.0 become muscle length 40%\n"
                + "5.    if Morphogen[2] > 1.0 become friction_node\n";
        Genome genome = GenomeCodec.parse(dna);

        assertThat(genome.getGenes()).hasSize(5);
        assertThat(genome.getGenes().get(0).getAction()).isEqualTo(GeneAction.BECOME);
        assertThat(genome.getGenes().get(1).getAction()).isEqualTo(GeneAction.LAY_SEGMENT);
        assertThat(genome.getGenes().get(2).getArguments().get(0))
                .isEqualTo(new ReferenceArg(new Index(1)));
        assertThat(genome.getGenes().get(3).getConditions()).isNotNull();
    }

    @Test
    void sectionOrderIsNotSignificant() {
        String morphogensFirst = "--- Morphogens\n"
                + "Morphogen[1]: 0.5, 0.5\n"
                + "--- Genes\n"
                + "1.    express Morphogen[1] amount 1\n"
                + "--- Meta genes\n"
                + "initialAngle: 90\n";
        Genome genome = GenomeCodec.parse(morphogensFirst);

        assertThat(genome.getGenes()).hasSize(1);
        assertThat(genome.getInitialAngle()).isEqualTo(90f);
        assertThat(genome.getMorphogens()).hasSize(1);
    }

    @Test
    void morphogenSectionIsOptional() {
        Genome genome = GenomeCodec.parse("--- Genes\n1.    express Morphogen[1] amount 1\n");

        assertThat(genome.getMorphogens()).hasSize(1);
        assertThat(genome.getMorphogens().get(0).getDiffusionRatio()).isBetween(0f, 1f);
    }

    @Test
    void indexLessLinesReceiveTheSmallestUnusedRootIndex() {
        String dna = "--- Genes\n"
                + "become rhythm_node period 40\n"
                + "lay_segment 30\n"
                + "express Morphogen[1] amount 1\n";
        Genome genome = GenomeCodec.parse(dna);

        assertThat(genome.getGenes()).hasSize(3);
        assertThat(genome.getGenes().get(0).getIndex()).isEqualTo(new Index(1));
        assertThat(genome.getGenes().get(1).getIndex()).isEqualTo(new Index(2));
        assertThat(genome.getGenes().get(2).getIndex()).isEqualTo(new Index(3));
    }

    @Test
    void degreesAreAcceptedInEverySpelling() {
        for (String spelling : new String[]{"30°", "30 degree", "30 degrees", "30 deg", "30"}) {
            Genome genome = GenomeCodec.parse("--- Genes\n1.    lay_segment " + spelling + "\n");
            assertThat(genome.getGenes().get(0).getArguments())
                    .as("spelling %s", spelling)
                    .containsExactly(new NumberArg(30f));
        }
    }

    @Test
    void percentagesAndBareNumbersAreEquivalent() {
        for (String spelling : new String[]{"40%", "40 percent", "40"}) {
            Genome genome = GenomeCodec.parse(
                    "--- Genes\n1.    become muscle length " + spelling + "\n");
            assertThat(genome.getGenes().get(0).getArguments().get(1))
                    .as("spelling %s", spelling)
                    .isEqualTo(new NumberArg(40f));
        }
    }

    @Test
    void commaIsAcceptedAsADecimalSeparator() {
        Genome genome = GenomeCodec.parse("--- Meta genes\ninitialAngle: 45,5\n");

        assertThat(genome.getInitialAngle()).isEqualTo(45.5f);
    }

    @Test
    void emptyGeneIsAnIndexWithNoAction() {
        Genome genome = GenomeCodec.parse("--- Genes\n1.    become muscle length 40%\n2.\n");

        assertThat(genome.getGenes()).hasSize(2);
        assertThat(genome.getGenes().get(1).getAction()).isEqualTo(GeneAction.EMPTY);
        assertThat(genome.getGenes().get(1).toString()).isEqualTo("2.");
    }

    @Test
    void optionalThenIsAcceptedAndNeverEmitted() {
        Genome genome = GenomeCodec.parse(
                "--- Genes\n1.    if Morphogen[1] > 1.0 then become muscle length 40%\n");

        Gene gene = genome.getGenes().get(0);
        assertThat(gene.getAction()).isEqualTo(GeneAction.BECOME);
        assertThat(gene.toString()).doesNotContain("then");
    }

    @Test
    void commentsAreIgnored() {
        String dna = "# leading comment\n"
                + "--- Genes\n"
                + "1.    become muscle length 40%   # trailing comment\n"
                + "# another comment\n";
        assertThat(GenomeCodec.parse(dna).getGenes()).hasSize(1);
    }

    @Test
    void genesComeBackInDottedOrderRegardlessOfInputOrder() {
        String dna = "--- Genes\n"
                + "3.    become friction_node\n"
                + "1.    become rhythm_node period 40\n"
                + "2.    lay_segment 30°\n";
        List<Gene> genes = GenomeCodec.parse(dna).getGenes();

        assertThat(genes.get(0).getIndex()).isEqualTo(new Index(1));
        assertThat(genes.get(1).getIndex()).isEqualTo(new Index(2));
        assertThat(genes.get(2).getIndex()).isEqualTo(new Index(3));
    }

    /**
     * Regression: nested gene indices must be read as indices, not mistaken for index-less lines.
     *
     * <p>The first implementation scanned digits and signs only, so the internal dot of {@code 1.1.}
     * terminated the scan and the token failed the trailing-whitespace check. The line was then silently
     * treated as index-less and renumbered, which is a silent-corruption bug rather than a parse error.</p>
     */
    @Test
    void nestedGeneIndicesAreReadNotRenumbered() {
        String dna = "--- Genes\n"
                + "1.        \n"
                + "1.1.      \n"
                + "1.2.      \n"
                + "4.        become muscle length 40%\n";
        List<Gene> genes = GenomeCodec.parse(dna).getGenes();

        assertThat(genes).hasSize(4);
        assertThat(genes.get(0).getIndex()).isEqualTo(new Index(1));
        assertThat(genes.get(1).getIndex()).isEqualTo(Index.parse("1.1"));
        assertThat(genes.get(2).getIndex()).isEqualTo(Index.parse("1.2"));
        assertThat(genes.get(3).getIndex()).isEqualTo(new Index(4));
    }

    @Test
    void nestedIndicesWithoutTheTrailingDotAreAlsoAccepted() {
        String dna = "--- Genes\n"
                + "2.1    become muscle length 40%\n"
                + "2.2    become friction_node\n";
        List<Gene> genes = GenomeCodec.parse(dna).getGenes();

        assertThat(genes.get(0).getIndex()).isEqualTo(Index.parse("2.1"));
        assertThat(genes.get(1).getIndex()).isEqualTo(Index.parse("2.2"));
    }

    @Test
    void negativeIndexSegmentsAreRead() {
        String dna = "--- Genes\n0.-1.    become muscle length 40%\n";
        assertThat(GenomeCodec.parse(dna).getGenes().get(0).getIndex())
                .isEqualTo(Index.parse("0.-1"));
    }

    /**
     * The index scan must not swallow a leading number that is actually an argument. An index-less line
     * beginning with a bare number would otherwise be misread.
     */
    @Test
    void aTrailingIndexOnAnActionLineStillParses() {
        String dna = "--- Genes\n"
                + "1.    lay_segment 30°\n"
                + "2.    become rhythm_node period 40\n";
        List<Gene> genes = GenomeCodec.parse(dna).getGenes();

        assertThat(genes).hasSize(2);
        assertThat(genes.get(1).getIndex()).isEqualTo(new Index(2));
    }

    @Test
    void geneCanonicalFormRoundTripsThroughTheReader() {
        String dna = "--- Genes\n"
                + "1.    if Morphogen[1] > 1.0 become muscle length 40%\n"
                + "2.    express Morphogen[2] amount 2\n"
                + "3.    lay_segment 30°\n"
                + "4.\n";
        Genome first = GenomeCodec.parse(dna);

        StringBuilder rebuilt = new StringBuilder("--- Genes\n");
        for (Gene gene : first.getGenes()) {
            rebuilt.append(gene.toString()).append('\n');
        }
        Genome second = GenomeCodec.parse(rebuilt.toString());

        assertThat(second.getGenes()).isEqualTo(first.getGenes());
    }

    // ------------------------------------------------------------ error reporting

    @Test
    void unknownMetaGeneIsRejected() {
        assertThatThrownBy(() -> GenomeCodec.parse("--- Meta genes\nbogusGene: 1\n"))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("bogusGene");
    }

    /** The regression that motivated the registry: the canonical spelling must resolve. */
    @Test
    void metaGeneNamesAreCaseInsensitive() {
        assertThat(GenomeCodec.parse("--- Meta genes\nInitialAngle: 45\n").getInitialAngle())
                .isEqualTo(45f);
        assertThat(GenomeCodec.parse("--- Meta genes\ninitialAngle: 45\n").getInitialAngle())
                .isEqualTo(45f);
    }

    @Test
    void unknownActionIsRejected() {
        assertThatThrownBy(() -> GenomeCodec.parse("--- Genes\n1.    teleport 5\n"))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("unknown action");
    }

    @Test
    void unknownElementTypeIsRejected() {
        assertThatThrownBy(() -> GenomeCodec.parse("--- Genes\n1.    become dragon\n"))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("unknown element type");
    }

    @Test
    void missingArgumentIsRejected() {
        assertThatThrownBy(() -> GenomeCodec.parse("--- Genes\n1.    become muscle\n"))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("missing argument");
    }

    @Test
    void extraArgumentIsRejected() {
        assertThatThrownBy(() -> GenomeCodec.parse("--- Genes\n1.    become friction_node 5\n"))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("extra argument");
    }

    @Test
    void contentBeforeASectionMarkerIsRejected() {
        assertThatThrownBy(() -> GenomeCodec.parse("1.    become muscle length 40%\n"))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("before any section marker");
    }

    @Test
    void errorCarriesItsLineNumberAndText() {
        String dna = "--- Genes\n"
                + "1.    become muscle length 40%\n"
                + "2.    nonsense here\n";
        try {
            GenomeCodec.parse(dna);
            org.junit.jupiter.api.Assertions.fail("expected DnaParseException");
        } catch (DnaParseException e) {
            assertThat(e.getLineNumber()).isEqualTo(3);
            assertThat(e.getLineText()).contains("nonsense here");
        }
    }

    @Test
    void nullAndBlankInputAreRejected() {
        assertThatThrownBy(() -> GenomeCodec.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GenomeCodec.parse("   \n  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    /** The pre-work {@code wait} keyword is gone: waiting is an empty gene now. */
    @Test
    void waitKeywordIsNoLongerAnAction() {
        assertThatThrownBy(() -> GenomeCodec.parse("--- Genes\n1.    wait\n"))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("unknown action");
    }

    @Test
    void abbreviatedMorphogenSpellingsAreAccepted() {
        for (String spelling : new String[]{"Morphogen[1]", "Mrph[1]", "Mph[1]"}) {
            Genome genome = GenomeCodec.parse(
                    "--- Genes\n1.    express " + spelling + " amount 1\n");
            assertThat(genome.getGenes().get(0).getArguments().get(0))
                    .as("spelling %s", spelling)
                    .isEqualTo(new ReferenceArg(new Index(1)));
        }
    }

    @Test
    void dottedMorphogenReferencesAreRead() {
        Genome genome = GenomeCodec.parse(
                "--- Genes\n1.    express Morphogen[1.1] amount 1\n");

        assertThat(genome.getGenes().get(0).getArguments().get(0))
                .isEqualTo(new ReferenceArg(Index.parse("1.1")));
    }
}
