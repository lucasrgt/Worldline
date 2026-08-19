package worldline.smoke.lightingb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Observes both complete vanilla light planes at one absolute chunk. */
public final class FixedSeedLightingSmoke {
    private FixedSeedLightingSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: FixedSeedLightingSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]), chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]); long seed = Long.parseLong(arguments[3]);
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient client = new B173WireClient("127.0.0.1", port, arguments[4], timeout);
        RemoteChunkSnapshot chunk;
        try { server.boot(); B173PlayerSeed.write(workspace, arguments[4], 8.5D, 120.0D, 8.5D);
            client.connect(); client.synchronizePose();
            RemoteWorldView world = client.awaitRemoteChunk(chunkX, chunkZ);
            chunk = world.chunkAt(chunkX, chunkZ); verify(chunk, chunkX, chunkZ);
        } finally { client.close(); server.close(); }
        Plane block = plane(chunk, false), sky = plane(chunk, true);
        String trace = "v1|server=official-b1.7.3|seed=" + seed
                + "|target=absolute-chunk|origin=" + chunkX + "," + chunkZ
                + "|samples=32768|block=" + block.hash + "|blockHist=" + block.histogram
                + "|sky=" + sky.hash + "|skyHist=" + sky.histogram
                + "|decode=packet51-nibbles-xzy|disconnect=clean";
        System.out.println("WORLDLINE_M112_LIGHT=chunk=" + chunkX + ":" + chunkZ
                + ",block=" + block.hash + ",sky=" + sky.hash
                + ",blockHist=" + block.histogram + ",skyHist=" + sky.histogram);
        System.out.println("WORLDLINE_M112_TRACE=" + trace);
        System.out.println("WORLDLINE_M112_SIGNATURE=" + sha256(trace.getBytes(StandardCharsets.UTF_8)));
    }

    private static Plane plane(RemoteChunkSnapshot chunk, boolean sky) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); int[] counts = new int[16];
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
            for (int y = 0; y < 128; y++) { int value = sky ? chunk.skyLightAt(x, y, z)
                    : chunk.blockLightAt(x, y, z); digest.update((byte) value); counts[value]++; }
        StringBuilder histogram = new StringBuilder();
        for (int value = 0; value < counts.length; value++) { if (value > 0) histogram.append(';');
            histogram.append(value).append(':').append(counts[value]); }
        return new Plane(hex(digest.digest()), histogram.toString());
    }

    private static void verify(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        require(chunk.observation().x() == chunkX * 16 && chunk.observation().z() == chunkZ * 16
                && chunk.observation().y() == 0 && chunk.observation().width() == 16
                && chunk.observation().height() == 128 && chunk.observation().depth() == 16
                && chunk.blockCount() == 32768, "lighting target shape drift");
    }
    private static String sha256(byte[] value) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(value)); }
    private static String hex(byte[] value) { StringBuilder result = new StringBuilder();
        for (byte item : value) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
    private static final class Plane { final String hash, histogram;
        Plane(String hash, String histogram) { this.hash = hash; this.histogram = histogram; } }
}
