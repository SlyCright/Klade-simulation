package site.klade.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contract for {@link Indexes} — allocation policy of the index-independent genetic operator.
 *
 * <p>All fixtures below are the user's examples, reproduced literally:</p>
 * <ul>
 *   <li>step1/step2: the two-row insert producing items C and D;</li>
 *   <li>step3: insert into every gap producing items E, F, G, H;</li>
 *   <li>allocations follow the single generalized rule chain:
 *       grow-left (incrementLast) -&gt; shrink-right (decrementLast) -&gt; extend-left (k, k-1, ...),
 *       with k defaulting to 0 (user decision over spec §3.1's "2.2 -&gt; 2.2.1").</li>
 * </ul>
 *
 * <p>Signed-off API decisions:</p>
 * <ul>
 *   <li>Verb-first name: {@code allocateBetween} (it returns a value; it does not insert).</li>
 *   <li>Two parameters only: endpoints must be the genome's current consecutive neighbours
 *       (nullable) — uniqueness of the result follows from that adjacency, so there is
 *       deliberately NO occupancy set parameter; freshness is asserted test-side below against
 *       each fixture's genome listing, and {@code Genome.ensureSortedAndUnique()} backstops
 *       precondition violations.</li>
 *   <li>No tombstones: deleted indices may be reused — evolution decides what occupies an index.</li>
 *   <li>No tree semantics: longer indices sharing a prefix are not "children" of it.</li>
 * </ul>
 */
@DisplayName("Indexes — allocateBetween (gap / append / prepend)")
class IndexesTest {

    private static Index idx(String s) {
        return Index.parse(s);
    }

    private static String allocateBetween(String left, String right) {
        Index allocated = Indexes.allocateBetween(
                left == null ? null : idx(left),
                right == null ? null : idx(right));
        return allocated.toString();
    }

    // ---------------------------------------------------------------- user's step1 -> step2

    @Test
    @DisplayName("step1->step2: insert between 0. and 1. gives 0.0. (item C)")
    void step1InsertBetween() {
        assertThat(allocateBetween("0.", "1.")).isEqualTo("0.0.");
    }

    @Test
    @DisplayName("step1->step2: insert after last (1.) gives 2. (item D)")
    void step1AppendAtEnd() {
        assertThat(allocateBetween("1.", null)).isEqualTo("2.");
    }

    // ---------------------------------------------------------------- user's step2 -> step3

    @Test
    @DisplayName("step2->step3: between 0. and 0.0. gives 0.-1. (item E)")
    void step3GapAfterPrefix() {
        assertThat(allocateBetween("0.", "0.0.")).isEqualTo("0.-1.");
    }

    @Test
    @DisplayName("step2->step3: between 0.0. and 1. gives 0.1. (item F)")
    void step3GapBeforeLevelJump() {
        assertThat(allocateBetween("0.0.", "1.")).isEqualTo("0.1.");
    }

    @Test
    @DisplayName("step2->step3: between 1. and 2. gives 1.0. (item G)")
    void step3GapConsecutive() {
        assertThat(allocateBetween("1.", "2.")).isEqualTo("1.0.");
    }

    @Test
    @DisplayName("step2->step3: insert after last (2.) gives 3. (item H)")
    void step3AppendAtEnd() {
        assertThat(allocateBetween("2.", null)).isEqualTo("3.");
    }

    // ---------------------------------------------------------------- generalized endpoints (nullable both sides)

    @Test
    @DisplayName("F2: insert before first (left == null) -> R.decrementLast: before 0. gives -1.")
    void prependBeforeFirst() {
        assertThat(allocateBetween(null, "0.")).isEqualTo("-1.");
    }

    @Test
    @DisplayName("F2: prepend before a negative first index: before 0.-1. gives -1.")
    void prependBeforeNegativeFirst() {
        assertThat(allocateBetween(null, "0.-1.")).isEqualTo("-1.");
    }

    @Test
    @DisplayName("F3: empty genome (both endpoints null) -> root index 0.")
    void emptyGenomeRoot() {
        assertThat(allocateBetween(null, null)).isEqualTo("0.");
    }

    @Test
    @DisplayName("append uses the next outer level: between 0.1. and null -> 1.")
    void appendFreeIncrement() {
        assertThat(allocateBetween("0.1.", null)).isEqualTo("1.");
    }

    // ---------------------------------------------------------------- spec §3.1 cases

    @Test
    @DisplayName("spec §3.1: numeric hole is filled (virgin gap): between 1.2. and 1.4. -> 1.3.")
    void numericHoleFilled() {
        assertThat(allocateBetween("1.2.", "1.4.")).isEqualTo("1.3.");
    }

    @Test
    @DisplayName("spec §3.1: different levels: between 2.1. and 3. -> 2.2.")
    void differentLevelsGrowLeft() {
        assertThat(allocateBetween("2.1.", "3.")).isEqualTo("2.2.");
    }

    @Test
    @DisplayName("append uses the next outer level: after 2.3. comes 3.")
    void appendAfterTwoThree() {
        assertThat(allocateBetween("2.3.", null)).isEqualTo("3.");
    }

    @Test
    @DisplayName("signed-off k=0: between 2.2. and 2.3. -> 2.2.0. (NOT spec's 2.2.1.)")
    void appendKDefaultsToZero() {
        assertThat(allocateBetween("2.2.", "2.3.")).isEqualTo("2.2.0.");
    }

    // ---------------------------------------------------------------- rule-chain mechanics

    @Test
    @DisplayName("multi-level right neighbor: between 0. and 0.5.7. gives 0.0.")
    void shrinkRightPreferredOverExtendLeft() {
        assertThat(allocateBetween("0.", "0.5.7.")).isEqualTo("0.0.");
    }

    @Test
    @DisplayName("current level is preferred: between 1.3. and 1.15. gives 1.4.")
    void currentLevelIsPreferred() {
        assertThat(allocateBetween("1.3.", "1.15.")).isEqualTo("1.4.");
    }

    @Test
    @DisplayName("derived from signed-off rule: insert between an index and its extension: "
            + "between 0.-1. and 0.-1.0. gives 0.-1.-1.")
    void insertBetweenIndexAndItsExtension() {
        assertThat(allocateBetween("0.-1.", "0.-1.0.")).isEqualTo("0.-1.-1.");
    }

    @Test
    @DisplayName("negative mirror of the G case: between -1. and 0. gives -1.0.")
    void negativeMirrorOfStepThreeG() {
        assertThat(allocateBetween("-1.", "0.")).isEqualTo("-1.0.");
    }

    @Test
    @DisplayName("same level negative fallback: 0.-15. | 0.-5. -> 0.-6.")
    void sameLevelNegativeFallback() {
        assertThat(allocateBetween("0.-15.", "0.-5.")).isEqualTo("0.-6.");
    }

    // ---------------------------------------------------------------- contract / validation

    @Test
    @DisplayName("F6: left >= right -> IllegalArgumentException")
    void leftMustPrecedeRight() {
        assertThatThrownBy(() -> allocateBetween("2.", "1."))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> allocateBetween("2.1.", "2.1."))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> allocateBetween("0.-1.", "0."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("result is strictly inside the gap and fresh (not in the genome) — all fixtures")
    void resultStrictlyInsideGapAndFresh() {
        // each fixture: [left, right, ...genome indices of the target specimen]
        String[][] fixtures = {
                {"0.", "1.", "0.", "1."},                              // C
                {"1.", null, "0.", "1."},                              // D
                {"0.", "0.0.", "0.", "0.0.", "1.", "2."},              // E
                {"0.0.", "1.", "0.", "0.0.", "1.", "2."},              // F
                {"1.", "2.", "0.", "0.0.", "1.", "2."},                // G
                {"2.", null, "0.", "0.0.", "1.", "2."},                // H
                {null, "0.", "0.", "1."},                              // F2 prepend
                {null, "0.-1.", "0.-1.", "0."},                        // F2 prepend, negative
                {null, null},                                          // F3 empty genome
                {"1.2.", "1.4.", "1.2.", "1.4."},                      // hole fill
                {"2.1.", "3.", "2.1.", "3."},                          // different levels
                {"2.2.", "2.3.", "2.2.", "2.3."},                      // k = 0
                {"0.", "0.5.7.", "0.", "0.5.7."},                      // 0.0. lies inside
                {"1.3.", "1.15.", "1.3.", "1.15."},                    // 1.4. lies inside
                {"-1.", "0.", "-1.", "0."},                            // negative mirror
                {"0.-1.", "0.-1.0.", "0.-1.", "0.-1.0."},              // derived: index vs extension
        };
        for (String[] fixture : fixtures) {
            String left = fixture[0];
            String right = fixture[1];
            List<String> genome = new ArrayList<>();
            for (int i = 2; i < fixture.length; i++) {
                genome.add(fixture[i]);
            }
            Index allocated = Indexes.allocateBetween(
                    left == null ? null : idx(left),
                    right == null ? null : idx(right));
            String label = "allocateBetween(" + left + ", " + right + ") -> " + allocated;
            assertThat(genome)
                    .as("%s: result must be fresh (not already in the genome)", label)
                    .doesNotContain(allocated.toString());
            if (left != null) {
                assertThat(idx(left).compareTo(allocated))
                        .as("%s: must sort after left", label).isNegative();
            }
            if (right != null) {
                assertThat(allocated.compareTo(idx(right)))
                        .as("%s: must sort before right", label).isNegative();
            }
        }
    }

    @Test
    @DisplayName("allocation is pure: same inputs -> same output")
    void allocationIsPure() {
        String first = allocateBetween("1.", "2.");
        String second = allocateBetween("1.", "2.");
        assertThat(first).isEqualTo(second).isEqualTo("1.0.");
    }

    @ParameterizedTest(name = "[{index}] allocated index differs per gap: {0}|{1} -> {2}")
    @MethodSource("distinctGapsProduceDistinctIndices")
    @DisplayName("different gaps of the same genome produce different indices")
    void distinctGapsProduceDistinctIndices(String left, String right, String expected) {
        assertThat(allocateBetween(left, right)).isEqualTo(expected);
    }

    static Stream<Arguments> distinctGapsProduceDistinctIndices() {
        return Stream.of(
                Arguments.of("0.", "0.0.", "0.-1."),
                Arguments.of("0.0.", "1.", "0.1."),
                Arguments.of("1.", "2.", "1.0."),
                Arguments.of("2.", null, "3."),
                Arguments.of(null, "0.", "-1.")
        );
    }

}
