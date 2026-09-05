package worldline.m791;

import aero.modellib.test.WorldlineM791Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Owns the frozen multi-chunk covered/reveal lifecycle. */
public final class OcclusionState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m791.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m791.prepare");
    private static final String ARM = System.getProperty("worldline.m791.arm", "prepare");
    private static int stage, warmup, stable, retained, checkpoint, machines, sample = -1;
    private static boolean capture, candidate, prewarmed;

    private OcclusionState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM791] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
        } else if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        prepareDisplay(game);
        if (!prewarmed) { WorldlineM791Rehydrator.prewarm(); prewarmed = true; }
        WorldlineM791Rehydrator.verifyPopulation(game.world);
        if (OcclusionProbe.captures() == OcclusionScene.CHECKPOINTS * 2) { finish(game); return; }
        int nextSample = retained / OcclusionScene.HOLD_FRAMES;
        if (sample != nextSample) configure(nextSample);
        checkpoint = sample / 2;
        retained++;
        capture = retained % OcclusionScene.HOLD_FRAMES == 0;
        OcclusionScene.place(game.player, checkpoint);
    }

    public static boolean retaining() { return stage == 2; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean captureFrame() { return capture; }
    public static int checkpoint() { return checkpoint; }
    public static boolean candidateEnabled() { return candidate; }

    private static void configure(int nextSample) {
        sample = nextSample;
        boolean startsOn = ARM.contains("on-off");
        candidate = (sample & 1) == 0 ? startsOn : !startsOn;
        OcclusionToggle.set(candidate);
    }

    private static void warm(Minecraft game) {
        World world = game.world;
        stabilize(game, world);
        if (warmup++ == 0) for (int x = -4; x <= 5; x++) {
            for (int z = -4; z <= 5; z++) world.getChunk(x, z);
        }
        machines = WorldlineM791Rehydrator.count(world);
        if (warmup == 2 && machines != 512) {
            WorldlineM791Rehydrator.rehydrate(world);
            machines = WorldlineM791Rehydrator.count(world);
        }
        if (machines < 512 && warmup < 1200) return;
        require(machines == 512, "M791 machine fixture drift: " + machines);
        OcclusionScene.place(game.player, 0);
        if (++stable < 80) return;
        WorldlineM791Rehydrator.verifyPopulation(world);
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM791] template-ready machines=512");
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
            File metrics = new File(System.getProperty("worldline.m791.metrics"));
            OcclusionProbe.write(metrics, ARM, machines);
            OcclusionProfile.write(metrics.getParentFile(), ARM);
        }
        catch (Exception error) { throw new IllegalStateException("M791 artifact write failed", error); }
        System.out.println("[WorldlineM791] capture-complete arm=" + ARM
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
