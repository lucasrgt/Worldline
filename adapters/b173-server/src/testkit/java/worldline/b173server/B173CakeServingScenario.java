package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;
import worldline.api.RemoteWorldView;
import worldline.testkit.CakeServingObservation;
import worldline.testkit.CakeServingScenario;

/** Official Beta 1.7.3 implementation of the public complete cake-serving scenario. */
public final class B173CakeServingScenario implements CakeServingScenario {
    private static final BlockState AIR = new BlockState(0, 0);
    private final Path serverJar, workspace;
    private final int port;
    private final long seed;

    public B173CakeServingScenario(Path serverJar, Path workspace, int port, long seed) {
        this.serverJar = serverJar; this.workspace = workspace; this.port = port; this.seed = seed;
    }

    @Override public CakeServingObservation observe() {
        if (seed != B173CakeServingArena.SEED) {
            throw new IllegalArgumentException("cake serving seed drift");
        }
        Duration timeout = Duration.ofSeconds(120);
        B173DedicatedServer server = B173DedicatedServer.difficulty(
                serverJar, workspace, port, seed, timeout, 1);
        B173CakeServingSessions sessions = null;
        try {
            server.boot(); server.setTime(6_000L);
            sessions = new B173CakeServingSessions(server, port, timeout,
                    B173CakeServingArena.open(workspace, port, timeout));
            return execute(sessions);
        } catch (Exception error) {
            throw error instanceof RuntimeException ? (RuntimeException) error
                    : new IllegalStateException("cake serving scenario failed", error);
        } finally {
            if (sessions != null) sessions.close();
            server.close();
        }
    }

    private static CakeServingObservation execute(B173CakeServingSessions sessions) {
        B173WireClient client = sessions.client();
        List<BlockState> states = new ArrayList<BlockState>();
        List<Integer> health = new ArrayList<Integer>();
        List<MovementDisposition> collisions = new ArrayList<MovementDisposition>();
        List<Integer> blockLight = new ArrayList<Integer>();
        List<Integer> skyLight = new ArrayList<Integer>();
        client.selectHeldSlot(1); client.useHeldItemOnBlock(
                B173CakeServingArena.SUPPORT, BlockFace.UP);
        client.selectHeldSlot(3);
        for (int metadata = 0; metadata <= 5; metadata++) {
            BlockState expected = new BlockState(92, metadata);
            verify(client.awaitBlock(B173CakeServingArena.TARGET, expected), expected,
                    "serving " + metadata);
            int expectedHealth = 1 + metadata * 3;
            if (client.health() != expectedHealth && client.awaitHealth(expectedHealth)
                    != expectedHealth) throw new IllegalStateException("cake health drift");
            states.add(expected); health.add(expectedHealth);
            collisions.add(B173CakeCollisionProbe.sample(client, sessions.origin()));
            RemoteWorldView light = client.sustainTicks(1);
            blockLight.add(light.blockLightAt(4, 72, 4));
            skyLight.add(light.skyLightAt(4, 72, 4));
            if (metadata == 3) {
                client.sustainTicks(200);
                verify(client.awaitBlock(B173CakeServingArena.TARGET, expected), expected,
                        "idle cake");
                sessions.reload(expectedHealth); client = sessions.client();
                client.selectHeldSlot(3);
                verify(client.awaitBlock(B173CakeServingArena.TARGET, expected), expected,
                        "reloaded partial cake");
            }
            if (metadata < 5) {
                client.activateBlock(B173CakeServingArena.TARGET, BlockFace.UP);
            }
        }
        client.activateBlock(B173CakeServingArena.TARGET, BlockFace.UP);
        verify(client.awaitBlock(B173CakeServingArena.TARGET, AIR), AIR, "sixth serving");
        if (client.awaitHealth(19) != 19) throw new IllegalStateException("final cake health drift");
        states.add(AIR); health.add(19);
        client.selectHeldSlot(1); client.useHeldItemOnBlock(
                B173CakeServingArena.SUPPORT, BlockFace.UP);
        BlockState supported = new BlockState(92, 0);
        verify(client.awaitBlock(B173CakeServingArena.TARGET, supported), supported,
                "supported second cake");
        client.selectHeldSlot(2); client.beginBreak(B173CakeServingArena.SUPPORT);
        client.sustainTicks(20); client.finishBreak(B173CakeServingArena.SUPPORT);
        verify(client.awaitBlock(B173CakeServingArena.SUPPORT, AIR),
                B173CakeServingArena.SUPPORT, AIR, "removed support");
        client.sustainTicks(40);
        verify(client.awaitBlock(B173CakeServingArena.TARGET, AIR), AIR, "unsupported cake");
        sessions.reload(19); client = sessions.client();
        verify(client.awaitBlock(B173CakeServingArena.TARGET, AIR), AIR, "persisted final air");
        return new CakeServingObservation(states, health, collisions, 0, 1_000,
                blockLight, skyLight, 200, new BlockState(92, 3), new BlockState(92, 3),
                new BlockState(92, 3), supported, AIR, AIR,
                ReloadBoundary.FRESH_LOGIN, sessions.reloads());
    }

    private static void verify(RemoteWorldView world, BlockState expected, String phase) {
        verify(world, B173CakeServingArena.TARGET, expected, phase);
    }

    private static void verify(RemoteWorldView world, worldline.api.BlockPosition position,
            BlockState expected, String phase) {
        if (world == null || !world.blockAt(position.x(), position.y(), position.z())
                .equals(expected)) {
            throw new IllegalStateException(phase + " state drifted");
        }
    }
}
