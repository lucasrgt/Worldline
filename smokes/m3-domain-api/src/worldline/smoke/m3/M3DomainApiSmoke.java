package worldline.smoke.m3;

import java.nio.file.Paths;
import java.util.List;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GamePlayer;
import worldline.api.GamePosition;
import worldline.api.GameWorld;
import worldline.api.WorldSource;
import worldline.b173.B173Runtimes;
import worldline.trace.CanonicalStateTrace;

/** Exercises M3 strictly through neutral public domain contracts. */
public final class M3DomainApiSmoke {
    private static final long SEED = 17320110707L;
    private static final BlockPosition STONE = new BlockPosition(8, 64, 8);
    private static final BlockPosition TARGET = new BlockPosition(8, 65, 8);

    private M3DomainApiSmoke() {}

    public static void main(String[] arguments) {
        AutomatedMinecraftRuntime runtime = B173Runtimes.create(SEED);
        Object[] handles = new Object[2];
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "m3-domain-api")));
            GameWorld world = runtime.world();
            GamePlayer player = runtime.player();
            handles[0] = world; handles[1] = player;
            require(world.block(STONE).equals(new BlockState(1, 0)), "stone selector failed");
            require(world.block(TARGET).equals(new BlockState(0, 0)), "target is not air");
            require(player.type().equals("minecraft:player") && player.username().equals("Worldline"),
                    "player semantic identity failed");
            assertEntityCollection(world.entities(), player);
            CanonicalStateTrace trace = trace();
            record(trace, "loaded", world, player);
            require(world.setBlock(TARGET, new BlockState(20, 3)), "block mutation failed");
            player.teleport(new GamePosition(10.5D, 66.0D, 10.5D));
            player.selectHotbarSlot(4);
            record(trace, "mutated", world, player);
            runtime.tick(3);
            record(trace, "tick3", world, player);
            require(world.time() == 3L && player.selectedHotbarSlot() == 4,
                    "domain state did not survive controlled ticks");
            System.out.println("WORLDLINE_M3_SOURCE=" + minecraftClassSource());
            System.out.println("WORLDLINE_M3_TRACE=" + trace.value());
            System.out.println("WORLDLINE_M3_SIGNATURE=" + trace.signature());
            System.out.println("WORLDLINE_M3_API=world,block,entity,player");
        } finally { runtime.close(); }
        expectClosed(() -> ((GameWorld) handles[0]).time());
        expectClosed(() -> ((GamePlayer) handles[1]).health());
    }

    private static CanonicalStateTrace trace() {
        return new CanonicalStateTrace(SEED, "time", "block64", "meta64", "block65", "meta65",
                "entities", "playerId", "alive", "x", "y", "z", "health", "slot");
    }

    private static void record(CanonicalStateTrace trace, String label, GameWorld world,
            GamePlayer player) {
        BlockState stone = world.block(STONE), target = world.block(TARGET);
        GamePosition position = player.position();
        trace.record(label, world.time(), stone.legacyId(), stone.metadata(), target.legacyId(),
                target.metadata(), world.entities().size(), player.id(), player.alive() ? 1 : 0,
                Double.doubleToLongBits(position.x()), Double.doubleToLongBits(position.y()),
                Double.doubleToLongBits(position.z()), player.health(), player.selectedHotbarSlot());
    }

    private static void assertEntityCollection(List<GameEntity> entities, GamePlayer player) {
        require(entities.size() == 1 && entities.get(0).id() == player.id(),
                "world entity collection does not contain the player");
        try { entities.clear(); throw new IllegalStateException("entity collection is mutable"); }
        catch (UnsupportedOperationException expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String minecraftClassSource() {
        try {
            return Class.forName("net.minecraft.client.Minecraft").getProtectionDomain()
                    .getCodeSource().getLocation().toString();
        } catch (ClassNotFoundException error) { throw new IllegalStateException(error); }
    }

    private static void expectClosed(Runnable action) {
        try { action.run(); throw new IllegalStateException("domain handle survived runtime close"); }
        catch (IllegalStateException expected) {
            require(expected.getMessage().contains("client is not booted"), "unexpected closed-handle failure");
        }
    }
}
