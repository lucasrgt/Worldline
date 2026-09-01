package worldline.b173server;

import java.time.Duration;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteSignText;
import worldline.api.RemoteWorldView;

/** Owns the two clean fresh-login boundaries used by the sign subsystem proof. */
final class B173SignSubsystemSessions implements AutoCloseable {
    private final B173DedicatedServer server;
    private final int port;
    private final Duration timeout;
    private B173WireClient client;
    private PlayerPose pose;
    private int reloads;

    B173SignSubsystemSessions(B173DedicatedServer server, int port, Duration timeout,
            B173SignSubsystemArena.Start start) {
        this.server = server; this.port = port; this.timeout = timeout;
        this.client = start.client; this.pose = start.pose;
    }

    B173WireClient client() { return client; }
    PlayerPose pose() { return pose; }
    void pose(PlayerPose value) { pose = value; }
    int reloads() { return reloads; }

    Snapshot reload(RemoteSignText standing, RemoteSignText wall, boolean expectText) {
        client.close();
        try {
            B173FixtureSupport.awaitPlayers(server, 0); server.save();
            client = new B173WireClient("127.0.0.1", port,
                    B173SignSubsystemArena.USERNAME, timeout);
            client.connect(); pose = client.synchronizePose(); client.awaitInventory();
            RemoteWorldView world = client.awaitRemoteChunk(0, 0);
            RemoteSignText[] texts = expectText ? texts(client, standing, wall)
                    : new RemoteSignText[] {standing, wall};
            reloads++;
            return new Snapshot(state(world, B173SignSubsystemArena.STANDING),
                    state(world, B173SignSubsystemArena.WALL), texts[0], texts[1]);
        } catch (Exception error) {
            throw new IllegalStateException("sign fresh-login reload failed", error);
        }
    }

    private static RemoteSignText[] texts(B173WireClient client,
            RemoteSignText standing, RemoteSignText wall) {
        RemoteSignText foundStanding = null, foundWall = null;
        for (int attempt = 0; attempt < 16
                && (foundStanding == null || foundWall == null); attempt++) {
            RemoteSignText value = B173SignAccess.poll(client);
            if (value == null) value = B173SignAccess.await(client);
            if (standing.equals(value)) foundStanding = value;
            if (wall.equals(value)) foundWall = value;
        }
        if (foundStanding == null || foundWall == null) {
            throw new IllegalStateException("persisted sign text pair absent");
        }
        return new RemoteSignText[] {foundStanding, foundWall};
    }

    private static BlockState state(RemoteWorldView world,
            worldline.api.BlockPosition position) {
        return world.blockAt(position.x(), position.y(), position.z());
    }

    @Override public void close() { client.close(); }

    static final class Snapshot {
        final BlockState standing, wall;
        final RemoteSignText standingText, wallText;
        Snapshot(BlockState standing, BlockState wall,
                RemoteSignText standingText, RemoteSignText wallText) {
            this.standing = standing; this.wall = wall;
            this.standingText = standingText; this.wallText = wallText;
        }
    }
}
