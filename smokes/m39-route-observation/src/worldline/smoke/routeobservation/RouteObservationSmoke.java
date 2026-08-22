package worldline.smoke.routeobservation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.MovementAlternative;
import worldline.api.MovementAttemptKind;
import worldline.api.MovementRouteEvent;
import worldline.api.MovementRouteResult;
import worldline.api.MovementStep;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RecoveringMovementMultiplayerSession;
import worldline.api.RemoteWorldView;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves route outcomes are observed synchronously with stable indexes. */
public final class RouteObservationSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|observer=synchronous-caller-thread"
            + "|events=0:0:primary,1:1:primary,1:2:fallback|outcomes=identity-bound"
            + "|cache=preserved|final=persisted|disconnect=clean";
    private RouteObservationSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: RouteObservationSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        RecoveringMovementMultiplayerSession client =
                new B173WireClient("127.0.0.1", port, username, timeout);
        List<MovementRouteEvent> events = new ArrayList<>(); Thread caller = Thread.currentThread();
        MovementRouteResult route; RemoteWorldView after; ServerPlayerState player; PlayerPose initial;
        try {
            server.boot(); client.connect(); awaitPlayers(server, Collections.singletonList(username));
            initial = client.synchronizePose(); int chunkX = floor(initial.x()) >> 4, chunkZ = floor(initial.z()) >> 4;
            client.awaitRemoteChunk(chunkX, chunkZ); RemoteWorldView before = client.sustainTicks(5);
            PlayerPose accepted = new PlayerPose(initial.x() + .125D, initial.y(), initial.z(),
                    initial.yaw(), initial.pitch()); BlockPosition block = solid(before, accepted);
            MovementAlternative safe = new MovementAlternative(new MovementStep(.125D, 0D, 0D, 5),
                    new MovementStep(0D, 0D, .125D, 5));
            MovementAlternative blocked = new MovementAlternative(new MovementStep(
                    block.x() + .5D - accepted.x(), block.y() - accepted.y(),
                    block.z() + .5D - accepted.z(), 10), new MovementStep(.125D, 0D, 0D, 5));
            route = client.moveRouteWithFallback(Arrays.asList(safe, blocked), event -> {
                require(Thread.currentThread() == caller, "route observer changed thread");
                require(event.outcomeIndex() == events.size(), "route event was not immediate"); events.add(event); });
            require(events.size() == 3 && event(events.get(0), 0, 0, MovementAttemptKind.PRIMARY)
                    && event(events.get(1), 1, 1, MovementAttemptKind.PRIMARY)
                    && event(events.get(2), 1, 2, MovementAttemptKind.FALLBACK), "route event order drifted");
            for (int index = 0; index < events.size(); index++) require(
                    events.get(index).outcome() == route.outcomes().get(index), "event outcome identity drifted");
            after = client.sustainTicks(1); require(after.containsChunk(chunkX, chunkZ), "observed route lost cache");
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            require(close(player.x(), route.finalPose().x()) && close(player.y(), route.finalPose().y())
                    && close(player.z(), route.finalPose().z()), "observed route pose was not persisted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M39_API=route-observer,synchronous,indexed,identity-bound,cache,persistence");
        System.out.println("WORLDLINE_M39_EVENTS=" + describe(events) + ";final=" + pose(route.finalPose()));
        System.out.println("WORLDLINE_M39_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M39_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M39_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M39_SIGNATURE=" + sha256(TRACE));
    }

    private static boolean event(MovementRouteEvent event, int alternative, int outcome,
            MovementAttemptKind kind) { return event.alternativeIndex() == alternative
                && event.outcomeIndex() == outcome && event.kind() == kind; }
    private static String describe(List<MovementRouteEvent> events) { StringBuilder value = new StringBuilder();
        for (MovementRouteEvent event : events) { if (value.length() > 0) value.append(','); value.append(
                event.alternativeIndex()).append(':').append(event.outcomeIndex()).append(':').append(event.kind()); }
        return value.toString(); }
    private static BlockPosition solid(RemoteWorldView world, PlayerPose pose) {
        int cx = floor(pose.x()), cy = floor(pose.y()), cz = floor(pose.z());
        for (int y = cy; y >= 0; y--) for (int radius = 0; radius <= 4; radius++)
            for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) {
                if (!world.containsChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) continue;
                int id = world.blockAt(x, y, z).legacyId();
                if ((id >= 1 && id <= 5) || id == 7 || id == 12 || id == 13) return new BlockPosition(x, y, z);
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
