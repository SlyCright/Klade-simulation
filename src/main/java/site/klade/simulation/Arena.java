package site.klade.simulation;

public class Arena {

    private final Genome genome;

    private float fitness;

    public Arena(Genome genome) {
        // TODO: implement
        this.genome = genome;
    }

    public void run() {
        // TODO: implement
        genome.setFitness(fitness);
    }

}
