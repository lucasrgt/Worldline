package worldline.smoke.terrainb173;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Observes exact generated block state at one absolute official-world chunk. */
public final class FixedSeedTerrainSmoke {
    private FixedSeedTerrainSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: FixedSeedTerrainSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient client = new B173WireClient(
                "127.0.0.1", port, arguments[4], timeout);
        int chunkX = Integer.parseInt(arguments[5]), chunkZ = Integer.parseInt(arguments[6]);
        PlayerPose pose; RemoteChunkSnapshot chunk;
        try {
            server.boot(); B173PlayerSeed.write(workspace, arguments[4], 8.5D, 120.0D, 8.5D);
            client.connect(); pose = client.synchronizePose();
            RemoteWorldView world = client.awaitRemoteChunk(chunkX, chunkZ);
            chunk = world.chunkAt(chunkX, chunkZ); verify(chunk, chunkX, chunkZ);
        } finally { client.close(); server.close(); }
        String terrain = terrainHash(chunk), metadata = metadataHash(chunk), surface = surfaceHash(chunk);
        String trace = "v2|server=official-b1.7.3|seed=" + seed
                + "|target=absolute-chunk|origin=" + chunkX + "," + chunkZ
                + "|blocks=32768|nonair=" + chunk.nonAirBlocks()
                + "|surface=" + surface + "|full-id-plane=diagnostic"
                + "|decode=packet50+packet51-xzy|disconnect=clean";
        System.out.println("WORLDLINE_M111_TERRAIN=chunk=" + chunkX + ":" + chunkZ
                + ",nonair=" + chunk.nonAirBlocks() + ",terrain=" + terrain
                + ",surface=" + surface);
        System.out.println("WORLDLINE_M111_METADATA_DIAGNOSTIC=" + metadata);
        System.out.println("WORLDLINE_M111_SPAWN_DIAGNOSTIC="
                + pose.x() + ":" + pose.y() + ":" + pose.z());
        System.out.println("WORLDLINE_M111_TRACE=" + trace);
        System.out.println("WORLDLINE_M111_SIGNATURE=" + sha256(trace.getBytes(StandardCharsets.UTF_8)));
    }

    private static void verify(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        require(chunk.observation().x() == chunkX * 16 && chunk.observation().z() == chunkZ * 16
                && chunk.observation().y() == 0 && chunk.observation().width() == 16
                && chunk.observation().height() == 128 && chunk.observation().depth() == 16,
                "target chunk shape/origin drift");
        require(chunk.blockCount() == 32768 && chunk.nonAirBlocks() > 0
                && chunk.nonAirBlocks() < 32768, "implausible terrain census");
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
            require(top(chunk, x, z) >= 0, "empty generated column");
    }

    private static String terrainHash(RemoteChunkSnapshot chunk) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
            for (int y = 0; y < 128; y++) digest.update((byte) chunk.blockAt(x, y, z).legacyId());
        return hex(digest.digest());
    }

    private static String metadataHash(RemoteChunkSnapshot chunk) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
            for (int y = 0; y < 128; y++) digest.update((byte) chunk.blockAt(x, y, z).metadata());
        return hex(digest.digest());
    }

    private static String surfaceHash(RemoteChunkSnapshot chunk) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); ByteBuffer value = ByteBuffer.allocate(4);
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) { int y = top(chunk, x, z);
            BlockState state = chunk.blockAt(x, y, z); value.clear();
            value.putShort((short) y).put((byte) state.legacyId()).put((byte) state.metadata());
            digest.update(value.array()); }
        return hex(digest.digest());
    }

    private static int top(RemoteChunkSnapshot chunk, int x, int z) {
        for (int y = 127; y >= 0; y--) if (chunk.blockAt(x, y, z).legacyId() != 0) return y;
        return -1;
    }
    private static String sha256(byte[] value) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(value));
    }
    private static String hex(byte[] value) { StringBuilder result = new StringBuilder();
        for (byte item : value) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
