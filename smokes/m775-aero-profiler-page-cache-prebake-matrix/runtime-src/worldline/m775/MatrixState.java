package worldline.m775;

import aero.modellib.test.WorldlineM775Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Drives the restored three-arm runtime matrix and bounded camera journey. */
public final class MatrixState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m775.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m775.prepare");
    private static final String ARM = System.getProperty("worldline.m775.arm", "prepare");
    private static final int REQUIRED_FRAMES = Integer.getInteger("worldline.m775.frames", 600);
    private static final long MINIMUM_MILLIS = Long.getLong("worldline.m775.minimumMillis", 15000L);
    private static int stage, warmup, stable, retained, maxBacklog, machines, phase;
    private static long retainedStarted;

    private MatrixState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM775] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return;
        }
        if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        retained++;
        phase = phaseFor(retained);
        MatrixScene.place(game.player, retained);
        if (retained == 1) {
            retainedStarted = System.nanoTime();
            System.out.println("[WorldlineM775] retained-start arm=" + ARM);
        }
        MatrixScene.act(game, retained);
        int backlog = backlog(game);
        maxBacklog = Math.max(maxBacklog, backlog);
        stable = retained > 450 && backlog == 0 ? stable + 1 : 0;
        long elapsed = (System.nanoTime() - retainedStarted) / 1_000_000L;
        if (retained < REQUIRED_FRAMES || elapsed < MINIMUM_MILLIS || stable < 20) return;
        finish(game, backlog, elapsed);
    }

    public static boolean retaining() { return stage == 2; }
    public static int phase() { return phase; }

    private static void warm(Minecraft game) {
        World world = game.world;
        if (warmup++ == 0) {
            for (int x = -4; x <= 8; x++)
                for (int z = -4; z <= 8; z++) world.getChunk(x, z);
        }
        machines = machineCount(world);
        if (!PREPARE && warmup == 2 && machines == 0) {
            WorldlineM775Rehydrator.rehydrate(world);
            machines = machineCount(world);
        }
        if (machines < 576 && warmup < 1200) return;
        require(machines == 576, "M775 machine fixture drift: " + machines);
        world.entities.retainAll(Collections.singleton(game.player));
        world.globalEntities.clear();
        MatrixScene.place(game.player, 0);
        stable = backlog(game) == 0 ? stable + 1 : 0;
        if (stable < 20) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM775] template-ready machines=576 backlog=0");
            stage = 3;
            game.scheduleStop();
        } else {
            stage = 2;
            stable = 0;
        }
    }

    private static void finish(Minecraft game, int backlog, long elapsed) {
        require(backlog == 0, "M775 final backlog did not drain");
        try {
            MatrixProbe.write(new File(System.getProperty("worldline.m775.metrics")),
                new File(System.getProperty("worldline.m775.framesFile")), ARM,
                maxBacklog, backlog, machines);
        } catch (Exception error) {
            throw new IllegalStateException("M775 artifact write failed", error);
        }
        System.out.println("[WorldlineM775] retained-complete arm=" + ARM
            + " frames=" + retained + " millis=" + elapsed + " backlog=" + backlog);
        stage = 3;
        game.scheduleStop();
    }

    private static int phaseFor(int frame) {
        if (frame <= 80) return 0;
        if (frame <= 200) return 1;
        if (frame <= 320) return 2;
        if (frame <= 450) return 3;
        return 4;
    }

    private static int backlog(Minecraft game) {
        return ((MatrixRendererStats) game.worldRenderer).worldlineCompileBacklog();
    }

    private static int machineCount(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (!block.isRemoved() && block.y >= 64 && block.y <= 124
                    && (block.y - 64) % 4 == 0 && clustered(block.x) && clustered(block.z)) count++;
        }
        return count;
    }

    private static boolean clustered(int value) {
        return (value >= 1 && value <= 3) || (value >= 11 && value <= 13);
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
