package worldline.m793;

import aero.modellib.test.WorldlineM793Rehydrator;
import aero.modellib.test.ProductionModels;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Owns the frozen multi-chunk covered/reveal lifecycle. */
public final class ProductionState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m793.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m793.prepare");
    private static final String ARM = System.getProperty("worldline.m793.arm", "prepare");
    private static int stage, warmup, stable, retained, checkpoint, machines, renderWarm, sample = -1;
    private static boolean capture, candidate;

    private ProductionState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM793] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
        } else if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        prepareDisplay(game);
        WorldlineM793Rehydrator.verifyPopulation(game.world);
        if (renderWarm++ < 240) { ProductionScene.place(game.player, 0); return; }
        if (ProductionProbe.captures() == ProductionScene.CHECKPOINTS * 2) { finish(game); return; }
        int nextSample = retained / ProductionScene.HOLD_FRAMES;
        if (sample != nextSample) configure(nextSample);
        int scene = sample / 2;
        retained++;
        checkpoint = scene * 3 + localFrame() / 60;
        capture = retained % 60 == 0;
        ProductionScene.place(game.player, scene);
        ProductionModels.update(scene, localFrame(), sample);
        if (scene == 10 && localFrame() % 60 == 0)
            game.world.setBlock(16, 79, 16, (localFrame() / 60 & 1) == 0 ? 7 : 1);
    }

    public static boolean retaining() { return stage == 2; }
    public static boolean acquiring() { return retaining() && retained > 0; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean captureFrame() { return capture; }
    public static int scene() { return sample < 0 ? -1 : sample / 2; }
    public static int checkpoint() { return checkpoint; }
    public static boolean candidateEnabled() { return candidate; }
    public static int localFrame() { return (retained - 1) % ProductionScene.HOLD_FRAMES; }
    public static boolean measured() {
        return acquiring() && (scene() == 0 || localFrame() >= ProductionScene.WARM_FRAMES);
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
        machines = WorldlineM793Rehydrator.count(world);
        if (warmup == 2 && machines != 512) {
            WorldlineM793Rehydrator.rehydrate(world);
            machines = WorldlineM793Rehydrator.count(world);
        }
        if (machines < 512 && warmup < 1200) return;
        require(machines == 512, "M793 machine fixture drift: " + machines);
        ProductionScene.place(game.player, 0);
        if (++stable < 80) return;
        WorldlineM793Rehydrator.verifyPopulation(world);
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM793] template-ready machines=512");
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
            File metrics = new File(System.getProperty("worldline.m793.metrics"));
            ProductionProbe.write(metrics, ARM, machines);
            ProductionProfile.write(metrics.getParentFile(), ARM);
        }
        catch (Exception error) { throw new IllegalStateException("M793 artifact write failed", error); }
        System.out.println("[WorldlineM793] capture-complete arm=" + ARM
            + " checkpoints=" + ProductionProbe.captures());
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
