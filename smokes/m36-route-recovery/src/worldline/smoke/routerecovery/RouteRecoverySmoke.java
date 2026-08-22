package worldline.smoke.routerecovery;

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
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves a relative route continues from a server-corrected pose. */
public final class RouteRecoverySmoke {
    private static final String TRACE = "v1|server=official-b1.7.3"
            + "|route=unchallenged-corrected-unchallenged|recovery=authoritative-relative"
            + "|cache=preserved|final=persisted|disconnect=clean";
    private RouteRecoverySmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: RouteRecoverySmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        RecoveringMovementMultiplayerSession client =
                new B173WireClient("127.0.0.1", port, username, timeout);
        PlayerPose initial; MovementRouteResult route; RemoteWorldView after; ServerPlayerState player;
        try {
            server.boot(); client.connect(); awaitPlayers(server, Collections.singletonList(username));
            initial = client.synchronizePose(); int chunkX = floor(initial.x()) >> 4, chunkZ = floor(initial.z()) >> 4;
            client.awaitRemoteChunk(chunkX, chunkZ); RemoteWorldView before = client.sustainTicks(5);
            PlayerPose predicted = new PlayerPose(initial.x() + .125D, initial.y(), initial.z(),
                    initial.yaw(), initial.pitch()); BlockPosition block = solid(before, predicted);
            route = client.moveRoute(Arrays.asList(new MovementStep(.125D, 0D, 0D, 5),
                    new MovementStep(block.x() + .5D - predicted.x(), block.y() - predicted.y(),
                            block.z() + .5D - predicted.z(), 10),
                    new MovementStep(.125D, 0D, 0D, 5)));
            List<MovementOutcome> outcomes = route.outcomes();
            require(outcomes.get(0).disposition() == MovementDisposition.UNCHALLENGED
                    && outcomes.get(0).resulting().equals(predicted), "route first step drifted");
            require(outcomes.get(1).corrected() && outcomes.get(1).resulting().equals(predicted),
                    "route collision did not restore accepted pose: " + pose(predicted) + " / "
                            + pose(outcomes.get(1).attempted()) + " / " + pose(outcomes.get(1).resulting()));
            require(outcomes.get(2).disposition() == MovementDisposition.UNCHALLENGED
                    && close(route.finalPose().x(), predicted.x() + .125D), "route did not recover after correction");
            require(route.corrections() == 1, "route correction count drifted");
            after = client.sustainTicks(1); require(after.containsChunk(chunkX, chunkZ), "route lost cached chunk");
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            require(close(player.x(), route.finalPose().x()) && close(player.y(), route.finalPose().y())
                    && close(player.z(), route.finalPose().z()), "recovered route pose was not persisted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M36_API=movement-route,ordered-outcomes,correction-recovery,cache,persistence");
        System.out.println("WORLDLINE_M36_ROUTE=" + dispositions(route) + ";" + pose(initial)
                + "->" + pose(route.outcomes().get(0).resulting()) + "->"
                + pose(route.outcomes().get(1).attempted()) + "->" + pose(route.finalPose()));
        System.out.println("WORLDLINE_M36_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M36_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M36_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M36_SIGNATURE=" + sha256(TRACE));
    }

    private static BlockPosition solid(RemoteWorldView world, PlayerPose pose) {
        int cx = floor(pose.x()), cy = floor(pose.y()), cz = floor(pose.z());
        for (int y = cy; y >= 0; y--) for (int radius = 0; radius <= 4; radius++)
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
    private static String dispositions(MovementRouteResult route) { StringBuilder value = new StringBuilder();
        for (MovementOutcome outcome : route.outcomes()) { if (value.length() > 0) value.append(',');
            value.append(outcome.disposition()); } return value.toString(); }
    private static int floor(double value) { return (int) Math.floor(value); }
    private static boolean close(double a, double b) { return Math.abs(a - b) < .000001D; }
    private static String pose(PlayerPose value) { return value.x() + "," + value.y() + "," + value.z(); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
