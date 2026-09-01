package worldline.m787.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m787.ColdEntryState;

@Mixin(value = WorldRenderer.class, priority = 1200)
public abstract class ColdEntryCloudMixin {
    @Inject(method = "renderClouds(F)V", at = @At("HEAD"), cancellable = true)
    private void worldlineHideClouds(float tickDelta, CallbackInfo callback) {
        if (ColdEntryState.ENABLED) callback.cancel();
    }

    @Inject(method = "renderFancyClouds(F)V", at = @At("HEAD"), cancellable = true)
    private void worldlineHideFancyClouds(float tickDelta, CallbackInfo callback) {
        if (ColdEntryState.ENABLED) callback.cancel();
    }
}
