package worldline.smoke.observercontrol;

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
import worldline.api.MovementRouteDirective;
import worldline.api.MovementRouteEvent;
import worldline.api.MovementRouteResult;
import worldline.api.MovementStep;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RecoveringMovementMultiplayerSession;
import worldline.api.RemoteWorldView;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves a synchronous event decision stops later caller-supplied movement. */
public final class ObserverControlSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|controller=synchronous-caller-thread"
            + "|events=0:0:primary,0:1:fallback|directive=stop-after-fallback|later=absent"
            + "|outcomes=identity-bound|cache=preserved|final=persisted|disconnect=clean";
    private ObserverControlSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: ObserverControlSmoke server.jar workspace port seed username");
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
            server.boot(); B173PlayerSeed.write(workspace, username, 4.5D, 60D, 4.5D);
            client.connect(); awaitPlayers(server, Collections.singletonList(username));
            initial = client.synchronizePose(); int chunkX = floor(initial.x()) >> 4, chunkZ = floor(initial.z()) >> 4;
            client.awaitRemoteChunk(chunkX, chunkZ); RemoteWorldView before = worldline.test.WorldlineSmokeAwait.observe(client,5);
            BlockPosition block = solid(before, initial);
            MovementAlternative blocked = new MovementAlternative(new MovementStep(
                    block.x() + .5D - initial.x(), block.y() - initial.y(),
                    block.z() + .5D - initial.z(), 10), new MovementStep(.125D, 0D, 0D, 5));
            MovementAlternative later = new MovementAlternative(new MovementStep(0D, 0D, .125D, 5),
                    new MovementStep(-.125D, 0D, 0D, 5));
            route = client.moveRouteWithFallbackUntil(Arrays.asList(blocked, later), event -> {
                require(Thread.currentThread() == caller, "route controller changed thread");
                require(event.outcomeIndex() == events.size(), "controlled event was not immediate");
                events.add(event); return event.kind() == MovementAttemptKind.FALLBACK
                        ? MovementRouteDirective.STOP : MovementRouteDirective.CONTINUE;
            });
            require(events.size() == 2 && event(events.get(0), 0, 0, MovementAttemptKind.PRIMARY)
                    && event(events.get(1), 0, 1, MovementAttemptKind.FALLBACK), "controlled event order drifted");
            require(route.outcomes().size() == 2 && route.corrections() == 1,
                    "observer stop did not exclude later alternative");
            for (int index = 0; index < events.size(); index++) require(
                    events.get(index).outcome() == route.outcomes().get(index), "event outcome identity drifted");
            after = worldline.test.WorldlineSmokeAwait.observe(client,1); require(after.containsChunk(chunkX, chunkZ), "controlled route lost cache");
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            require(close(player.x(), route.finalPose().x()) && close(player.y(), route.finalPose().y())
                    && close(player.z(), route.finalPose().z()), "controlled route pose was not persisted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M40_API=route-controller,synchronous,directive,stop,identity-bound,cache,persistence");
        System.out.println("WORLDLINE_M40_EVENTS=" + describe(events) + ";outcomes=" + route.outcomes().size());
        System.out.println("WORLDLINE_M40_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M40_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M40_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M40_SIGNATURE=" + sha256(TRACE));
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
        for (int y = cy; y >= cy - 5; y--) for (int radius = 0; radius <= 4; radius++)
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
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
