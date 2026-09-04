package site.klade.simulation;

public class PriorityGenerator {

    private int nextPriority = 0;

    public int next() {
        return nextPriority++;
    }

    public void reset() {
        nextPriority = 0;
    }

}
