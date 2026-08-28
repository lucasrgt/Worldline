package worldline.b173server;

import java.time.Duration;
import worldline.api.PlayerPose;

/** Owns fresh-login boundaries while preserving the measured cake collision lane. */
final class B173CakeServingSessions implements AutoCloseable {
    private final B173DedicatedServer server;
    private final int port;
    private final Duration timeout;
    private final PlayerPose origin;
    private B173WireClient client;
    private int reloads;

    B173CakeServingSessions(B173DedicatedServer server, int port, Duration timeout,
            B173CakeServingArena.Start start) {
        this.server = server; this.port = port; this.timeout = timeout;
        this.client = start.client; this.origin = start.origin;
    }

    B173WireClient client() { return client; }
    PlayerPose origin() { return origin; }
    int reloads() { return reloads; }

    void reload(int expectedHealth) {
        client.close();
        try {
            B173FixtureSupport.awaitPlayers(server, 0); server.save();
            client = new B173WireClient("127.0.0.1", port,
                    B173CakeServingArena.USERNAME, timeout);
            client.connect(); PlayerPose pose = client.synchronizePose(); client.awaitInventory();
            if (client.awaitHealth(expectedHealth) != expectedHealth) {
                throw new IllegalStateException("cake reloaded health drift");
            }
            client.awaitRemoteChunk(0, 0);
            B173CakeCollisionProbe.restore(client, origin, pose); reloads++;
        } catch (Exception error) {
            throw new IllegalStateException("cake fresh-login reload failed", error);
        }
    }

    @Override public void close() { client.close(); }
}
