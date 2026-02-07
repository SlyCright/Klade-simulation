package site.klade.simulation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class Simulation {

    public final int SPECIES_TOTAL;

    public final int SPECIMENS_PER_SPECIES;

    public final int SLEEP_PER_UPDATE_MILLIS;

    private final AtomicInteger generationNumber = new AtomicInteger(0);

    private ArrayList<Species> speciesList = new ArrayList<>();

    private final ExecutorService executor;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Getter
    private volatile SimulationSnapshotDto snapShot;

    private final Random random = new Random();

    public static Simulation withDefaultSettings() {
        return with(SettingsDto.builder().build());
    }

    public static Simulation with(SettingsDto settings) {
        return new Simulation(settings);
    }

    private Simulation(SettingsDto settings) {
        this.SPECIES_TOTAL = settings.getSpeciesTotal();
        this.SPECIMENS_PER_SPECIES = settings.getSpecimensPerSpecies();
        this.SLEEP_PER_UPDATE_MILLIS = settings.getSleepPerUpdateMillis();
        // TODO: Use thread pool in future. Single thread is for MVP to prevent complexity of thread
        //  management
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "simulation-thread");
            t.setDaemon(true);
            return t;
        });
        initialize();
    }

    private void initialize() {
        generationNumber.set(0);
        speciesList.clear();
        for (int i = 0; i < SPECIES_TOTAL; i++) {
            speciesList.add(new Species(SPECIMENS_PER_SPECIES));
        }
        this.snapShot = new SimulationSnapshotDto(
                generationNumber.get(),
                DataTransferUtilities.getDeepCopyOf(speciesList));
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            executor.submit(this::simulationLoop);
        }
    }

    private void simulationLoop() {
        while (isRunning.get()) {
            try {
                update();
            } catch (Exception e) {
                System.out.println("Simulation error: " + e.getMessage());
                isRunning.set(false);
                break;
            }
        }
    }

    public void runCertainGenerations(int number) {
        if (isRunning.get()) return;
        for (int i = 0; i < number; i++) {
            update();
        }
    }

    public void stop() {
        isRunning.set(false);
    }

    public void reset() {
        stop();
        int i = 0;
        while (this.generationNumber.get() != 0 || !this.speciesList.isEmpty()) {
            sleepInterrupted();
            initialize();
            if (++i > 1000) break;
        }
    }

    public void shutdown() {
        stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void update() {
        if (sleepInterrupted()) return;
        fitnessCalculation();
        offspringsGeneration();
        this.snapShot = new SimulationSnapshotDto(
                generationNumber.incrementAndGet(),
                DataTransferUtilities.getDeepCopyOf(speciesList));
    }

    private boolean sleepInterrupted() {
        try {
            Thread.sleep(SLEEP_PER_UPDATE_MILLIS);
        } catch (InterruptedException e) {
            isRunning.set(false);
            Thread.currentThread().interrupt();
            return true;
        }
        return false;
    }

    private void fitnessCalculation() {
        for (var species : speciesList) {
            for (var genome : species.getGenomes()) {
                new Arena(genome).run();
            }
        }
    }

    private void offspringsGeneration() {
        var offspringsSpeciesList = new ArrayList<Species>();
        for (var species : speciesList) {
            var genomes = species.getGenomes();
            var offspringsGenomes = new ArrayList<Genome>();
            for (int i = 0; i < SPECIMENS_PER_SPECIES; i++) {
                var candidate1 = genomes.get(random.nextInt(SPECIMENS_PER_SPECIES));
                var candidate2 = genomes.get(random.nextInt(SPECIMENS_PER_SPECIES));
                var winner = candidate1.getFitness() > candidate2.getFitness() ?
                        candidate1 : candidate2;
                offspringsGenomes.add(new Genome(winner));
            }
            var offspringsSpecies = new Species(offspringsGenomes);
            offspringsSpeciesList.add(offspringsSpecies);
        }
        speciesList = offspringsSpeciesList;
    }
}