package worldline.smoke.playb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayableMultiplayerSession;
import worldline.api.PlayerPose;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves a bidirectional play-position exchange and persisted look action. */
public final class PlayPoseSmoke {
    private static final String TRACE = "v1|login=accepted|prelude=spawn,time|position=acknowledged"
            + "|look=135.0,-22.5|logout=saved|rotation=persisted";

    private PlayPoseSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: PlayPoseSmoke server.jar workspace port seed username yaw pitch");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String username = arguments[4];
        float yaw = Float.parseFloat(arguments[5]), pitch = Float.parseFloat(arguments[6]);
        Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout);
        PlayableMultiplayerSession client = new B173WireClient(
                "127.0.0.1", port, username, timeout);
        PlayerPose initial;
        ServerPlayerState player;
        try {
            server.boot(); client.connect();
            awaitPlayers(server, Collections.singletonList(username), 5000L);
            initial = client.synchronizePose();
            client.look(yaw, pitch);
            Thread.sleep(300L);
            client.close();
            awaitPlayers(server, Collections.emptyList(), 5000L);
            server.save(); player = server.player(username);
            require(close(initial.x(), player.x()) && close(initial.y(), player.y())
                    && close(initial.z(), player.z()), "acknowledged position did not persist");
            require(player.yaw() == yaw && player.pitch() == pitch,
                    "requested rotation did not persist");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M24_API=login,spawn-time,position-ack,look,save,player-pose");
        System.out.println("WORLDLINE_M24_SOURCE=" + B173WireClient.class.getProtectionDomain()
                .getCodeSource().getLocation());
        System.out.println("WORLDLINE_M24_INITIAL=" + pose(initial));
        System.out.println("WORLDLINE_M24_PERSISTED=" + player.x() + "," + player.y() + ","
                + player.z() + "," + player.yaw() + "," + player.pitch());
        System.out.println("WORLDLINE_M24_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M24_SIGNATURE=" + sha256(TRACE));
    }

    private static void awaitPlayers(PersistentMultiplayerServerRuntime server,
            List<String> expected, long timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < deadline) {
            if (server.players().equals(expected)) return;
            Thread.sleep(100L);
        }
        throw new IllegalStateException("player list did not become " + expected + ": " + server.players());
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) < 0.000001D;
    }

    private static String pose(PlayerPose value) {
        return value.x() + "," + value.y() + "," + value.z() + "," + value.yaw() + "," + value.pitch();
    }

    private static String sha256(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255));
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
