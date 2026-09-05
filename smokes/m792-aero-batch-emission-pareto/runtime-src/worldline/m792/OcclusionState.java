package worldline.m792;

import aero.modellib.test.WorldlineM792Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Owns the frozen multi-chunk covered/reveal lifecycle. */
public final class OcclusionState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m792.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m792.prepare");
    private static final String ARM = System.getProperty("worldline.m792.arm", "prepare");
    private static int stage, warmup, stable, retained, checkpoint, machines, renderWarm, sample = -1;
    private static boolean capture, candidate, prewarmed;

    private OcclusionState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM792] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
        } else if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        prepareDisplay(game);
        if (!prewarmed) { WorldlineM792Rehydrator.prewarm(); prewarmed = true; }
        WorldlineM792Rehydrator.verifyPopulation(game.world);
        if (renderWarm < OcclusionScene.CHECKPOINTS * 240) {
            int camera = renderWarm / 240;
            candidate = (renderWarm / 120 & 1) != 0;
            OcclusionScene.place(game.player, camera);
            WorldlineM792Rehydrator.pose(game.world, camera >= 12 ? -1 : camera >= 8 ? renderWarm % 120 : 10);
            renderWarm++;
            return;
        }
        if (OcclusionProbe.captures() == OcclusionScene.CHECKPOINTS * 2) { finish(game); return; }
        int nextSample = retained / OcclusionScene.HOLD_FRAMES;
        if (sample != nextSample) configure(nextSample);
        checkpoint = sample / 2;
        retained++;
        capture = retained % OcclusionScene.HOLD_FRAMES == 0;
        OcclusionScene.place(game.player, checkpoint);
        WorldlineM792Rehydrator.pose(game.world, checkpoint >= 12 ? -1 : checkpoint >= 8 ? localFrame() : 10);
    }

    public static boolean retaining() { return stage == 2; }
    public static boolean acquiring() { return retaining() && retained > 0; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean captureFrame() { return capture; }
    public static int checkpoint() { return checkpoint; }
    public static boolean candidateEnabled() { return candidate; }
    public static int localFrame() { return (retained - 1) % OcclusionScene.HOLD_FRAMES; }
    public static boolean measured() {
        return retaining() && localFrame() >= OcclusionScene.WARM_FRAMES;
    }

    private static void configure(int nextSample) {
        sample = nextSample;
        boolean startsOn = ARM.contains("on-off");
        candidate = (sample & 1) == 0 ? startsOn : !startsOn;
    }

    private static void warm(Minecraft game) {
        World world = game.world;
        stabilize(game, world);
        if (warmup++ == 0) for (int x = -4; x <= 5; x++) {
            for (int z = -4; z <= 5; z++) world.getChunk(x, z);
        }
        machines = WorldlineM792Rehydrator.count(world);
        if (warmup == 2 && machines != 512) {
            WorldlineM792Rehydrator.rehydrate(world);
            machines = WorldlineM792Rehydrator.count(world);
        }
        if (machines < 512 && warmup < 1200) return;
        require(machines == 512, "M792 machine fixture drift: " + machines);
        OcclusionScene.place(game.player, 0);
        if (++stable < 80) return;
        WorldlineM792Rehydrator.verifyPopulation(world);
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM792] template-ready machines=512");
            stage = 3;
            game.scheduleStop();
        } else stage = 2;
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
            File metrics = new File(System.getProperty("worldline.m792.metrics"));
            OcclusionProbe.write(metrics, ARM, machines);
            OcclusionProfile.write(metrics.getParentFile(), ARM);
        }
        catch (Exception error) { throw new IllegalStateException("M792 artifact write failed", error); }
        System.out.println("[WorldlineM792] capture-complete arm=" + ARM
            + " checkpoints=" + OcclusionProbe.captures());
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
        game.options.advancedOpengl = true;
        game.options.fpsLimit = 0;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
