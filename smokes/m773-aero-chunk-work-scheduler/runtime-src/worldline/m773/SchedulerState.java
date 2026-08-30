package worldline.m773;

import aero.modellib.test.WorldlineM773Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Drives template preparation, retained invalidation phases, and full drainage. */
public final class SchedulerState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m773.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m773.prepare");
    private static final String ARM = System.getProperty("worldline.m773.arm", "prepare");
    private static final int REQUIRED_FRAMES = Integer.getInteger("worldline.m773.frames", 600);
    private static final long MINIMUM_MILLIS =
        Long.getLong("worldline.m773.minimumMillis", 15000L);
    private static final int PRIORITY_FRAMES = 12;
    private static final int STRESS_FRAMES = 300;
    private static final int STABLE_FRAMES = 20;
    private static int stage, warmup, stable, retained, maxBacklog, machines;
    private static long retainedStarted;

    private SchedulerState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM773] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return;
        }
        if (stage != 1 || game.world == null || game.player == null) return;
        warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        retained++;
        boolean moving = retained > PRIORITY_FRAMES;
        SchedulerScene.place(game.player, retained, moving);
        if (retained == 1) {
            SchedulerScene.select(game);
            retainedStarted = System.nanoTime();
            System.out.println("[WorldlineM773] retained-start arm=" + ARM);
        }
        int backlog = backlog(game);
        if (backlog > maxBacklog) maxBacklog = backlog;
        if (retained <= PRIORITY_FRAMES) {
            SchedulerScene.priority(game, retained);
            stable = 0;
        } else if (retained <= PRIORITY_FRAMES + STRESS_FRAMES) {
            SchedulerScene.stress(game);
            stable = 0;
        } else {
            stable = backlog == 0 ? stable + 1 : 0;
        }
        long elapsed = (System.nanoTime() - retainedStarted) / 1_000_000L;
        if (retained < REQUIRED_FRAMES || elapsed < MINIMUM_MILLIS || stable < STABLE_FRAMES)
            return;
        finish(game, backlog, elapsed);
    }

    public static boolean retaining() { return stage == 2; }
    public static int retainedFrame() { return retained; }

    private static void warm(Minecraft game) {
        World world = game.world;
        if (warmup++ == 0) {
            for (int x = -4; x <= 4; x++)
                for (int z = -4; z <= 4; z++) world.getChunk(x, z);
        }
        machines = machineCount(world);
        if (!PREPARE && warmup == 2 && machines == 0) {
            WorldlineM773Rehydrator.rehydrate(world);
            machines = machineCount(world);
        }
        if (machines < 576 && warmup < 1200) return;
        require(machines == 576, "M773 machine fixture drift: " + machines);
        world.entities.retainAll(Collections.singleton(game.player));
        world.globalEntities.clear();
        SchedulerScene.place(game.player, 0, false);
        int backlog = backlog(game);
        stable = backlog == 0 ? stable + 1 : 0;
        if (stable < STABLE_FRAMES) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM773] template-ready machines=576 backlog=0");
            stage = 3;
            game.scheduleStop();
        } else {
            stage = 2;
            stable = 0;
        }
    }

    private static void finish(Minecraft game, int backlog, long elapsed) {
        require(backlog == 0, "M773 final backlog did not drain");
        try {
            SchedulerProbe.write(new File(System.getProperty("worldline.m773.metrics")),
                new File(System.getProperty("worldline.m773.frameArtifact")), ARM,
                maxBacklog, backlog, machines);
        } catch (Exception error) {
            throw new IllegalStateException("M773 artifact write failed", error);
        }
        System.out.println("[WorldlineM773] retained-complete arm=" + ARM
            + " frames=" + retained + " millis=" + elapsed + " backlog=" + backlog);
        stage = 3;
        game.scheduleStop();
    }

    private static int backlog(Minecraft game) {
        return ((SchedulerRendererStats) game.worldRenderer).worldlineCompileBacklog();
    }

    private static int machineCount(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (!block.isRemoved() && isCentralMachine(block)) count++;
        }
        return count;
    }

    private static boolean isCentralMachine(BlockEntity block) {
        if (block.y < 64 || block.y > 124 || (block.y - 64) % 4 != 0) return false;
        boolean x = (block.x >= 1 && block.x <= 3) || (block.x >= 11 && block.x <= 13);
        boolean z = (block.z >= 1 && block.z <= 3) || (block.z >= 11 && block.z <= 13);
        return x && z;
    }

    private static void prepareDisplay(Minecraft game) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = false;
        if (game.options != null) {
            game.options.hideHud = true;
            game.options.bobView = false;
            game.options.viewDistance = 1;
            game.options.fpsLimit = 0;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
