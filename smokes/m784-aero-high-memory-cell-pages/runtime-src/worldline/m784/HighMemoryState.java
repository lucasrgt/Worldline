package worldline.m784;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.Aero_Prewarm;
import aero.modellib.render.Aero_RenderOptions;
import aero.modellib.test.MegaModelBlockEntity;
import aero.modellib.test.MegaModelBlockEntityRenderer;
import aero.modellib.test.WorldlineM784Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Owns one deterministic high-memory Cell Pages arm in a fresh client JVM. */
public final class HighMemoryState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m784.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m784.prepare");
    private static final String ARM = System.getProperty("worldline.m784.arm", "prepare");
    private static final int TARGET_FRAMES = Integer.getInteger("worldline.m784.frames", 1200);
    private static final int WARM_FRAMES = Integer.getInteger("worldline.m784.warmFrames", 480);
    private static final long MINIMUM_NS = Long.getLong("worldline.m784.minimumMillis", 20000L)
        * 1_000_000L;
    private static int stage, worldWarm, routeWarm, convergenceFrames, stablePages, retained, checkpoint;
    private static int machines, lastCached = -1;
    private static boolean capture, measuring, armBegan, controlledSubmission;
    private static long measurementStarted;
    private static Minecraft activeGame;

    private HighMemoryState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        activeGame = game;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM784] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAeroHigh", "Worldline Aero High", 17320110784L);
            return;
        }
        if (stage == 1 && game.world != null && game.player != null) warmWorld(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        prepareDisplay(game);
        if (!warmComplete()) {
            measuring = capture = false;
            if (routeWarm < WARM_FRAMES) HighMemoryScene.place(game.player, routeWarm++);
            else {
                HighMemoryScene.place(game.player, 0);
                convergenceFrames++;
            }
            updateStablePages();
            if (convergenceFrames > 0 && convergenceFrames % 120 == 0) {
                System.out.println("[WorldlineM784] warm arm=" + ARM + " route=" + routeWarm
                    + " convergence=" + convergenceFrames + " stable=" + stablePages
                    + " cached=" + Aero_BECellRenderer.cachedPageCount()
                    + " rebuilds=" + Aero_BECellRenderer.pageRebuildsThisFrame()
                    + " direct=" + Aero_BECellRenderer.directFallbacksThisFrame()
                    + " prewarm=" + Aero_Prewarm.queuedModelCount());
            }
            require(convergenceFrames < 2400, "M784 page cache did not converge");
            return;
        }
        if (!armBegan) beginArm();
        long elapsed = System.nanoTime() - measurementStarted;
        if (retained >= TARGET_FRAMES && elapsed >= MINIMUM_NS
                && retained % HighMemoryScene.ROUTE_FRAMES == 0) {
            finish(game);
            return;
        }
        measuring = true;
        int route = retained % HighMemoryScene.ROUTE_FRAMES;
        checkpoint = route / HighMemoryScene.CAPTURE_STRIDE;
        capture = (route + 1) % HighMemoryScene.CAPTURE_STRIDE == 0;
        HighMemoryScene.place(game.player, route);
        retained++;
    }

    public static boolean retaining() { return stage == 2 && measuring; }
    public static boolean fixtureActive() { return stage == 2; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean captureFrame() { return capture; }
    public static int checkpoint() { return checkpoint; }
    public static boolean controlledSubmission() { return controlledSubmission; }

    /** Submits the complete fixed fixture immediately before Aero's real flush. */
    public static void submitFixture(double cameraX, double cameraY, double cameraZ) {
        Minecraft game = activeGame;
        if (stage != 2 || game == null || game.world == null) return;
        HighMemoryProbe.beginScene();
        controlledSubmission = true;
        try {
            for (Object value : game.world.blockEntities) {
                BlockEntity block = (BlockEntity) value;
                if (!(block instanceof MegaModelBlockEntity)
                        || !WorldlineM784Rehydrator.contains(block.x, block.y, block.z)) continue;
                Aero_BECellRenderer.queueAtRest(MegaModelBlockEntityRenderer.MODEL,
                    MegaModelBlockEntityRenderer.TEXTURE, block,
                    block.x - cameraX, block.y - cameraY, block.z - cameraZ,
                    0.0F, 1.0F, Aero_RenderOptions.DEFAULT);
            }
        } finally {
            controlledSubmission = false;
        }
    }

    private static boolean warmComplete() {
        return routeWarm >= WARM_FRAMES && stablePages >= 60
            && (!Aero_Prewarm.ENABLED || Aero_Prewarm.queuedModelCount() == 0);
    }

    private static void updateStablePages() {
        int cached = Aero_BECellRenderer.cachedPageCount();
        boolean idle = cached > 0 && cached == lastCached
            && Aero_BECellRenderer.pageRebuildsThisFrame() == 0;
        stablePages = idle ? stablePages + 1 : 0;
        lastCached = cached;
    }

    private static void beginArm() {
        HighMemoryProbe.beginArm(ARM);
        measurementStarted = System.nanoTime();
        armBegan = true;
    }

    private static void warmWorld(Minecraft game) {
        World world = game.world;
        stabilize(game, world);
        if (worldWarm++ == 0) {
            for (int x = -1; x <= 4; x++) {
                for (int z = -1; z <= 4; z++) world.getChunk(x, z);
            }
        }
        machines = WorldlineM784Rehydrator.count(world);
        if (worldWarm == 2 && machines != WorldlineM784Rehydrator.MACHINES) {
            WorldlineM784Rehydrator.rehydrate(world);
            machines = WorldlineM784Rehydrator.count(world);
        }
        WorldlineM784Rehydrator.order(world);
        if (machines < WorldlineM784Rehydrator.MACHINES && worldWarm < 1200) return;
        require(machines == WorldlineM784Rehydrator.MACHINES,
            "M784 fixture drift: " + machines);
        HighMemoryScene.place(game.player, 0);
        if (worldWarm < 40) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM784] template-ready machines=" + machines);
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
            HighMemoryProbe.write(new File(System.getProperty("worldline.m784.metrics")),
                new File(System.getProperty("worldline.m784.framesFile")), ARM, machines);
        } catch (Exception error) {
            throw new IllegalStateException("M784 artifact write failed", error);
        }
        System.out.println("[WorldlineM784] capture-complete arm=" + ARM
            + " frames=" + HighMemoryProbe.frames() + " captures=" + HighMemoryProbe.captures());
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
