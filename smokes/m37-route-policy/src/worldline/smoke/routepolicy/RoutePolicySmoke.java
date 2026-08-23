package worldline.smoke.routepolicy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.MovementDisposition;
import worldline.api.MovementOutcome;
import worldline.api.MovementRouteResult;
import worldline.api.MovementStep;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RecoveringMovementMultiplayerSession;
import worldline.api.RemoteWorldView;
import worldline.api.RouteCorrectionPolicy;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves stop-on-correction ends a route without retry or later movement. */
public final class RoutePolicySmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|policy=stop-on-correction"
            + "|route=unchallenged-corrected|third=not-executed|retries=zero"
            + "|cache=preserved|final=persisted|disconnect=clean";
    private RoutePolicySmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: RoutePolicySmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        RecoveringMovementMultiplayerSession client =
                new B173WireClient("127.0.0.1", port, username, timeout);
        PlayerPose initial; MovementRouteResult route; RemoteWorldView after; ServerPlayerState player;
        try {
            server.boot(); B173PlayerSeed.write(workspace, username, 4.5D, 60D, 4.5D);
            client.connect(); awaitPlayers(server, Collections.singletonList(username));
            initial = client.synchronizePose(); int chunkX = floor(initial.x()) >> 4, chunkZ = floor(initial.z()) >> 4;
            client.awaitRemoteChunk(chunkX, chunkZ); RemoteWorldView before = worldline.test.WorldlineSmokeAwait.observe(client,5);
            PlayerPose accepted = new PlayerPose(initial.x() + .125D, initial.y(), initial.z(),
                    initial.yaw(), initial.pitch()); BlockPosition block = solid(before, accepted);
            route = client.moveRoute(Arrays.asList(new MovementStep(.125D, 0D, 0D, 5),
                    new MovementStep(block.x() + .5D - accepted.x(), block.y() - accepted.y(),
                            block.z() + .5D - accepted.z(), 10),
                    new MovementStep(.125D, 0D, 0D, 5)), RouteCorrectionPolicy.STOP_ON_CORRECTION);
            List<MovementOutcome> outcomes = route.outcomes();
            require(outcomes.size() == 2 && route.corrections() == 1, "route did not stop exactly once");
            require(outcomes.get(0).disposition() == MovementDisposition.UNCHALLENGED
                    && outcomes.get(0).resulting().equals(accepted), "accepted step drifted");
            require(outcomes.get(1).corrected() && route.finalPose().equals(accepted),
                    "corrected step did not stop at accepted pose");
            after = worldline.test.WorldlineSmokeAwait.observe(client,1); require(after.containsChunk(chunkX, chunkZ), "stopped route lost cache");
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            require(close(player.x(), accepted.x()) && close(player.y(), accepted.y())
                    && close(player.z(), accepted.z()), "unexecuted third step changed persisted pose");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M37_API=route-policy,stop-on-correction,no-retry,cache,persistence");
        System.out.println("WORLDLINE_M37_ROUTE=outcomes=" + route.outcomes().size() + ",corrections="
                + route.corrections() + ";" + pose(initial) + "->" + pose(route.finalPose()));
        System.out.println("WORLDLINE_M37_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M37_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M37_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M37_SIGNATURE=" + sha256(TRACE));
    }

    private static BlockPosition solid(RemoteWorldView world, PlayerPose pose) {
        int cx = floor(pose.x()), cy = floor(pose.y()), cz = floor(pose.z());
        for (int y = cy; y >= cy - 5; y--) for (int radius = 0; radius <= 4; radius++)
            for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) {
                if (!world.containsChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) continue;
                int id = world.blockAt(x, y, z).legacyId();
                if ((id >= 1 && id <= 5) || id == 7 || id == 12 || id == 13)
                    return new BlockPosition(x, y, z);
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
