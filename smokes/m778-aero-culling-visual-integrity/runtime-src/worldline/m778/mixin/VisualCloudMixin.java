package worldline.m778.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m778.VisualState;

/** Removes the client-clock cloud layer from M778's framebuffer oracle. */
@Mixin(value = WorldRenderer.class, priority = 1200)
public abstract class VisualCloudMixin {
    @Inject(method = "renderClouds(F)V", at = @At("HEAD"), cancellable = true)
    private void worldlineFreezeClouds(float tickDelta, CallbackInfo callback) {
        if (VisualState.ENABLED) callback.cancel();
    }

    @Inject(method = "renderFancyClouds(F)V", at = @At("HEAD"), cancellable = true)
    private void worldlineFreezeFancyClouds(float tickDelta, CallbackInfo callback) {
        if (VisualState.ENABLED) callback.cancel();
    }
}
