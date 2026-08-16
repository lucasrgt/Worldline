package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlineChunkProbe;
import aero.modellib.test.worldline.WorldlineChunkGeometry;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Counts real rebuild and invalidation calls while the M14 probe is active. */
@Mixin(ChunkBuilder.class)
public abstract class WorldlineChunkBuilderProbeMixin {
    @Inject(method = "rebuild()V", at = @At("HEAD"))
    private void worldlineRebuilt(CallbackInfo callback) {
        WorldlineChunkProbe.rebuilt();
        WorldlineChunkGeometry.begin((ChunkBuilder) (Object) this);
    }

    @Inject(method = "rebuild()V", at = @At("RETURN"))
    private void worldlineGeometry(CallbackInfo callback) {
        WorldlineChunkGeometry.end((ChunkBuilder) (Object) this);
    }

    @Inject(method = "invalidate()V", at = @At("HEAD"))
    private void worldlineInvalidated(CallbackInfo callback) { WorldlineChunkProbe.invalidated(); }
}
