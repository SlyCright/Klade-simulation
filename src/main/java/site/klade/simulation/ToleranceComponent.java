package site.klade.simulation;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class ToleranceComponent implements Component {

    private final Vector2 previousPosition = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);

    private boolean toleranceReached = false;

    public Vector2 getPreviousPosition() {
        return previousPosition;
    }

    public boolean isToleranceReached() {
        return toleranceReached;
    }

    public void setToleranceReached(boolean toleranceReached) {
        this.toleranceReached = toleranceReached;
    }
}
