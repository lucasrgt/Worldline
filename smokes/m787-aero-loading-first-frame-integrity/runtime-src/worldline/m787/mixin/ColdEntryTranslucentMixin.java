package worldline.m787.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m787.ColdEntryState;

@Mixin(value = WorldRenderer.class, priority = 1200)
public abstract class ColdEntryTranslucentMixin {
    @Inject(method = "renderChunks(IIID)I", at = @At("HEAD"), cancellable = true)
    private void worldlineHideTranslucentTerrain(int start, int end, int pass,
            double tickDelta, CallbackInfoReturnable<Integer> callback) {
        if (ColdEntryState.ENABLED && pass == 1) callback.setReturnValue(0);
    }
}
