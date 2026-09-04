package site.klade.simulation;

public class TickCounter {

    private int currentTick = 0;

    private final int maxTicks;

    private boolean evaluationComplete = false;

    public TickCounter(int maxTicks) {
        this.maxTicks = maxTicks;
    }

    public void increment() {
        if (currentTick++ >= maxTicks) evaluationComplete = true;
    }

    public boolean shouldContinue() {
        return !evaluationComplete;
    }

}
