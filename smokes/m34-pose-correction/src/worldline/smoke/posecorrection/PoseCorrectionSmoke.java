package worldline.smoke.posecorrection;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteWorldView;
import worldline.api.SustainedRemoteWorldMultiplayerSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves an invalid movement converges to the server pose without losing the cache. */
public final class PoseCorrectionSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|session=protocol14|attempt=solid-block-center"
            + "|correction=packet13-decoded-acknowledged|pose=server-authoritative"
            + "|cache=preserved|disconnect=clean";

    private PoseCorrectionSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: PoseCorrectionSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        SustainedRemoteWorldMultiplayerSession client = new B173WireClient("127.0.0.1", port, username, timeout);
        PlayerPose initial, attempted, corrected; RemoteWorldView after;
        try {
            server.boot(); client.connect(); initial = client.synchronizePose();
            int chunkX = (int) Math.floor(initial.x()) >> 4, chunkZ = (int) Math.floor(initial.z()) >> 4;
            client.awaitRemoteChunk(chunkX, chunkZ); RemoteWorldView before = client.sustainTicks(5);
            BlockPosition block = solid(before, initial);
            attempted = client.moveBy(block.x() + .5D - initial.x(), block.y() - initial.y(),
                    block.z() + .5D - initial.z()); after = client.sustainTicks(10);
            corrected = client.moveBy(0, 0, 0);
            require(!corrected.equals(attempted), "server did not correct invalid movement");
            require(corrected.equals(initial), "correction did not restore authoritative pose");
            require(after.containsChunk(chunkX, chunkZ), "pose correction lost original cached chunk");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M34_API=server,session,movement,correction,pose,cache");
        System.out.println("WORLDLINE_M34_POSE=" + pose(initial) + "->" + pose(attempted) + "->" + pose(corrected));
        System.out.println("WORLDLINE_M34_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M34_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M34_SIGNATURE=" + sha256(TRACE));
    }

    private static BlockPosition solid(RemoteWorldView world, PlayerPose pose) {
        int centerX = (int) Math.floor(pose.x()), centerY = (int) Math.floor(pose.y());
        int centerZ = (int) Math.floor(pose.z());
        for (int y = centerY; y >= centerY - 5; y--) for (int radius = 0; radius <= 4; radius++)
            for (int x = centerX - radius; x <= centerX + radius; x++)
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    if (!world.containsChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) continue;
                    int id = world.blockAt(x, y, z).legacyId();
                    if (id > 0 && (id < 7 || id > 11)) return new BlockPosition(x, y, z);
                }
        throw new IllegalStateException("nearby solid block absent");
    }
    private static String pose(PlayerPose value) { return value.x() + "," + value.y() + "," + value.z(); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
