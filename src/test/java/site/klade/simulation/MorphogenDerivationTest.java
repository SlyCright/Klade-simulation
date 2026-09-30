package site.klade.simulation;

import org.junit.jupiter.api.Test;
import site.klade.simulation.gene.Gene;
import site.klade.simulation.gene.GeneAction;
import site.klade.simulation.gene.NumberArg;
import site.klade.simulation.gene.ReferenceArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Conformance suite for morphogen derivation (plan §2.2).
 *
 * <p>Pins the load-bearing property: <b>inheritance is decided by dotted position alone</b>, never by
 * recorded provenance, so a genome that no mutator ever touched — one assembled by crossover —
 * materialises its definitions correctly.</p>
 */
class MorphogenDerivationTest {

    private static Genome genomeWithGenes(Gene... genes) {
        Genome genome = new Genome(new MetaGenes(), new ArrayList<Morphogen>(),
                new ArrayList<Gene>(Arrays.asList(genes)));
        return genome;
    }

    private static Gene express(Index geneIndex, Index morphogen) {
        return new Gene(geneIndex, null, GeneAction.EXPRESS,
                Arrays.<site.klade.simulation.gene.GeneArg>asList(
                        new ReferenceArg(morphogen), new NumberArg(1f)));
    }

    private static Morphogen defined(String id, float diffusion, float decay) {
        return new Morphogen(Index.parse(id), diffusion, decay);
    }

    @Test
    void referencedButUndefinedMorphogenIsCreated() {
        Genome genome = genomeWithGenes(express(new Index(1), new Index(1)));
        genome.deriveMorphogens();

        assertThat(genome.getMorphogens()).hasSize(1);
        assertThat(genome.getMorphogens().get(0).getId()).isEqualTo(new Index(1));
    }

    @Test
    void unreferencedMorphogenIsRemoved() {
        Genome genome = genomeWithGenes(express(new Index(1), new Index(1)));
        genome.getMorphogens().add(defined("9", 0.5f, 0.5f));
        genome.deriveMorphogens();

        assertThat(genome.getMorphogens()).hasSize(1);
        assertThat(genome.getMorphogens().get(0).getId()).isEqualTo(new Index(1));
    }

    @Test
    void newMorphogenInheritsFromItsPredecessor() {
        // 1 is defined; 1.0 is new and sorts immediately after it — the duplication case.
        Genome genome = genomeWithGenes(
                express(new Index(1), new Index(1)),
                express(new Index(2), Index.parse("1.0")));
        genome.getMorphogens().add(defined("1", 0.42f, 0.17f));
        genome.deriveMorphogens();

        Morphogen copy = genome.findMorphogen(Index.parse("1.0"));
        assertThat(copy).isNotNull();
        assertThat(copy.getDiffusionRatio()).isEqualTo(0.42f);
        assertThat(copy.getDecayRatio()).isEqualTo(0.17f);
    }

    @Test
    void newMorphogenAppendedAfterTheLastInheritsFromTheLast() {
        Genome genome = genomeWithGenes(
                express(new Index(1), new Index(3)),
                express(new Index(2), new Index(4)));
        genome.getMorphogens().add(defined("3", 0.6f, 0.2f));
        genome.deriveMorphogens();

        Morphogen copy = genome.findMorphogen(new Index(4));
        assertThat(copy.getDiffusionRatio()).isEqualTo(0.6f);
        assertThat(copy.getDecayRatio()).isEqualTo(0.2f);
    }

    @Test
    void newMorphogenBeforeAllInheritsFromItsSuccessor() {
        Genome genome = genomeWithGenes(
                express(new Index(1), new Index(0)),
                express(new Index(2), new Index(1)));
        genome.getMorphogens().add(defined("1", 0.9f, 0.3f));
        genome.deriveMorphogens();

        Morphogen copy = genome.findMorphogen(new Index(0));
        // No predecessor exists, so the successor is the source.
        assertThat(copy.getDiffusionRatio()).isEqualTo(0.9f);
        assertThat(copy.getDecayRatio()).isEqualTo(0.3f);
    }

    @Test
    void newMorphogenWithNoSourceIsRandomisedWithinTheLegalDomain() {
        Genome genome = genomeWithGenes(express(new Index(1), new Index(1)));
        genome.deriveMorphogens();

        Morphogen created = genome.getMorphogens().get(0);
        assertThat(created.getDiffusionRatio()).isBetween(0f, 1f);
        assertThat(created.getDecayRatio()).isBetween(0f, 1f);
    }

    @Test
    void referencesInsideConditionsAreCollectedToo() {
        site.klade.simulation.condition.Cond condition =
                site.klade.simulation.condition.ConditionParser.parse("Morphogen[7] > 1.0");
        Genome genome = genomeWithGenes(
                new Gene(new Index(1), condition, GeneAction.EMPTY,
                        Collections.<site.klade.simulation.gene.GeneArg>emptyList()));
        genome.deriveMorphogens();

        assertThat(genome.getMorphogens()).hasSize(1);
        assertThat(genome.getMorphogens().get(0).getId()).isEqualTo(new Index(7));
    }

