package site.klade.simulation.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

public class Specimen implements Component {

    public Array<Entity> nodes = new Array<Entity>();

    public Array<Entity> segments = new Array<Entity>();

}
