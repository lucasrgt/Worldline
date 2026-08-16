package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlineChunkProbe;
import aero.modellib.test.worldline.WorldlineChunkReadiness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Opens and publishes one chunk-probe record per rendered client frame. */
@Mixin(GameRenderer.class)
public abstract class WorldlineChunkProbeFrameMixin {
    @Shadow private Minecraft client;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineProbeBegin(float tickDelta, CallbackInfo callback) {
        WorldlineChunkProbe.beginFrame();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineProbeEnd(float tickDelta, CallbackInfo callback) {
        WorldlineChunkReadiness.observe(client.worldRenderer);
        WorldlineChunkProbe.endFrame();
    }
}
