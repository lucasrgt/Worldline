package aero.modellib.test.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.InteractionManager;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import java.util.Collections;
import aero.modellib.test.worldline.WorldlineFrameOracle;
import aero.modellib.test.worldline.WorldlineSaveForce;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drives a fixed real-world capture from the Minecraft main thread. */
@Mixin(Minecraft.class)
public abstract class WorldlineCaptureMixin {
    @Shadow public World world;
    @Shadow public ClientPlayerEntity player;
    @Shadow public InteractionManager interactionManager;
    @Shadow public abstract void startGame(String directory, String name, long seed);
    @Shadow public abstract void scheduleStop();

    @Unique private static final boolean WORLDLINE_ENABLED =
        Boolean.getBoolean("worldline.capture.enabled");
    @Unique private static final long WORLDLINE_SEED =
        Long.getLong("worldline.capture.seed", 17320110707L);
    @Unique private static final int WORLDLINE_TICKS =
        Integer.getInteger("worldline.capture.ticks", 240);
    @Unique private static final int WORLDLINE_Y =
        Integer.getInteger("worldline.capture.y", 67);
    @Unique private static final int WORLDLINE_MIN_BES =
        Integer.getInteger("worldline.capture.minBlockEntities", 500);
    @Unique private static final int WORLDLINE_MIN_WARMUP =
        Integer.getInteger("worldline.capture.minWarmupTicks", 0);
    @Unique private static final String WORLDLINE_PATH =
        System.getProperty("worldline.capture.path", "stationary");
    @Unique private static final int WORLDLINE_VIEW_DISTANCE =
        Integer.getInteger("worldline.capture.viewDistance", -1);
    @Unique private static final boolean WORLDLINE_STABILIZE_SCENE =
        Boolean.getBoolean("worldline.frameOracle.stabilizeScene");
    @Unique private static final int WORLDLINE_SAVE_TICK =
        Integer.getInteger("worldline.capture.saveTick", -1);
    @Unique private static final int WORLDLINE_DIRTY_TICK =
        Integer.getInteger("worldline.capture.dirtyTick", -1);
    @Unique private static final int WORLDLINE_DIRTY_CHUNKS =
        Integer.getInteger("worldline.capture.dirtyChunks", 0);
    @Unique private int worldlinePhase;
    @Unique private int worldlineTicks;
    @Unique private int worldlineY;
    @Unique private int worldlineWarmup;

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void worldlineCapture(CallbackInfo callback) {
        if (!WORLDLINE_ENABLED) return;
        if (worldlinePhase == 0 && world == null) {
            worldlinePhase = 1;
            interactionManager = new SingleplayerInteractionManager((Minecraft) (Object) this);
            System.out.println("[WorldlineCapture] start seed=" + WORLDLINE_SEED);
            worldlinePrepareDisplay();
            startGame("WorldlineAero", "Worldline Aero", WORLDLINE_SEED);
            worldlinePrepareDisplay();
            return;
        }
        if (world == null || player == null) return;
        if (worldlinePhase >= 3) return;
        worldlinePrepareDisplay();
        if (worldlinePhase == 2 && WorldlineFrameOracle.freeze(worldlineTicks)) {
            player.velocityX = 0.0D; player.velocityY = 0.0D; player.velocityZ = 0.0D;
            if (WORLDLINE_STABILIZE_SCENE) worldlineStabilizeScene();
            worldlinePlacePlayer();
            callback.cancel(); return;
        }
        if (worldlinePhase == 1) {
            worldlineY = WORLDLINE_Y;
            if (worldlineWarmup == 0) for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) world.getChunk(x, z);
        }
        player.velocityX = 0.0D; player.velocityY = 0.0D; player.velocityZ = 0.0D;
        worldlinePlacePlayer();
        if (worldlinePhase == 1) {
            worldlineWarmup++;
            if ((worldlineWarmup < WORLDLINE_MIN_WARMUP
                    || world.blockEntities.size() < WORLDLINE_MIN_BES)
                    && worldlineWarmup < 400) return;
            worldlinePhase = 2;
            System.out.println("[WorldlineCapture] ready blockEntities="
                    + world.blockEntities.size() + " entityBlocks="
                    + worldlineEntityBlocks() + " path=" + WORLDLINE_PATH
                    + " view=" + WORLDLINE_VIEW_DISTANCE);
        }
        worldlineTicks++;
        if (WORLDLINE_SAVE_TICK > 0 && worldlineTicks == WORLDLINE_SAVE_TICK)
            world.saveWithLoadingDisplay(false, null);
        if (WORLDLINE_DIRTY_TICK > 0 && worldlineTicks == WORLDLINE_DIRTY_TICK)
            System.out.println("[WorldlineCapture] dirtyChunks="
                    + WorldlineSaveForce.markDirty(world, WORLDLINE_DIRTY_CHUNKS));
        if (worldlineTicks >= WORLDLINE_TICKS) {
            System.out.println("[WorldlineCapture] complete ticks=" + worldlineTicks);
            worldlinePhase = 3;
            scheduleStop();
        }
    }

    @Unique
    private void worldlinePrepareDisplay() {
        Minecraft game = (Minecraft) (Object) this;
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = false;
        game.options.hideHud = true;
        game.options.bobView = false;
        if (WORLDLINE_VIEW_DISTANCE >= 0)
            game.options.viewDistance = Math.min(3, WORLDLINE_VIEW_DISTANCE);
    }

    @Unique
    private void worldlinePlacePlayer() {
        boolean look = "look".equals(WORLDLINE_PATH);
        int step = "moving".equals(WORLDLINE_PATH) ? Math.min(worldlineTicks, 60) : 0;
        double x = 8.5D + step * 0.25D, z = 8.5D + step * 0.125D;
        double y = worldlineY + (look && worldlineTicks % 10 < 5 ? 1.2D : 0.0D);
        float yaw = 45.0F + (look ? worldlineTicks * 12.0F : step * 2.0F);
        player.setPositionAndAngles(x, y, z, yaw, look ? 20.0F : 0.0F);
        WorldlineFrameOracle.pose(WORLDLINE_PATH, WORLDLINE_VIEW_DISTANCE, x, y, z, yaw);
    }

    @Unique
    private void worldlineStabilizeScene() {
        world.setTime(6000L);
        world.getProperties().setRaining(false);
        world.getProperties().setThundering(false);
        world.setRainGradient(0.0F);
        world.entities.retainAll(Collections.singleton(player));
        world.globalEntities.clear();
        ((Minecraft) (Object) this).raining = false;
    }

    @Unique
    private int worldlineEntityBlocks() {
        int count = 0;
        for (int x = -16; x < 32; x++) for (int z = -16; z < 32; z++)
            for (int y = 0; y < 128; y++) {
                int id = world.getBlockId(x, y, z);
                if (id > 0 && Block.BLOCKS[id] instanceof BlockWithEntity) count++;
            }
        return count;
    }
}
