package site.klade.simulation.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class Kinematics implements Component {

    public final Vector2 acceleration = new Vector2();

    public final Vector2 velocity = new Vector2();

    public final Vector2 position = new Vector2();

}
