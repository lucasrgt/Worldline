package worldline.m777.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m777.MatrixProbe;
import worldline.m777.MatrixState;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class MatrixFrameMetricsMixin {
    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineMeasureFrame(float tickDelta, CallbackInfo callback) {
        MatrixProbe.beginFrame(MatrixState.retaining(), MatrixState.phase());
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineMeasureRender(float tickDelta, CallbackInfo callback) {
        MatrixProbe.sampleRenderWorld();
    }
}
