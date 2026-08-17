package worldline.smoke.playerb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import worldline.api.MultiplayerSession;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves that a multiplayer login creates bounded persisted player state. */
public final class PlayerPersistenceSmoke {
    private static final String TRACE =
            "v1|login=accepted|logout=saved|dimension=0|health=20|inventory=empty|position=finite";

    private PlayerPersistenceSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: PlayerPersistenceSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String username = arguments[4];
        Duration timeout = Duration.ofSeconds(90);
        PersistentMultiplayerServerRuntime server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout);
        MultiplayerSession client = new B173WireClient("127.0.0.1", port, username, timeout);
        ServerPlayerState player;
        try {
            server.boot(); client.connect();
            awaitPlayers(server, Collections.singletonList(username), 5000L);
            client.close();
            awaitPlayers(server, Collections.emptyList(), 5000L);
            server.save();
            player = server.player(username);
            require(player.dimension() == 0 && player.health() == 20 && player.inventoryItems() == 0,
                    "persisted player fields drifted");
            require(Double.isFinite(player.x()) && Double.isFinite(player.y()) && Double.isFinite(player.z())
                    && player.y() > 0.0D, "persisted player position drifted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M23_API=login,logout,save,player-state");
        System.out.println("WORLDLINE_M23_SOURCE=" + B173DedicatedServer.class.getProtectionDomain()
                .getCodeSource().getLocation());
        System.out.println("WORLDLINE_M23_POSITION=" + player.x() + "," + player.y() + "," + player.z());
        System.out.println("WORLDLINE_M23_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M23_SIGNATURE=" + sha256(TRACE));
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
