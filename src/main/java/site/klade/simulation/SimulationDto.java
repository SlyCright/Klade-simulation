package site.klade.simulation;

public class SimulationDto {

    private final long totalTicks;

    private final long rhymeNodeStateChanges;

    private final boolean isRhymeNodeCurrentlyActive;

    public SimulationDto(long totalTicks, long rhymeNodeStateChanges, boolean isRhymeNodeCurrentlyActive) {
        this.totalTicks = (int) totalTicks;
        this.rhymeNodeStateChanges = rhymeNodeStateChanges;
        this.isRhymeNodeCurrentlyActive = isRhymeNodeCurrentlyActive;
    }

    @SuppressWarnings("unused")
    public long getTotalTicks() {
        return totalTicks;
    }

    @SuppressWarnings("unused")
    public long getRhymeNodeStateChanges() {
        return rhymeNodeStateChanges;
    }

    @SuppressWarnings("unused")
    public boolean getIsRhymeNodeCurrentlyActive() {
        return isRhymeNodeCurrentlyActive;
    }
}