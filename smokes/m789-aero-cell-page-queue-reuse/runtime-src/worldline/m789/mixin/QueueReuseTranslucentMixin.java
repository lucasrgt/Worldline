package worldline.m789.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m789.QueueReuseState;

@Mixin(value = WorldRenderer.class, priority = 1200)
public abstract class QueueReuseTranslucentMixin {
    @Inject(method = "renderChunks(IIID)I", at = @At("HEAD"), cancellable = true)
    private void worldlineHideTranslucentTerrain(int start, int end, int pass,
            double tickDelta, CallbackInfoReturnable<Integer> callback) {
        if (QueueReuseState.ENABLED && pass == 1) callback.setReturnValue(0);
    }
}
