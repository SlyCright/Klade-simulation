package site.klade.simulation;

import com.badlogic.ashley.core.Component;

public class SimulationStateComponent implements Component {

    public long totalTicks;

    public long rhymeNodeStateChanges;

    public boolean isRhymeNodeCurrentlyActive;
}
