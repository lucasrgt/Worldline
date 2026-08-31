package worldline.m783.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m783.VisualProbe;
import worldline.m783.VisualState;

@Mixin(value = GameRenderer.class, priority = 900)
public abstract class VisualFrameMetricsMixin {
    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineMeasureFrame(float tickDelta, CallbackInfo callback) {
        VisualProbe.beginFrame(VisualState.retaining(), VisualState.phase(),
                VisualState.frameIndex());
    }
}
