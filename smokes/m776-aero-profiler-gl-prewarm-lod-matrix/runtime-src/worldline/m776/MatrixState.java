package worldline.m776;

import aero.modellib.test.WorldlineM776Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Drives the restored four-arm GL, prewarm, and LOD journey. */
public final class MatrixState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m776.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m776.prepare");
    private static final String ARM = System.getProperty("worldline.m776.arm", "prepare");
    private static final int REQUIRED_FRAMES = Integer.getInteger("worldline.m776.frames", 600);
    private static final long MINIMUM_MILLIS = Long.getLong("worldline.m776.minimumMillis", 12000L);
    private static int stage, warmup, stable, retained, machines;
    private static long retainedStarted;

    private MatrixState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM776] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return;
        }
        if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        retained++;
        MatrixScene.place(game.player, retained);
        if (retained == 1) {
            retainedStarted = System.nanoTime();
            System.out.println("[WorldlineM776] retained-start arm=" + ARM);
        }
        long elapsed = (System.nanoTime() - retainedStarted) / 1_000_000L;
        if (retained < REQUIRED_FRAMES || elapsed < MINIMUM_MILLIS) return;
        finish(game, elapsed);
    }

    public static boolean retaining() { return stage == 2; }
    public static int phase() { return MatrixScene.phase(retained); }

    private static void warm(Minecraft game) {
        World world = game.world;
        if (warmup++ == 0) {
            for (int x = -2; x <= 6; x++)
                for (int z = -2; z <= 6; z++) world.getChunk(x, z);
        }
        machines = WorldlineM776Rehydrator.count(world);
        if (warmup == 2 && machines != 120) {
            WorldlineM776Rehydrator.rehydrate(world);
            machines = WorldlineM776Rehydrator.count(world);
        }
        if (machines < 120 && warmup < 1200) return;
        require(machines == 120, "M776 machine fixture drift: " + machines);
        world.entities.retainAll(Collections.singleton(game.player));
        world.globalEntities.clear();
        MatrixScene.place(game.player, 0);
        if (++stable < 20) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM776] template-ready machines=120");
            stage = 3;
            game.scheduleStop();
        } else stage = 2;
    }

    private static void finish(Minecraft game, long elapsed) {
        try {
            MatrixProbe.write(new File(System.getProperty("worldline.m776.metrics")),
                new File(System.getProperty("worldline.m776.framesFile")), ARM, machines);
        } catch (Exception error) {
            throw new IllegalStateException("M776 artifact write failed", error);
        }
        System.out.println("[WorldlineM776] retained-complete arm=" + ARM
            + " frames=" + retained + " millis=" + elapsed);
        stage = 3;
        game.scheduleStop();
    }

    private static void prepareDisplay(Minecraft game) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = stage != 2;
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
