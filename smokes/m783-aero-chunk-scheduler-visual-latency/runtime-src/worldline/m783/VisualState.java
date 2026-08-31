package worldline.m783;

import aero.modellib.test.WorldlineM783Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Drives one long restored-world route with a midpoint renderer world rebind. */
public final class VisualState {
    private static final boolean ENABLED = Boolean.getBoolean("worldline.m783.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m783.prepare");
    private static final String ARM = System.getProperty("worldline.m783.arm", "prepare");
    private static final int REQUIRED = Integer.getInteger("worldline.m783.frames", 2400);
    private static final int TRANSITION_FRAME = Integer.getInteger(
            "worldline.m783.transitionFrame", 1200);
    private static final long MINIMUM_MILLIS = Long.getLong("worldline.m783.minimumMillis", 30000L);
    private static int stage, warmup, stable, retained, phase, machines, maxBacklog, transitions;
    private static int transitionWait;
    private static long retainedStarted;

    private VisualState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        VisualFixture.prepareDisplay(game);
        if (stage == 0 && game.world == null) start(game, 1);
        else if (stage == 3) awaitRebind(game);
        else if (stage == 1 && game.world != null && game.player != null) warm(game);
    }

    public static void frame(Minecraft game) {
        if (stage != 2 || game.world == null || game.player == null) return;
        retained++;
        phase = ((retained - 1) % 600) / 100;
        VisualScene.place(game.player, retained);
        if (retained == 1) {
            retainedStarted = System.nanoTime();
            System.out.println("[WorldlineM783] retained-start arm=" + ARM);
        }
        if (retained == TRANSITION_FRAME) {
            stage = 3;
            return;
        }
        VisualScene.act(game, retained);
        int backlog = backlog(game);
        maxBacklog = Math.max(maxBacklog, backlog);
        stable = retained > REQUIRED - 100 && backlog == 0
                && VisualProbe.pendingVisible() == 0 ? stable + 1 : 0;
        long elapsed = (System.nanoTime() - retainedStarted) / 1_000_000L;
        require(retained < REQUIRED + 600 || stable >= 20,
                "M783 retained drain timeout: backlog=" + backlog
                        + " pending=" + VisualProbe.pendingVisible());
        if (retained < REQUIRED || elapsed < MINIMUM_MILLIS || stable < 20) return;
        finish(game, backlog, elapsed);
    }

    public static boolean retaining() { return stage == 2; }
    public static int phase() { return phase; }
    public static int frameIndex() { return retained; }

    private static void start(Minecraft game, int nextStage) {
        stage = nextStage;
        warmup = stable = 0;
        game.interactionManager = new SingleplayerInteractionManager(game);
        System.out.println("[WorldlineM783] start prepare=" + PREPARE
                + " arm=" + ARM + " stage=" + nextStage);
        game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
    }

    private static void warm(Minecraft game) {
        World world = game.world;
        if (warmup++ == 0) {
            for (int x = -4; x <= 8; x++)
                for (int z = -4; z <= 8; z++) world.getChunk(x, z);
        }
        machines = VisualFixture.machineCount(world);
        if (!PREPARE && warmup == 2 && machines == 0) {
            WorldlineM783Rehydrator.rehydrate(world);
            machines = VisualFixture.machineCount(world);
        }
        if (machines < 576 && warmup < 1200) return;
        require(machines == 576, "M783 machine fixture drift: " + machines);
        world.entities.retainAll(Collections.singleton(game.player));
        world.globalEntities.clear();
        VisualScene.place(game.player, retained);
        stable = backlog(game) == 0 ? stable + 1 : 0;
        if (stable < 20) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM783] template-ready machines=576 backlog=0");
            stage = 5;
            game.scheduleStop();
            return;
        }
        stage = 2;
        stable = 0;
    }

    private static void rebind(Minecraft game) {
        require(backlog(game) == 0, "M783 rebind backlog did not drain");
        game.world.saveWithLoadingDisplay(true, null);
        game.setWorld(game.world);
        transitions++;
        stage = 2;
        stable = warmup = 0;
        VisualFixture.prepareDisplay(game);
        VisualScene.place(game.player, retained);
    }

    private static void awaitRebind(Minecraft game) {
        transitionWait++;
        require(transitionWait <= 600, "M783 rebind drain timeout: backlog="
                + backlog(game) + " pending=" + VisualProbe.pendingVisible());
        if (backlog(game) != 0 || VisualProbe.pendingVisible() != 0) return;
        rebind(game);
    }

    private static void finish(Minecraft game, int backlog, long elapsed) {
        require(backlog == 0 && transitions == 1, "M783 final lifecycle drift");
        try {
            VisualProbe.write(new File(System.getProperty("worldline.m783.metrics")),
                    new File(System.getProperty("worldline.m783.framesFile")), ARM,
                    maxBacklog, backlog, machines, transitions);
        } catch (Exception error) {
            throw new IllegalStateException("M783 artifact write failed", error);
        }
        System.out.println("[WorldlineM783] retained-complete arm=" + ARM
                + " frames=" + retained + " millis=" + elapsed + " backlog=" + backlog);
        stage = 5;
        game.scheduleStop();
    }

    private static int backlog(Minecraft game) {
        return ((VisualRendererStats) game.worldRenderer).worldlineCompileBacklog();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
