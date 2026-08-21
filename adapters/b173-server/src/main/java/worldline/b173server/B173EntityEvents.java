package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;

/** Single reader for Packet38 and Packet29 shared by combat, dropped items and mobs. */
final class B173EntityEvents {
    private final B173CombatTracker combat;
    private final B173DroppedItemTracker dropped;
    private final B173MobTracker mobs;
    B173EntityEvents(B173CombatTracker combat, B173DroppedItemTracker dropped, B173MobTracker mobs) {
        this.combat = combat; this.dropped = dropped; this.mobs = mobs;
    }
    void status(DataInputStream input) throws IOException {
        int entity = input.readInt(), status = input.readByte();
        combat.status(entity, status); mobs.status(entity, status);
    }
    void destroy(DataInputStream input) throws IOException {
        int entity = input.readInt(); dropped.destroy(entity); mobs.destroy(entity);
    }
}
