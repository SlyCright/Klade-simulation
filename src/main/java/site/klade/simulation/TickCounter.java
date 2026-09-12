package site.klade.simulation;

public class TickCounter {

    private int currentTick = 0;

    private final int maxTicks;

    private boolean done = false;

    public TickCounter(int maxTicks) {
        this.maxTicks = maxTicks;
    }

    public void increment() {
        if (++currentTick >= maxTicks) done = true;
    }

    public boolean shouldContinue() {
        return !done;
    }

    public boolean isDone() {
        return done;
    }

}
