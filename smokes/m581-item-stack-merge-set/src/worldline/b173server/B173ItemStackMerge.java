package worldline.b173server;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.RemoteItemStack;

/** Packet29 and live-count peeks for the official item-stack-merge SET. */
public final class B173ItemStackMerge {
    private B173ItemStackMerge() {}

    public static int live(B173WireClient client, RemoteItemStack expected) {
        return client.channel().inbound().dropped().liveCount(expected);
    }

    public static boolean destroyed(B173WireClient client, int entityId) {
        return client.channel().inbound().dropped().isDestroyed(entityId);
    }

    public static boolean collected(B173WireClient client, int entityId) {
        return client.channel().inbound().dropped().isCollected(entityId);
    }

    public static void awaitPlayers(B173DedicatedServer server, int count)
            throws Exception {
        long end = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) return;
            Thread.sleep(100L);
        }
        throw new IllegalStateException("player count drift");
    }

    public static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte item : bytes) text.append(String.format("%02x", item & 255));
        return text.toString();
    }

    public static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
