package site.klade.simulation;

import com.badlogic.ashley.core.Component;

public class RhythmNodeComponent implements Component {

    public static final int PERIOD_TICKS = 10;

    public long tick;

    public boolean isActive;
}
