package worldline.smoke.movementoutcome;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.MovementDisposition;
import worldline.api.MovementOutcome;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RemoteWorldView;
import worldline.api.ResolvedMovementMultiplayerSession;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Distinguishes bounded unchallenged motion from authoritative rollback. */
public final class MovementOutcomeSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|small=unchallenged-persisted"
            + "|invalid=corrected|rollback=last-accepted-pose|cache=preserved|disconnect=clean";
    private MovementOutcomeSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: MovementOutcomeSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        ResolvedMovementMultiplayerSession client =
                new B173WireClient("127.0.0.1", port, username, timeout);
        PlayerPose initial; MovementOutcome small, rollback; RemoteWorldView after; ServerPlayerState player;
        try {
            server.boot(); client.connect(); awaitPlayers(server, Collections.singletonList(username));
            initial = client.synchronizePose(); int chunkX = floor(initial.x()) >> 4, chunkZ = floor(initial.z()) >> 4;
            client.awaitRemoteChunk(chunkX, chunkZ); client.sustainTicks(5);
            small = client.moveAndObserve(.125D, 0D, 0D, 5);
            require(small.disposition() == MovementDisposition.UNCHALLENGED
                    && close(small.resulting().x(), initial.x() + .125D), "small move was challenged");
            RemoteWorldView before = client.sustainTicks(1); BlockPosition block = solid(before, small.resulting());
            rollback = client.moveAndObserve(block.x() + .5D - small.resulting().x(),
                    block.y() - small.resulting().y(), block.z() + .5D - small.resulting().z(), 10);
            require(rollback.corrected() && rollback.resulting().equals(small.resulting()),
                    "invalid move did not roll back to last accepted pose");
            after = client.sustainTicks(1); require(after.containsChunk(chunkX, chunkZ), "rollback lost cached chunk");
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            require(close(player.x(), small.resulting().x()) && close(player.y(), small.resulting().y())
                    && close(player.z(), small.resulting().z()), "unchallenged move was not persisted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M35_API=movement-outcome,unchallenged,corrected,pose,cache,persistence");
        System.out.println("WORLDLINE_M35_POSES=" + pose(initial) + "->" + pose(small.resulting())
                + "->" + pose(rollback.attempted()) + "->" + pose(rollback.resulting()));
        System.out.println("WORLDLINE_M35_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M35_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M35_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M35_SIGNATURE=" + sha256(TRACE));
    }

    private static BlockPosition solid(RemoteWorldView world, PlayerPose pose) {
        int cx = floor(pose.x()), cy = floor(pose.y()), cz = floor(pose.z());
        for (int y = cy; y >= cy - 5; y--) for (int radius = 0; radius <= 4; radius++)
            for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) {
                if (!world.containsChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) continue;
                int id = world.blockAt(x, y, z).legacyId();
                if (id > 0 && (id < 7 || id > 11)) return new BlockPosition(x, y, z);
            }
        throw new IllegalStateException("nearby solid block absent");
    }
    private static void awaitPlayers(PersistentMultiplayerServerRuntime server, List<String> expected)
            throws InterruptedException { long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) { if (server.players().equals(expected)) return;
            Thread.sleep(100L); } throw new IllegalStateException("player list did not become " + expected); }
    private static int floor(double value) { return (int) Math.floor(value); }
    private static boolean close(double a, double b) { return Math.abs(a - b) < .000001D; }
    private static String pose(PlayerPose value) { return value.x() + "," + value.y() + "," + value.z(); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
