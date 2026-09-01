package worldline.m787;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.WorldlineM787Direct;
import aero.modellib.render.Aero_RenderOptions;
import aero.modellib.test.MegaModelBlockEntity;
import aero.modellib.test.MegaModelBlockEntityRenderer;
import aero.modellib.test.WorldlineM787Rehydrator;
import java.io.File;
import java.util.Collections;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.world.World;

/** Owns one fresh restored-world loading and cold-render client. */
public final class ColdEntryState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m787.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m787.prepare");
    private static final String ARM = System.getProperty("worldline.m787.arm", "prepare");
    private static int stage, prepareTicks, frame, submitted;
    private static boolean controlledSubmission;
    private static Minecraft activeGame;

    private ColdEntryState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        activeGame = game;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            ColdEntryLoadTrace.begin();
            System.out.println("[WorldlineM787] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAeroColdEntry", "Worldline Aero Cold Entry", 17320110787L);
            return;
        }
        if (PREPARE && stage == 1 && game.world != null && ++prepareTicks >= 40) {
            game.world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM787] template-ready machines="
                + WorldlineM787Rehydrator.count(game.world));
            stage = 3;
            game.scheduleStop();
        }
    }

    public static void worldLoaded(Minecraft game) {
        if (!ENABLED || stage != 1 || game.world == null || game.player == null) return;
        loadFixtureChunks(game.world);
        if (PREPARE) WorldlineM787Rehydrator.rehydrate(game.world);
        stabilize(game, game.world);
        WorldlineM787Rehydrator.order(game.world);
        int machines = WorldlineM787Rehydrator.count(game.world);
        require(machines == WorldlineM787Rehydrator.MACHINES,
            "M787 fixture drift at loading return: " + machines);
        ColdEntryLoadTrace.finish();
        if (!PREPARE) {
            ColdEntryScene.place(game.player, 0);
            stage = 2;
        }
    }

    public static void beginFrame(Minecraft game) {
        if (stage != 2 || game.player == null) return;
        prepareDisplay(game);
        ColdEntryScene.place(game.player, frame);
        submitted = 0;
    }

    public static void finishFrame(Minecraft game) {
        if (stage != 2 || game.player == null) return;
        ColdEntryScene.place(game.player, frame);
        double x = game.player.x, y = game.player.y, z = game.player.z;
        WorldlineM787Direct.discardQueued();
        ColdEntryProbe.beginScene(game);
        submitPages(x, y, z);
        ColdEntryProbe.flush(x, y, z);
        ColdEntryProbe.finishPageFrame(game, frame, submitted);
        if (ColdEntryScene.captureIndex(frame) >= 0) {
            ColdEntryProbe.beginScene(game);
            submitDirect(x, y, z);
            ColdEntryProbe.captureDirect(game, frame);
        }
        frame++;
        if (frame < ColdEntryScene.FRAMES) return;
        try {
            ColdEntryProbe.write(new File(System.getProperty("worldline.m787.metrics")),
                ARM, WorldlineM787Rehydrator.MACHINES);
        } catch (Exception error) {
            throw new IllegalStateException("M787 artifact write failed", error);
        }
        System.out.println("[WorldlineM787] capture-complete arm=" + ARM
            + " frames=" + frame + " captures=" + ColdEntryProbe.captures());
        stage = 3;
        game.scheduleStop();
    }

    public static boolean fixtureActive() { return stage == 2; }
    public static boolean freezeTicks() { return stage == 2; }
    public static boolean controlledSubmission() { return controlledSubmission; }

    private static void submitPages(double cameraX, double cameraY, double cameraZ) {
        Minecraft game = activeGame;
        controlledSubmission = true;
        try {
            for (Object value : game.world.blockEntities) {
                BlockEntity block = (BlockEntity) value;
                if (!(block instanceof MegaModelBlockEntity)
                        || !WorldlineM787Rehydrator.contains(block.x, block.y, block.z)) continue;
                Aero_BECellRenderer.queueAtRest(MegaModelBlockEntityRenderer.MODEL,
                    MegaModelBlockEntityRenderer.TEXTURE, block,
                    block.x - cameraX, block.y - cameraY, block.z - cameraZ,
                    0.0F, 1.0F, Aero_RenderOptions.DEFAULT);
                submitted++;
            }
        } finally {
            controlledSubmission = false;
        }
    }

    private static void submitDirect(double cameraX, double cameraY, double cameraZ) {
        Minecraft game = activeGame;
        for (Object value : game.world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (!(block instanceof MegaModelBlockEntity)
                    || !WorldlineM787Rehydrator.contains(block.x, block.y, block.z)) continue;
            WorldlineM787Direct.draw(MegaModelBlockEntityRenderer.MODEL,
                MegaModelBlockEntityRenderer.TEXTURE,
                block.x - cameraX, block.y - cameraY, block.z - cameraZ,
                0.0F, 1.0F, Aero_RenderOptions.DEFAULT);
        }
    }

    private static void loadFixtureChunks(World world) {
        for (int x = -1; x <= 4; x++) {
            for (int z = -1; z <= 4; z++) world.getChunk(x, z);
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
