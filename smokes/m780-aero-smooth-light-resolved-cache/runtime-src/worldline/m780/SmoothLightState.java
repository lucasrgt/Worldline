package worldline.m780;

import aero.modellib.render.Aero_SmoothLightCache;
import aero.modellib.test.SmoothLightContract;
import aero.modellib.test.WorldlineM780Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Owns one deterministic, immutable-cache arm in a fresh client JVM. */
public final class SmoothLightState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m780.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m780.prepare");
    private static final String ARM = System.getProperty("worldline.m780.arm", "prepare");
    private static final int WARM_FRAMES = SmoothLightScene.ROUTE_FRAMES * 2;
    private static int stage, warmup, stable, routeWarm, retained, routeFrame, checkpoint;
    private static boolean capture, measuring, armBegan, changingLight, lightReady;
    private static int lightDiagnostic;
    private static long lightDeadline;
    private static int machines;

    private SmoothLightState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM780] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAeroLight", "Worldline Aero Light", 17320110708L);
            return;
        }
        if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        prepareDisplay(game);
        if (routeWarm < WARM_FRAMES) {
            measuring = capture = false;
            SyntheticLight.phase(0);
            SmoothLightScene.place(game.player, routeWarm++ % SmoothLightScene.ROUTE_FRAMES);
            return;
        }
        if (!armBegan) beginArm();
        if (retained == SmoothLightScene.ROUTE_FRAMES) {
            finish(game);
            return;
        }
        if (retained == SmoothLightScene.ROUTE_FRAMES / 2 && waitForLightRefresh(game)) return;
        measuring = true;
        routeFrame = retained;
        checkpoint = routeFrame / SmoothLightScene.CAPTURE_STRIDE;
        capture = (routeFrame + 1) % SmoothLightScene.CAPTURE_STRIDE == 0;
        SmoothLightScene.place(game.player, routeFrame);
        retained++;
    }

    public static boolean retaining() { return stage == 2 && measuring; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean captureFrame() { return capture; }
    public static int checkpoint() { return checkpoint; }
    public static int lightDiagnostic() { return lightDiagnostic; }
    public static void consumedLightDiagnostic() { lightDiagnostic = 0; }

    private static void beginArm() {
        Aero_SmoothLightCache.clear();
        Aero_SmoothLightCache.resetStatistics();
        SmoothLightProbe.beginArm(ARM.equals("cache-on"));
        armBegan = true;
    }

    private static boolean waitForLightRefresh(Minecraft game) {
        measuring = capture = false;
        SmoothLightScene.place(game.player, retained);
        if (!changingLight) {
            changingLight = true;
            lightDiagnostic = 1;
            return true;
        }
        SyntheticLight.phase(1);
        if (lightDeadline == 0L) lightDeadline = System.nanoTime() + 100_000_000L;
        if (System.nanoTime() < lightDeadline) return true;
        if (!lightReady) {
            lightReady = true;
            lightDiagnostic = 2;
            return true;
        }
        return false;
    }

    private static void warm(Minecraft game) {
        World world = game.world;
        stabilize(game, world);
        if (warmup++ == 0) {
            for (int x = -1; x <= 5; x++) {
                for (int z = -1; z <= 5; z++) world.getChunk(x, z);
            }
        }
        machines = WorldlineM780Rehydrator.count(world);
        if (warmup == 2 && machines != SmoothLightContract.MACHINES) {
            WorldlineM780Rehydrator.rehydrate(world);
            machines = WorldlineM780Rehydrator.count(world);
        }
        WorldlineM780Rehydrator.order(world);
        if (machines < SmoothLightContract.MACHINES && warmup < 1200) return;
        require(machines == SmoothLightContract.MACHINES, "M780 fixture drift: " + machines);
        SyntheticLight.phase(0);
        SmoothLightScene.place(game.player, 0);
        if (++stable < 40) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM780] template-ready machines=" + machines);
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
            SmoothLightProbe.write(new File(System.getProperty("worldline.m780.metrics")),
                ARM, machines);
        } catch (Exception error) {
            throw new IllegalStateException("M780 artifact write failed", error);
        }
        System.out.println("[WorldlineM780] capture-complete arm=" + ARM
            + " frames=" + retained + " captures=" + SmoothLightProbe.captures());
        stage = 3;
        measuring = false;
        game.scheduleStop();
    }

    private static void prepareDisplay(Minecraft game) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = stage != 2;
        if (game.options == null) return;
        game.options.hideHud = true;
        game.options.bobView = false;
        game.options.viewDistance = 0;
        game.options.fpsLimit = 0;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
