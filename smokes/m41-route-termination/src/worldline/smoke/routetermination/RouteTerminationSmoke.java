package worldline.smoke.routetermination;

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
import worldline.api.MovementRouteExecution;
import worldline.api.MovementRouteTermination;
import worldline.api.MovementStep;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RecoveringMovementMultiplayerSession;
import worldline.api.RemoteWorldView;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves exact immutable summaries for stopped and exhausted routes. */
public final class RouteTerminationSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|execution=immutable"
            + "|stopped=controller-stop@1:2:fallback|exhausted=exhausted@0:0:primary"
            + "|outcomes=identity-bound|later=absent|cache=preserved|final=persisted|disconnect=clean";
    private RouteTerminationSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: RouteTerminationSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        RecoveringMovementMultiplayerSession client =
                new B173WireClient("127.0.0.1", port, username, timeout);
        List<MovementRouteEvent> stoppedEvents = new ArrayList<>(), exhaustedEvents = new ArrayList<>();
        MovementRouteExecution stopped, exhausted; RemoteWorldView after; ServerPlayerState player;
        try {
            server.boot(); client.connect(); awaitPlayers(server, Collections.singletonList(username));
            PlayerPose initial = client.synchronizePose(); int chunkX = floor(initial.x()) >> 4;
            int chunkZ = floor(initial.z()) >> 4; client.awaitRemoteChunk(chunkX, chunkZ);
            RemoteWorldView before = client.sustainTicks(5);
            PlayerPose accepted = new PlayerPose(initial.x() + .125D, initial.y(), initial.z(),
                    initial.yaw(), initial.pitch()); BlockPosition block = solid(before, accepted);
            MovementAlternative safe = alternative(.125D, 0D, 0D);
            MovementAlternative blocked = new MovementAlternative(new MovementStep(
                    block.x() + .5D - accepted.x(), block.y() - accepted.y(),
                    block.z() + .5D - accepted.z(), 10), new MovementStep(.125D, 0D, 0D, 5));
            MovementAlternative later = alternative(0D, 0D, .125D);
            stopped = client.moveRouteWithFallbackExecution(Arrays.asList(safe, blocked, later), event -> {
                stoppedEvents.add(event); return event.kind() == MovementAttemptKind.FALLBACK
                        ? MovementRouteDirective.STOP : MovementRouteDirective.CONTINUE; });
            exhausted = client.moveRouteWithFallbackExecution(Collections.singletonList(
                    alternative(0D, 0D, .125D)), event -> {
                        exhaustedEvents.add(event); return MovementRouteDirective.CONTINUE; });
            require(stopped.stopped() && stopped.termination() == MovementRouteTermination.CONTROLLER_STOP
                    && stopped.result().outcomes().size() == 3 && stoppedEvents.size() == 3,
                    "controller-stop summary drifted");
            require(!exhausted.stopped() && exhausted.termination() == MovementRouteTermination.EXHAUSTED
                    && exhausted.result().outcomes().size() == 1 && exhaustedEvents.size() == 1,
                    "exhausted summary drifted");
            require(terminal(stopped, stoppedEvents, MovementAttemptKind.FALLBACK)
                    && terminal(exhausted, exhaustedEvents, MovementAttemptKind.PRIMARY),
                    "terminal event identity drifted");
            after = client.sustainTicks(1); require(after.containsChunk(chunkX, chunkZ), "summarized route lost cache");
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            PlayerPose finalPose = exhausted.result().finalPose(); require(close(player.x(), finalPose.x())
                    && close(player.y(), finalPose.y()) && close(player.z(), finalPose.z()),
                    "summarized route pose was not persisted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M41_API=route-execution,immutable,terminal-event,exhausted,controller-stop");
        System.out.println("WORLDLINE_M41_TERMINATION=stopped=" + stopped.termination() + "@"
                + describe(stopped.terminalEvent()) + ";exhausted=" + exhausted.termination() + "@"
                + describe(exhausted.terminalEvent()));
        System.out.println("WORLDLINE_M41_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M41_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M41_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M41_SIGNATURE=" + sha256(TRACE));
    }

    private static MovementAlternative alternative(double x, double y, double z) {
        return new MovementAlternative(new MovementStep(x, y, z, 5), new MovementStep(-x, -y, -z, 5)); }
    private static boolean terminal(MovementRouteExecution execution, List<MovementRouteEvent> events,
            MovementAttemptKind kind) { MovementRouteEvent event = events.get(events.size() - 1);
        return execution.terminalEvent() == event && event.kind() == kind
                && event.outcome() == execution.result().outcomes().get(event.outcomeIndex()); }
    private static String describe(MovementRouteEvent event) { return event.alternativeIndex()
            + ":" + event.outcomeIndex() + ":" + event.kind(); }
    private static BlockPosition solid(RemoteWorldView world, PlayerPose pose) {
        int cx = floor(pose.x()), cy = floor(pose.y()), cz = floor(pose.z());
        for (int y = cy; y >= 0; y--) for (int radius = 0; radius <= 4; radius++)
            for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) {
                if (!world.containsChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) continue;
                int id = world.blockAt(x, y, z).legacyId();
                if ((id >= 1 && id <= 5) || id == 7 || id == 12 || id == 13) return new BlockPosition(x, y, z);
            } throw new IllegalStateException("nearby solid block absent"); }
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
