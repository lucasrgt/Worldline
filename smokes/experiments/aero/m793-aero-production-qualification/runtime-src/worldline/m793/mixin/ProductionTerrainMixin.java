package worldline.m793.mixin;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m793.ProductionTerrain;
import worldline.m793.ProductionState;
@Mixin(WorldRenderer.class)
abstract class ProductionTerrainMixin {
    @Shadow private ChunkBuilder[] chunks;
    @Shadow private ChunkBuilder[] sortedChunks;
    @Shadow double prevCameraX, prevCameraY, prevCameraZ;
    private int worldlineLastReset = -1;

    /** Replays the same native chunk-sort starting state in every arm. */
    @Inject(method = "render", at = @At("HEAD"))
    private void worldlineResetSort(CallbackInfoReturnable<Integer> callback) {
        if (!ProductionState.acquiring() || ProductionState.localFrame() != 0
                || worldlineLastReset == ProductionState.frameId()) return;
        worldlineLastReset = ProductionState.frameId();
        System.arraycopy(chunks, 0, sortedChunks, 0, chunks.length);
        prevCameraX = prevCameraY = prevCameraZ = -9999D;
    }
    @Inject(method = "render", at = @At("RETURN"))
    private void worldlineTerrain(CallbackInfoReturnable<Integer> callback) {
        int dirty = 0;
        if (chunks != null) for (ChunkBuilder chunk : chunks)
            if (chunk != null && chunk.inFrustum && chunk.dirty) dirty++;
        ProductionTerrain.dirty(dirty);
    }
}
