package worldline.m778;

import aero.modellib.test.WorldlineM778Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Owns the frozen-world visual checkpoint lifecycle. */
public final class VisualState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m778.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m778.prepare");
    private static final String ARM = System.getProperty("worldline.m778.arm", "prepare");
    private static int stage, warmup, stable, retained, checkpoint, machines;
    private static int sample = -1;
    private static boolean capture, culling, prewarmed;

    private VisualState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM778] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return;
        }
        if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        prepareDisplay(game);
        if (!prewarmed) {
            WorldlineM778Rehydrator.prewarm();
            prewarmed = true;
        }
        if (VisualProbe.captures() == VisualScene.CHECKPOINTS * 2) {
            finish(game);
            return;
        }
        int nextSample = retained / VisualScene.HOLD_FRAMES;
        if (sample != nextSample) configureSample(nextSample);
        checkpoint = sample / 2;
        retained++;
        capture = retained % VisualScene.HOLD_FRAMES == 0;
        VisualScene.place(game.player, checkpoint);
    }

    public static boolean retaining() { return stage == 2; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean captureFrame() { return capture; }
    public static int checkpoint() { return checkpoint; }
    public static boolean cullingEnabled() { return culling; }

    private static void configureSample(int nextSample) {
        sample = nextSample;
        boolean startsOn = ARM.contains("on-off");
        culling = (sample & 1) == 0 ? startsOn : !startsOn;
        VisualCulling.set(culling);
    }

    private static void warm(Minecraft game) {
        World world = game.world;
        stabilize(game, world);
        if (warmup++ == 0) {
            for (int x = -2; x <= 6; x++) {
                for (int z = -2; z <= 6; z++) world.getChunk(x, z);
            }
        }
        machines = WorldlineM778Rehydrator.count(world);
        if (warmup == 2 && machines != 120) {
            WorldlineM778Rehydrator.rehydrate(world);
            machines = WorldlineM778Rehydrator.count(world);
        }
        if (machines < 120 && warmup < 1200) return;
        require(machines == 120, "M778 machine fixture drift: " + machines);
        VisualScene.place(game.player, 0);
        if (++stable < 40) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM778] template-ready machines=120");
            stage = 3;
            game.scheduleStop();
        } else {
            stage = 2;
        }
    }

    private static void stabilize(Minecraft game, World world) {
        world.setTime(6000L);
        world.getProperties().setRaining(false);
        world.getProperties().setThundering(false);
        world.setRainGradient(0.0F);
        world.entities.retainAll(Collections.singleton(game.player));
        world.globalEntities.clear();
        game.raining = false;
    }

    private static void finish(Minecraft game) {
        try {
            VisualProbe.write(new File(System.getProperty("worldline.m778.metrics")), ARM, machines);
        } catch (Exception error) {
            throw new IllegalStateException("M778 artifact write failed", error);
        }
        System.out.println("[WorldlineM778] capture-complete arm=" + ARM
            + " checkpoints=" + VisualProbe.captures());
        stage = 3;
        game.scheduleStop();
    }

    private static void prepareDisplay(Minecraft game) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = stage != 2;
        if (game.options == null) return;
        game.options.hideHud = true;
        game.options.bobView = false;
        game.options.viewDistance = 1;
        game.options.fpsLimit = 0;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
