package site.klade.simulation;

import com.badlogic.gdx.math.Vector2;

public class Genome {

    // TODO: that's simulation settings. Should be moved there
    public static final float MAX_INITIAL_DISTANCE = 200f;

    public static final float MIN_INITIAL_DISTANCE = 150f;

    public static final float START_POSITION_MUTATION_RATE = 0.1f;

    public static final float INITIAL_IMPULSE_MUTATION_RATE = 1f;

    private final Vector2 startPosition = new Vector2();

    private final Vector2 initialImpulse = new Vector2();

    private float fitness;

    public Genome() {
        float distance = (float) (Math.random() *
                (MAX_INITIAL_DISTANCE - MIN_INITIAL_DISTANCE) + MIN_INITIAL_DISTANCE);
        float angle = (float) (Math.random() * 2 * Math.PI);
        float x = (float) (Math.cos(angle) * distance);
        float y = (float) (Math.sin(angle) * distance);
        startPosition.set(x, y);
        initialImpulse.set(
                (float) (Math.random() - 0.5) * INITIAL_IMPULSE_MUTATION_RATE,
                (float) (Math.random() - 0.5) * INITIAL_IMPULSE_MUTATION_RATE);
        fitness = 0f;
    }

    public Genome(Genome genome) {
        this.startPosition.set(genome.getStartPosition());
        this.initialImpulse.set(genome.getInitialImpulse());
        fitness = genome.getFitness();
    }

    public static Genome getMutatedZeroFitnessCopyOf(Genome genome) {
        return new Genome(genome).mutate();
    }

    public Vector2 getStartPosition() {
        return this.startPosition;
    }

    public Vector2 getInitialImpulse() {
        return this.initialImpulse;
    }

    public float getFitness() {
        return this.fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    public String toString() {
        return "Genome(startPositon=" + this.getStartPosition() +
                ", initialImpulse=" + this.getInitialImpulse() +
                ", fitness=" + this.getFitness() + ")";
    }

    private Genome mutate() {
        startPosition.add(
                new Vector2(
                        (float) (Math.random() - 0.5) * START_POSITION_MUTATION_RATE,
                        (float) (Math.random() - 0.5) * START_POSITION_MUTATION_RATE));
        initialImpulse.add(
                new Vector2(
                        (float) (Math.random() - 0.5) * INITIAL_IMPULSE_MUTATION_RATE,
                        (float) (Math.random() - 0.5) * INITIAL_IMPULSE_MUTATION_RATE));
        fitness = 0f;
        return this;
    }
}
