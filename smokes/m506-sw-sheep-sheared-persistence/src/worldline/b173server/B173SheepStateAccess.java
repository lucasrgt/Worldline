package worldline.b173server;

/** Smoke-only view of EntitySheep data-watcher index 16. */
public final class B173SheepStateAccess {
    private B173SheepStateAccess() {}

    public static int metadata(B173WireClient client, int entity) {
        if (client == null || entity < 0) throw new IllegalArgumentException("invalid sheep state request");
        return client.channel().inbound().mobs().size(entity);
    }

    public static int awaitMetadata(B173WireClient client, int entity, int expected) {
        for (int tick = 0; tick < 80; tick++) {
            int value = metadata(client, entity);
            if (value == expected) return value;
            client.sustainTicks(1);
        }
        throw new IllegalStateException("sheep metadata did not reach " + expected);
    }
}
