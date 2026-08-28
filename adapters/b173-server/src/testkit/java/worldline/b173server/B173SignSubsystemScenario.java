package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.RemoteSignText;
import worldline.api.RemoteWorldView;
import worldline.testkit.SignSubsystemObservation;
import worldline.testkit.SignSubsystemScenario;

/** Official Beta 1.7.3 implementation of the public complete sign scenario. */
public final class B173SignSubsystemScenario implements SignSubsystemScenario {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState STANDING = new BlockState(63, 4);
    private static final BlockState WALL = new BlockState(68, 5);
    private final Path serverJar, workspace;
    private final int port;
    private final long seed;

    public B173SignSubsystemScenario(Path serverJar, Path workspace, int port, long seed) {
        this.serverJar = serverJar; this.workspace = workspace; this.port = port; this.seed = seed;
    }

    @Override public SignSubsystemObservation observe() {
        if (seed != B173SignSubsystemArena.SEED) {
            throw new IllegalArgumentException("sign subsystem seed drift");
        }
        Duration timeout = Duration.ofSeconds(120);
        B173DedicatedServer server = new B173DedicatedServer(
                serverJar, workspace, port, seed, timeout, 3, true);
        B173SignSubsystemSessions sessions = null;
        try {
            server.boot(); server.setTime(6_000L);
            B173SignSubsystemArena.Start start = B173SignSubsystemArena.open(
                    workspace, port, timeout);
            sessions = new B173SignSubsystemSessions(server, port, timeout, start);
            return execute(sessions, start.signs);
        } catch (Exception error) {
            throw error instanceof RuntimeException ? (RuntimeException) error
                    : new IllegalStateException("sign subsystem scenario failed", error);
        } finally {
            if (sessions != null) sessions.close();
            server.close();
        }
    }

    private static SignSubsystemObservation execute(
            B173SignSubsystemSessions sessions, int signsBefore) {
        B173WireClient client = sessions.client();
        B173SignDomainProbe.Result domain = B173SignDomainProbe.execute(client, signsBefore);
        client.selectHeldSlot(1); client.look(-90F, 0F);
        client.useHeldItemOnBlock(B173SignSubsystemArena.STANDING_SUPPORT, BlockFace.UP);
        verify(client.awaitBlock(B173SignSubsystemArena.STANDING, STANDING),
                B173SignSubsystemArena.STANDING, STANDING, "standing placement");
        client.selectHeldSlot(1); client.useHeldItemOnBlock(
                B173SignSubsystemArena.WALL_SUPPORT, BlockFace.EAST);
        verify(client.awaitBlock(B173SignSubsystemArena.WALL, WALL),
                B173SignSubsystemArena.WALL, WALL, "wall placement");
        RemoteSignText standingText = new RemoteSignText(B173SignSubsystemArena.STANDING,
                "Stand", "sign", "TestKit", "ok");
        RemoteSignText wallText = new RemoteSignText(B173SignSubsystemArena.WALL,
                "Wall", "sign", "TestKit", "ok");
        client.sustainTicks(10); B173SignAccess.update(client, standingText);
        client.sustainTicks(10); B173SignAccess.update(client, wallText); client.sustainTicks(20);
        B173SignSubsystemSessions.Snapshot persisted = sessions.reload(
                standingText, wallText, true); client = sessions.client();
        B173SignCollisionProbe.Result standingCollision =
                B173SignCollisionProbe.standing(client, sessions.pose());
        B173SignCollisionProbe.Result wallCollision =
                B173SignCollisionProbe.wall(client, standingCollision.pose);
        sessions.pose(wallCollision.pose);
        RemoteWorldView light = client.sustainTicks(1);
        int standingBlockLight = light.blockLightAt(4, 72, 4);
        int wallBlockLight = light.blockLightAt(5, 72, 5);
        int standingSkyLight = light.skyLightAt(4, 72, 4);
        int wallSkyLight = light.skyLightAt(5, 72, 5);
        RemoteWorldView ticked = client.sustainTicks(240);
        BlockState tickStanding = state(ticked, B173SignSubsystemArena.STANDING);
        BlockState tickWall = state(ticked, B173SignSubsystemArena.WALL);
        breakSupport(client, B173SignSubsystemArena.STANDING_SUPPORT);
        breakSupport(client, B173SignSubsystemArena.WALL_SUPPORT);
        RemoteWorldView unsupported = client.sustainTicks(40);
        BlockState unsupportedStanding = state(unsupported, B173SignSubsystemArena.STANDING);
        BlockState unsupportedWall = state(unsupported, B173SignSubsystemArena.WALL);
        B173SignSubsystemSessions.Snapshot ended = sessions.reload(
                standingText, wallText, false);
        return new SignSubsystemObservation(domain.metadata, STANDING, WALL,
                domain.before, domain.afterFirst, domain.brokenFrom, domain.brokenTo, domain.drop,
                standingText, wallText, persisted.standing, persisted.wall,
                persisted.standingText, persisted.wallText,
                Arrays.asList(standingCollision.disposition, wallCollision.disposition),
                Arrays.asList(standingBlockLight, wallBlockLight),
                Arrays.asList(standingSkyLight, wallSkyLight), 240, tickStanding, tickWall,
                unsupportedStanding, unsupportedWall, ended.standing, ended.wall,
                ReloadBoundary.FRESH_LOGIN, sessions.reloads());
    }

    private static void breakSupport(B173WireClient client,
            worldline.api.BlockPosition position) {
        client.selectHeldSlot(2); client.beginBreak(position);
        client.sustainTicks(20); client.finishBreak(position);
        client.awaitBlock(position, AIR); client.sustainTicks(20);
    }

    private static BlockState state(RemoteWorldView world,
            worldline.api.BlockPosition position) {
        return world.blockAt(position.x(), position.y(), position.z());
    }
    private static void verify(RemoteWorldView world, worldline.api.BlockPosition position,
            BlockState expected, String phase) {
        if (world == null || !state(world, position).equals(expected)) {
            throw new IllegalStateException(phase + " state drifted");
        }
    }
}
