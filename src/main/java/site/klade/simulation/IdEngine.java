package site.klade.simulation;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

public class IdEngine {

    private int id = 0;

    private final IntMap<Entity> idToEntity = new IntMap<Entity>();
    private final ObjectMap<Entity, Integer> entityToId = new ObjectMap<Entity, Integer>();

    public int getNextId() {
        return id++;
    }

    public Entity getEntityById(int id) {
        return idToEntity.get(id);
    }

    public int getIdByEntity(Entity entity) {
        return entityToId.get(entity);
    }

    public void registerEntity(Entity entity) {
        int entityId = getNextId();
        idToEntity.put(entityId, entity);
        entityToId.put(entity, entityId);
    }

    public void removeEntity(int id) {
        Entity entity = idToEntity.get(id);
        if (entity != null) {
            entityToId.remove(entity);
        }
        idToEntity.remove(id);
    }
}
