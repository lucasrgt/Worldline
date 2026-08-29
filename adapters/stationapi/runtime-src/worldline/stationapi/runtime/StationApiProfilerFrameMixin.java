package worldline.stationapi.runtime;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.profiling.WorldlineProfilerMetrics;

/** Defines the renderer update as the complete client-frame capture boundary. */
@Mixin(GameRenderer.class)
public abstract class StationApiProfilerFrameMixin {
    @Unique private long worldlineWorldStart;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineFrameBegin(float tickDelta, CallbackInfo callback) {
        StationApiProfilerRuntime.beginFrame();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("RETURN"))
    private void worldlineFrameEnd(float tickDelta, CallbackInfo callback) {
        StationApiProfilerRuntime.endFrame();
    }

    @Inject(method = "renderWorld(FI)V", at = @At("HEAD"))
    private void worldlineWorldBegin(float tickDelta, int eye, CallbackInfo callback) {
        worldlineWorldStart = StationApiProfilerRuntime.timer();
    }

    @Inject(method = "renderWorld(FI)V", at = @At("RETURN"))
    private void worldlineWorldEnd(float tickDelta, int eye, CallbackInfo callback) {
        StationApiProfilerRuntime.elapsed(WorldlineProfilerMetrics.RENDER_WORLD,
                worldlineWorldStart);
    }
}
