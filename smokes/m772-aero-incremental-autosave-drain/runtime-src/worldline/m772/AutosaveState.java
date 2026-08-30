package worldline.m772;

import java.nio.file.Path;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;

/** Drives template, measured, and fresh-reload phases on the client thread. */
public final class AutosaveState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m772.enabled");
    private static final String MODE = System.getProperty("worldline.m772.mode", "prepare");
    private static final String ARM = System.getProperty("worldline.m772.arm", MODE);
    private static final Path METRICS = Path.of(System.getProperty("worldline.m772.metrics"));
    private static final Path FRAMES = Path.of(System.getProperty("worldline.m772.frames"));
    private static final int TARGETS = 12;
    private static final int FORCE_TICK = 540;
    private static int stage, warmTicks, measuredTicks;

    private AutosaveState() {}

    public static void beforeTick(Minecraft game) throws Exception {
        if (!ENABLED) return;
        AutosaveScene.prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM772] start mode=" + MODE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return;
        }
        if (game.world == null || game.player == null || stage >= 4) return;
        AutosaveScene.loadTargets(game.world);
        AutosaveScene.place(game.player);
        if (stage == 1) warm(game);
        if (stage == 2 && measuredTicks++ == FORCE_TICK) {
            forceAndFinish(game);
        }
    }

    private static void warm(Minecraft game) throws Exception {
        if (++warmTicks < 40) return;
        game.world.saveWithLoadingDisplay(true, null);
        if (MODE.equals("prepare")) {
            System.out.println("[WorldlineM772] template-ready targets=" + TARGETS);
            stage = 4;
            game.scheduleStop();
            return;
        }
        if (MODE.equals("verify")) {
            require(AutosaveScene.sentinels(game.world, Block.DIAMOND_BLOCK.id) == TARGETS,
                    "fresh reload lost forced-drain sentinels");
            System.out.println("[WorldlineM772] persistence-pass arm=" + ARM
                    + " sentinels=" + TARGETS);
            stage = 4;
            game.scheduleStop();
            return;
        }
        require(AutosaveScene.targetDirty(game.world) == 0,
                "normalized template remained dirty");
        require(AutosaveScene.setSentinels(game.world, Block.GOLD_BLOCK.id) == TARGETS,
                "initial sentinel mutation failed");
        require(AutosaveScene.targetDirty(game.world) == TARGETS,
                "initial dirty set drifted");
        AutosaveProbe.start();
        stage = 2;
        System.out.println("[WorldlineM772] retained-start arm=" + ARM + " dirty=" + TARGETS);
    }

    private static void forceAndFinish(Minecraft game) throws Exception {
        require(AutosaveScene.setSentinels(game.world, Block.DIAMOND_BLOCK.id) == TARGETS,
                "forced sentinel mutation failed");
        require(AutosaveScene.targetDirty(game.world) == TARGETS,
                "forced dirty set drifted");
        game.world.saveWithLoadingDisplay(true, null);
        require(AutosaveScene.targetDirty(game.world) == 0,
                "forced save did not drain targets");
        AutosaveProbe.finish(METRICS, FRAMES, ARM);
        System.out.println("[WorldlineM772] retained-complete arm=" + ARM
                + " ticks=" + measuredTicks + " forcedAfter=0");
        stage = 4;
        game.scheduleStop();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
