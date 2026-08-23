package worldline.b173server;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Reusable packet-level fixture operations shared by official b1.7.3 smoke oracles. */
public final class B173FixtureSupport {
    private B173FixtureSupport() { }

    public static BlockPosition place(B173WireClient client, BlockPosition support,
            BlockFace face, int legacyId) throws Exception {
        return place(client, support, face, new BlockState(legacyId, 0));
    }

    public static BlockPosition place(B173WireClient client, BlockPosition support,
            BlockFace face, BlockState expected) throws Exception {
        BlockPosition target = face.adjacent(support);
        client.placeHeldBlock(support, face);
        client.awaitBlock(target, expected);
        return target;
    }

    public static void awaitPlayers(B173DedicatedServer server, int expected) throws Exception {
        long deadline = System.currentTimeMillis() + 5_000L;
        while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == expected) return;
            Thread.sleep(100L);
        }
        throw new IllegalStateException("player count drift");
    }

    public static int local(int coordinate, int chunk) {
        return coordinate - chunk * 16;
    }

    public static boolean water(int legacyId) {
        return legacyId == 8 || legacyId == 9;
    }

    public static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255));
        return result.toString();
    }
}
