package worldline.m793.mixin;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m793.ProductionTerrain;
@Mixin(WorldRenderer.class)
abstract class ProductionTerrainMixin {
    @Shadow private ChunkBuilder[] chunks;
    @Inject(method = "render", at = @At("RETURN"))
    private void worldlineTerrain(CallbackInfoReturnable<Integer> callback) {
        int dirty = 0;
        if (chunks != null) for (ChunkBuilder chunk : chunks)
            if (chunk != null && chunk.inFrustum && chunk.dirty) dirty++;
        ProductionTerrain.dirty(dirty);
    }
}
