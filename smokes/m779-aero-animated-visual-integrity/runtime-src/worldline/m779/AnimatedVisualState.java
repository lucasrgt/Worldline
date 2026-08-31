package worldline.m779;

import aero.modellib.test.WorldlineM779Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Owns one immutable-culling, deterministic-pose route in a fresh JVM. */
public final class AnimatedVisualState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m779.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m779.prepare");
    private static final String ARM = System.getProperty("worldline.m779.arm", "prepare");
    private static final int ROUTE_WARM_FRAMES = AnimatedVisualScene.ROUTE_FRAMES * 5;
    private static int stage, warmup, stable, routeWarm;
    private static int retained, routeFrame, checkpoint;
    private static boolean capture, prewarmed, measuring, armBegan;
    private static int[] counts = new int[3];

    private AnimatedVisualState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM779] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return;
        }
        if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        prepareDisplay(game);
        if (!prewarmed) {
            WorldlineM779Rehydrator.prewarm();
            prewarmed = true;
        }
        if (routeWarm < ROUTE_WARM_FRAMES) {
            capture = measuring = false;
            int frame = routeWarm++ % AnimatedVisualScene.ROUTE_FRAMES;
            AnimatedVisualScene.place(game.player, frame);
            WorldlineM779Rehydrator.pose(game.world, frame);
            return;
        }
        if (retained == AnimatedVisualScene.ROUTE_FRAMES) {
            finish(game);
            return;
        }
        if (!armBegan) {
            AnimatedVisualProbe.beginArm(ARM.equals("cull-on"));
            armBegan = true;
        }
        measuring = true;
        routeFrame = retained;
        checkpoint = routeFrame / AnimatedVisualScene.CAPTURE_STRIDE;
        capture = (routeFrame + 1) % AnimatedVisualScene.CAPTURE_STRIDE == 0;
        AnimatedVisualScene.place(game.player, routeFrame);
        WorldlineM779Rehydrator.pose(game.world, routeFrame);
        retained++;
    }

    public static boolean retaining() { return stage == 2 && measuring; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean captureFrame() { return capture; }
    public static int checkpoint() { return checkpoint; }

    private static void warm(Minecraft game) {
        World world = game.world;
        stabilize(game, world);
        if (warmup++ == 0) {
            for (int x = -2; x <= 6; x++) {
                for (int z = -2; z <= 6; z++) world.getChunk(x, z);
            }
        }
        counts = WorldlineM779Rehydrator.counts(world);
        if (warmup == 2 && total(counts) != 120) {
            WorldlineM779Rehydrator.rehydrate(world);
            counts = WorldlineM779Rehydrator.counts(world);
        }
        if (total(counts) < 120 && warmup < 1200) return;
        require(counts[0] == 40 && counts[1] == 40 && counts[2] == 40,
            "M779 fixture drift: " + counts[0] + "/" + counts[1] + "/" + counts[2]);
        AnimatedVisualScene.place(game.player, 0);
        WorldlineM779Rehydrator.pose(world, 0);
        if (++stable < 40) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM779] template-ready machines=120 types=40/40/40");
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
            AnimatedVisualProbe.write(new File(System.getProperty("worldline.m779.metrics")),
                ARM, counts);
        } catch (Exception error) {
            throw new IllegalStateException("M779 artifact write failed", error);
        }
        System.out.println("[WorldlineM779] capture-complete arm=" + ARM
            + " frames=" + retained + " captures=" + AnimatedVisualProbe.captures());
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

    private static int total(int[] values) { return values[0] + values[1] + values[2]; }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
