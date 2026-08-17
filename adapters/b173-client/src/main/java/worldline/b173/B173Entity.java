package worldline.b173;

import net.minecraft.src.Entity;
import worldline.api.GameEntity;
import worldline.api.GamePosition;

/** Neutral live entity handle over one mapped b1.7.3 entity. */
class B173Entity implements GameEntity {
    private final B173ClientBackend owner;
    final Entity entity;

    B173Entity(B173ClientBackend owner, Entity entity) {
        if (owner == null) throw new NullPointerException("owner");
        if (entity == null) throw new NullPointerException("entity");
        this.owner = owner;
        this.entity = entity;
    }

    @Override public int id() { return value().entityId; }

    @Override public String type() {
        return B173Types.of(value());
    }

    @Override public GamePosition position() {
        Entity value = value();
        return new GamePosition(value.posX, value.posY, value.posZ);
    }

    @Override public boolean alive() { return !value().isDead; }

    @Override public void teleport(GamePosition position) {
        if (position == null) throw new NullPointerException("position");
        value().setPosition(position.x(), position.y(), position.z());
    }

    private Entity value() { owner.client(); return entity; }
}
