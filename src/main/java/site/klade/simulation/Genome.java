package site.klade.simulation;

import site.klade.simulation.gene.Gene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Genome {

    private final MetaGenes metaGenes;

    private List<Gene> genes = new ArrayList<Gene>();

    private List<Morphogen> morphogens = new ArrayList<Morphogen>();

    private float currentFitness = Float.MAX_VALUE;  // Updated every battle tick

    private float accumulatedFitness = 0.0f;         // Summed after every battle

    // Constructor for first generation initialization
    public Genome(float hyperGene) {
        this.metaGenes = MetaGenes.createWithHyperGene(hyperGene);
    }

    // Constructor for offspring creation (don't forget to reset fitnesses)
    public Genome(Genome other) {
        this.metaGenes = new MetaGenes(other.getMetaGenes());
        List<Morphogen> otherMorphogenes = other.getMorphogens();
        this.morphogens = new ArrayList<Morphogen>(otherMorphogenes.size());
        for (Morphogen m : otherMorphogenes) {
            this.morphogens.add(new Morphogen(m));
        }
        List<Gene> otherGenes = other.getGenes();
        this.genes = new ArrayList<Gene>(otherGenes.size());
        for (Gene g : otherGenes) {
            this.genes.add(new Gene(g));
        }
        this.currentFitness = other.getCurrentFitness();
        this.accumulatedFitness = other.getAccumulatedFitness();
    }

    // Constructor for creation from DTO
    public Genome(MetaGenes metaGenes, List<Morphogen> morphogens, List<Gene> genes) {
        this.metaGenes = metaGenes;
        this.morphogens = new ArrayList<Morphogen>(morphogens);
        this.genes = new ArrayList<Gene>(genes);
    }

    public void resetCurrentFitness() {
        this.currentFitness = Float.MAX_VALUE;
    }

    public void resetFitnesses() {
        this.currentFitness = Float.MAX_VALUE;
        this.accumulatedFitness = 0.0f;
    }

    public void updateAccumulatedFitness() {
        accumulatedFitness += currentFitness;
    }

    public MetaGenes getMetaGenes() {
        return metaGenes;
    }

    /**
     * Materialises {@link #morphogens} from the references actually present in the genes.
     *
     * <p>A morphogen definition exists <b>because a gene references it</b>, so the set is derived rather
     * than authored: definitions with no remaining reference are removed, and ids that are referenced but
     * undefined gain a definition.</p>
     *
     * <p><b>Inheritance is by dotted position, never by recorded provenance.</b> A genome may arrive here
     * without any mutator having run — crossover assembles a child from two parents' genes — so a
     * definition has to be reconstructible from the reference set alone. See
     * {@link #sourceFor(Index, java.util.Set)}.</p>
     *
     * <p><b>Idempotent.</b> It re-randomises nothing that already exists, so calling it twice on the same
     * genome changes nothing. The only randomness is the initial draw for a morphogen that has no source
     * at all — which happens once per lineage, for its first morphogen.</p>
     *
     * <p>This method never <i>allocates</i> an id; which id to use is the mutation layer's decision
     * (see the plan's `MorphogenIndexPolicy`). Here we only materialise whatever the genes reference.</p>
     */
    public void deriveMorphogens() {
        Set<Index> referenced = MorphogenRefs.sorted(MorphogenRefs.collect(this));
        // Keep only definitions that are still referenced, and remember what we already have so the
        // inheritance pass below can use survivors as sources.
        List<Morphogen> kept = new ArrayList<Morphogen>(referenced.size());
        for (Morphogen morphogen : morphogens) {
            if (referenced.contains(morphogen.getId())) kept.add(morphogen);
        }
        // Add a definition for every referenced-but-undefined id, inheriting from whatever is defined
        // so far. Processing in sorted order makes the result independent of iteration order: a source
        // is decided from the definitions that exist, not from the order the ids happen to arrive.
        for (Index id : referenced) {
            if (findById(kept, id) == null) kept.add(createFor(id, kept));
        }
        // Canonical order: by dotted index. Genes are kept sorted the same way, and the codec emits in
        // this order, so the whole genome has one deterministic layout.
        Collections.sort(kept, new Comparator<Morphogen>() {
            @Override
            public int compare(Morphogen left, Morphogen right) {
                return left.getId().compareTo(right.getId());
            }
        });
        morphogens.clear();
        morphogens.addAll(kept);
    }

    /** Every morphogen index referenced by any gene, across arguments and conditions. */
    public Set<Index> referencedMorphogens() {
        return MorphogenRefs.sorted(MorphogenRefs.collect(this));
    }

    /** The defined morphogen with this id, or {@code null}. */
    public Morphogen findMorphogen(Index id) {
        return findById(morphogens, id);
    }

    /** The gene with this index, or {@code null}. */
    public Gene findGene(Index id) {
        for (Gene gene : genes) {
            if (gene.getIndex() != null && gene.getIndex().equals(id)) {
                return gene;
            }
        }
        return null;
    }

    private static Morphogen findById(List<Morphogen> candidates, Index id) {
        for (Morphogen morphogen : candidates) {
            if (morphogen.getId().equals(id)) {
                return morphogen;
            }
        }
        return null;
    }

    /**
     * Builds the definition for a newly referenced morphogen: an exact copy of its order-nearest
     * source, or a randomised definition when there is no source.
     */
    private Morphogen createFor(Index id, List<Morphogen> defined) {
        Morphogen source = sourceFor(id, defined);
        if (source != null) {
            return new Morphogen(id, source.getDiffusionRatio(), source.getDecayRatio());
        }
        return new Morphogen(id, randomRatio(), randomRatio());
    }

    /**
     * The morphogen this id inherits its values from: the defined id immediately <i>before</i> it in
     * dotted order, or, when there is none, the defined id immediately after it. {@code null} only when
     * nothing at all is defined yet.
     *
     * <p><b>Predecessor first</b> because that is what recovers duplication: an id allocated adjacent to
     * its source (which is what duplication does) resolves to that source. It also matches how the
     * genome's own {@code IndexAllocator} fills gaps.</p>
     *
     * <p>This is deliberately a plain ordering rule rather than a prefix or "ancestor" rule. Dotted ids
     * encode order only — there is no morphogen tree — and an order-based rule needs no reasoning about
     * nesting depth, which keeps it consistent with {@link Index}'s own contract.</p>
     */
    static Morphogen sourceFor(Index id, List<Morphogen> defined) {
        Morphogen predecessor = null;
        Morphogen successor = null;
        for (Morphogen candidate : defined) {
            int order = candidate.getId().compareTo(id);
            if (order < 0 && (predecessor == null
                    || candidate.getId().compareTo(predecessor.getId()) > 0)) {
                predecessor = candidate;
            } else if (order > 0 && (successor == null
                    || candidate.getId().compareTo(successor.getId()) < 0)) {
                successor = candidate;
            }
        }
        return predecessor != null ? predecessor : successor;
    }

    /**
     * A value for a brand-new morphogen, uniform in {@code [0.0, 1.0]} — the same domain every
     * morphogen value is later mutated within, so a new definition can never start outside it.
     *
     * <p>Random rather than a fixed default on purpose: a constant would place every lineage's first
     * morphogen at the same point in value space, biasing all lineages toward one phenotype instead of
     * letting selection search.</p>
     */
    private static float randomRatio() {
        return RANDOM.nextFloat();
    }

    /**
     * Shared source of randomness for a new morphogen's initial values. Kept as a field rather than a
     * static utility so it stays GWT-friendly and consistent with the other classes here, which all use
     * {@link Random} (see {@code MetaGenes}).
     */
    private static final Random RANDOM = new Random();

    public float getInitialAngle() {
        return metaGenes.getInitialAngle();
    }

    public float getHyperGene() {
        return metaGenes.getHyperGene();
    }

    public List<Morphogen> getMorphogens() {
        return morphogens;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public float getCurrentFitness() {
        return currentFitness;
    }

    public void setCurrentFitness(float currentFitness) {
        this.currentFitness = currentFitness;
    }

    public float getAccumulatedFitness() {
        return accumulatedFitness;
    }

    public void setAccumulatedFitness(float accumulatedFitness) {
        this.accumulatedFitness = accumulatedFitness;
    }

    /**
     * A <b>diagnostic summary</b> for logs and test output — deliberately not the DNA text.
     *
     * <p>The canonical spelling of a genome is a document: it needs section markers and the explanatory
     * comment header, and assembling those is the backend writer's job. This library only owns the
     * canonical spelling of the <i>pieces</i> — {@link site.klade.simulation.gene.Gene#toString()},
     * {@link Morphogen#toString()} and {@link MetaGenes#toString()} — which is why no method here emits a
     * whole DNA document.</p>
     *
     * <p>The pieces are embedded verbatim so a debug print shows the same text the writer would emit for
     * them; the counts make an empty genome obvious at a glance.</p>
     */
    @Override
    public String toString() {
        return "Genome(genes=" + genes.size()
                + ", morphogens=" + morphogens.size()
                + ", currentFitness=" + currentFitness
                + ", accumulatedFitness=" + accumulatedFitness
                + ")\n" + metaGenes + genes + morphogens;
    }

}
