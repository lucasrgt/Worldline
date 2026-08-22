package worldline.b173server;

import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;

/** Reusable Packet21 observation for exact stacks dropped by a dead player. */
public final class B173PlayerDeathDrops {
    private B173PlayerDeathDrops() {}

    public static RemoteDroppedItem[] await(B173WireClient actor, RemoteItemStack... expected) {
        if (actor == null || expected == null || expected.length == 0 || expected.length > 36)
            throw new IllegalArgumentException("invalid player death drop request");
        RemoteDroppedItem[] result = new RemoteDroppedItem[expected.length];
        for (int index = 0; index < expected.length; index++) {
            RemoteItemStack stack = expected[index];
            if (stack == null) throw new IllegalArgumentException("null expected death drop");
            RemoteDroppedItem drop = actor.peekDroppedItem(stack);
            result[index] = drop == null ? actor.awaitDroppedItem(stack) : drop;
            if (!result[index].item().equals(stack))
                throw new IllegalStateException("player death Packet21 stack drift");
            for (int prior = 0; prior < index; prior++)
                if (result[prior].entityId() == result[index].entityId())
                    throw new IllegalStateException("player death Packet21 entity reused");
        }
        return result;
    }
}
