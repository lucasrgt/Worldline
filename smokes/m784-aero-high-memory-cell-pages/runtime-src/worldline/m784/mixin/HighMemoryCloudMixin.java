package worldline.m784.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m784.HighMemoryState;

@Mixin(value = WorldRenderer.class, priority = 1200)
public abstract class HighMemoryCloudMixin {
    @Inject(method = "renderClouds(F)V", at = @At("HEAD"), cancellable = true)
    private void worldlineFreezeClouds(float tickDelta, CallbackInfo callback) {
        if (HighMemoryState.ENABLED) callback.cancel();
    }

    @Inject(method = "renderFancyClouds(F)V", at = @At("HEAD"), cancellable = true)
    private void worldlineFreezeFancyClouds(float tickDelta, CallbackInfo callback) {
        if (HighMemoryState.ENABLED) callback.cancel();
    }
}
