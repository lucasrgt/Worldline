package worldline.smoke.routecorrelation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.CorrelatedMovementRouteEvent;
import worldline.api.CorrelatedMovementRouteExecution;
import worldline.api.MovementAlternative;
import worldline.api.MovementAttemptKind;
import worldline.api.MovementRouteDirective;
import worldline.api.MovementRouteTermination;
import worldline.api.MovementStep;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RecoveringMovementMultiplayerSession;
import worldline.api.RemoteWorldView;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves caller-owned correlation identity across live route events and summary. */
public final class RouteCorrelationSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|correlation=caller-owned-identity"
            + "|events=0:0:primary|terminal=0:0:primary|termination=controller-stop|later=absent"
            + "|registry=absent|cache=preserved|final=persisted|disconnect=clean";
    private RouteCorrelationSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: RouteCorrelationSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        RecoveringMovementMultiplayerSession client =
                new B173WireClient("127.0.0.1", port, username, timeout);
        Object correlation = new Object(); List<CorrelatedMovementRouteEvent> events = new ArrayList<>();
        CorrelatedMovementRouteExecution execution; RemoteWorldView after; ServerPlayerState player;
        try {
            server.boot(); client.connect(); awaitPlayers(server, Collections.singletonList(username));
            PlayerPose initial = client.synchronizePose(); int chunkX = floor(initial.x()) >> 4;
            int chunkZ = floor(initial.z()) >> 4; client.awaitRemoteChunk(chunkX, chunkZ);
            client.sustainTicks(5); MovementAlternative safe = alternative(.125D, 0D, 0D);
            MovementAlternative later = alternative(0D, 0D, .125D);
            execution = client.moveRouteWithFallbackCorrelated(
                    java.util.Arrays.asList(safe, later), correlation, event -> {
                        require(event.correlation() == correlation, "correlation identity drifted");
                        require(event.event().outcomeIndex() == events.size(), "correlated event was not immediate");
                        events.add(event); return MovementRouteDirective.STOP; });
            require(events.size() == 1 && execution.correlation() == correlation
                    && execution.execution().termination() == MovementRouteTermination.CONTROLLER_STOP,
                    "correlated execution summary drifted");
            CorrelatedMovementRouteEvent terminal = events.get(0);
            require(execution.terminalEvent() == terminal && terminal.event().alternativeIndex() == 0
                    && terminal.event().outcomeIndex() == 0 && terminal.event().kind() == MovementAttemptKind.PRIMARY
                    && terminal.event() == execution.execution().terminalEvent(), "correlated terminal event drifted");
            after = client.sustainTicks(1); require(after.containsChunk(chunkX, chunkZ), "correlated route lost cache");
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            PlayerPose finalPose = execution.execution().result().finalPose(); require(close(player.x(), finalPose.x())
                    && close(player.y(), finalPose.y()) && close(player.z(), finalPose.z()),
                    "correlated route pose was not persisted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M42_API=opaque-correlation,identity,event,terminal-summary,no-registry");
        System.out.println("WORLDLINE_M42_EVENTS=" + describe(events) + ";termination="
                + execution.execution().termination());
        System.out.println("WORLDLINE_M42_CACHE=chunks=" + after.chunks().size());
        System.out.println("WORLDLINE_M42_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M42_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M42_SIGNATURE=" + sha256(TRACE));
    }

    private static MovementAlternative alternative(double x, double y, double z) {
        return new MovementAlternative(new MovementStep(x, y, z, 5), new MovementStep(-x, -y, -z, 5)); }
    private static String describe(List<CorrelatedMovementRouteEvent> events) { StringBuilder value = new StringBuilder();
        for (CorrelatedMovementRouteEvent correlated : events) { if (value.length() > 0) value.append(',');
            value.append(correlated.event().alternativeIndex()).append(':')
                    .append(correlated.event().outcomeIndex()).append(':').append(correlated.event().kind()); }
        return value.toString(); }
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
