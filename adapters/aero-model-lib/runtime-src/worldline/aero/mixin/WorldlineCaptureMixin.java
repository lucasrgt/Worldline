package worldline.aero.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.InteractionManager;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import worldline.aero.WorldlineSaveForce;
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

    @Unique private int worldlinePhase;
    @Unique private int worldlineTicks;
    @Unique private int worldlineY;
    @Unique private int worldlineWarmup;
    @Unique private boolean worldlineDisplayReady;

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void worldlineCapture(CallbackInfo callback) {
        if (!WorldlineCaptureSettings.ENABLED) return;
        if (worldlinePhase == 0 && world == null) {
            worldlinePhase = 1;
            interactionManager = new SingleplayerInteractionManager((Minecraft) (Object) this);
            System.out.println("[WorldlineCapture] start seed=" + WorldlineCaptureSettings.SEED);
            worldlinePrepareDisplay();
            startGame("WorldlineAero", "Worldline Aero", WorldlineCaptureSettings.SEED);
            worldlinePrepareDisplay();
            return;
        }
        if (world == null || player == null) return;
        if (worldlinePhase >= 3) return;
        worldlinePrepareDisplay();
        if (worldlinePhase == 2 && WorldlineFrameOracle.freeze(worldlineTicks)) {
            player.velocityX = 0.0D; player.velocityY = 0.0D; player.velocityZ = 0.0D;
            if (WorldlineCaptureSettings.STABILIZE_SCENE) worldlineStabilizeScene();
            worldlinePlacePlayer();
            callback.cancel(); return;
        }
        if (worldlinePhase == 1) {
            worldlineY = WorldlineCaptureSettings.Y;
            if (worldlineWarmup == 0) for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) world.getChunk(x, z);
        }
        player.velocityX = 0.0D; player.velocityY = 0.0D; player.velocityZ = 0.0D;
        worldlinePlacePlayer();
        if (!worldlineDisplayReady) {
            worldlineDisplayReady = true;
            worldlinePrepareDisplay();
        }
        if (worldlinePhase == 1) {
            worldlineWarmup++;
            if ((worldlineWarmup < WorldlineCaptureSettings.MIN_WARMUP_TICKS
                    || world.blockEntities.size() < WorldlineCaptureSettings.MIN_BLOCK_ENTITIES)
                    && worldlineWarmup < 400) return;
            worldlinePhase = 2;
            System.out.println("[WorldlineCapture] ready blockEntities="
                    + world.blockEntities.size() + " entityBlocks="
                    + worldlineEntityBlocks() + " path=" + WorldlineCaptureSettings.PATH
                    + " view=" + WorldlineCaptureSettings.VIEW_DISTANCE);
        }
        worldlineTicks++;
        if (WorldlineCaptureSettings.SAVE_TICK > 0
                && worldlineTicks == WorldlineCaptureSettings.SAVE_TICK)
            world.saveWithLoadingDisplay(false, null);
        if (WorldlineCaptureSettings.DIRTY_TICK > 0
                && worldlineTicks == WorldlineCaptureSettings.DIRTY_TICK)
            System.out.println("[WorldlineCapture] dirtyChunks="
                    + WorldlineSaveForce.markDirty(world, WorldlineCaptureSettings.DIRTY_CHUNKS));
        if (worldlineTicks >= WorldlineCaptureSettings.TICKS) {
            System.out.println("[WorldlineCapture] complete ticks=" + worldlineTicks);
            worldlinePhase = 3;
            scheduleStop();
        }
    }

    @Unique
    private void worldlinePrepareDisplay() {
        WorldlineCaptureScene.prepareDisplay((Minecraft) (Object) this, worldlineDisplayReady);
    }

    @Unique
    private void worldlinePlacePlayer() {
        WorldlineCaptureScene.placePlayer(player, worldlineY, worldlineTicks);
    }

    @Unique
    private void worldlineStabilizeScene() {
        WorldlineCaptureScene.stabilize((Minecraft) (Object) this, world, player);
    }

    @Unique
    private int worldlineEntityBlocks() {
        return WorldlineCaptureScene.entityBlocks(world);
    }
}