    @Test
    void derivationIsIdempotentAndStable() {
        Genome genome = genomeWithGenes(
                express(new Index(1), new Index(1)),
                express(new Index(2), Index.parse("2.1")));
        genome.getMorphogens().add(defined("1", 0.31f, 0.11f));
        genome.deriveMorphogens();
        List<Morphogen> first = new ArrayList<Morphogen>(genome.getMorphogens());

        genome.deriveMorphogens();
        List<Morphogen> second = new ArrayList<Morphogen>(genome.getMorphogens());

        assertThat(second).isEqualTo(first);
        // The inherited copy must not be re-randomised on the second pass.
        Morphogen copy = genome.findMorphogen(Index.parse("2.1"));
        assertThat(copy.getDiffusionRatio()).isEqualTo(0.31f);
    }

    @Test
    void morphogensAreKeptInDottedOrder() {
        Genome genome = genomeWithGenes(
                express(new Index(1), new Index(2)),
                express(new Index(2), Index.parse("1.1")),
                express(new Index(3), new Index(1)));
        genome.deriveMorphogens();

        List<Index> ids = new ArrayList<Index>();
        for (Morphogen morphogen : genome.getMorphogens()) {
            ids.add(morphogen.getId());
        }
        // Dotted order is prefix-first: `1.` precedes `1.1.`, which precedes `2.`.
        assertThat(ids).containsExactly(new Index(1), Index.parse("1.1"), new Index(2));
        // Sorted ascending.
        for (int i = 1; i < ids.size(); i++) {
            assertThat(ids.get(i - 1).compareTo(ids.get(i))).isLessThan(0);
        }
    }

    @Test
    void sourceForPrefersPredecessorOverSuccessor() {
        List<Morphogen> defined = Arrays.asList(defined("1", 0.1f, 0.1f), defined("5", 0.5f, 0.5f));
        Morphogen source = Genome.sourceFor(new Index(3), defined);

        assertThat(source.getId()).isEqualTo(new Index(1));
    }

    @Test
    void sourceForFallsBackToSuccessorWhenThereIsNoPredecessor() {
        List<Morphogen> defined = Arrays.asList(defined("5", 0.5f, 0.5f));
        Morphogen source = Genome.sourceFor(new Index(3), defined);

        assertThat(source.getId()).isEqualTo(new Index(5));
    }

    @Test
    void sourceForReturnsNullWhenNothingIsDefined() {
        assertThat(Genome.sourceFor(new Index(1), new ArrayList<Morphogen>())).isNull();
    }

    // ------------------------------------------------- canonical spelling of the pieces

    /**
     * Each part of the genome owns the canonical spelling of its own line, so the writer only assembles
     * sections. These pin that contract; without it the format would be re-derived per consumer, which is
     * how a separator once went missing.
     */
    @Test
    void morphogenToStringIsItsDefinitionLine() {
        assertThat(new Morphogen(new Index(1), 0.8f, 0.1f).toString())
                .isEqualTo("Morphogen[1]: 0.8, 0.1");
        assertThat(new Morphogen(Index.parse("2.1"), 0.95f, 0.2f).toString())
                .isEqualTo("Morphogen[2.1]: 0.95, 0.2");
        // An integral ratio loses the trailing ".0" like every other emitted number.
        assertThat(new Morphogen(new Index(3), 1.0f, 0.0f).toString())
                .isEqualTo("Morphogen[3]: 1, 0");
    }

    @Test
    void metaGenesToStringIsTheSectionBodyInRegistryOrder() {
        MetaGenes metaGenes = new MetaGenes();
        metaGenes.setHyperGene(0.5f);
        metaGenes.setInitialAngle(45f);

        String body = metaGenes.toString();
        assertThat(body).isEqualTo("hyperGene: 0.5 (Float)\ninitialAngle: 45 (Float)\n");
        // Registry order is what makes the output canonical rather than JVM-dependent.
        assertThat(body.indexOf("hyperGene")).isLessThan(body.indexOf("initialAngle"));
    }

    /**
     * The morphogen line written by the canonical spelling is the morphogen line the reader understands.
     * This is the property that keeps the two directions of the format in step.
     */
    @Test
    void morphogenLineRoundTripsThroughTheReader() {
        Genome genome = GenomeCodec.parse("--- Genes\n"
                + "1.    express Morphogen[1] amount 1\n"
                + "--- Morphogens\n"
                + "Morphogen[1]: 0.8, 0.1\n");

        String emitted = genome.getMorphogens().get(0).toString();
        assertThat(emitted).isEqualTo("Morphogen[1]: 0.8, 0.1");

        Genome reparsed = GenomeCodec.parse("--- Genes\n"
                + "1.    express Morphogen[1] amount 1\n"
                + "--- Morphogens\n" + emitted + "\n");
        assertThat(reparsed.getMorphogens().get(0).getDiffusionRatio()).isEqualTo(0.8f);
        assertThat(reparsed.getMorphogens().get(0).getDecayRatio()).isEqualTo(0.1f);
    }
}
