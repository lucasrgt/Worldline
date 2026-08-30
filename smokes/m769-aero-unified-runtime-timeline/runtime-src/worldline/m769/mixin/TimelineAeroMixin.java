package worldline.m769.mixin;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m769.TimelineAeroMetrics;

/** Publishes real Aero enqueue and page-flush spans into the generic schema. */
@Mixin(Aero_BECellRenderer.class)
public abstract class TimelineAeroMixin {
    @Unique private static long worldlineEnqueueStart;
    @Unique private static long worldlineFlushStart;

    @Inject(method = "queueAtRest", at = @At("HEAD"), remap = false)
    private static void worldlineQueueBegin(Aero_MeshModel model, String texture,
            BlockEntity blockEntity, double x, double y, double z, float rotation,
            float brightness, Aero_RenderOptions options, CallbackInfo callback) {
        worldlineEnqueueStart = TimelineAeroMetrics.begin();
    }

    @Inject(method = "queueAtRest", at = @At("RETURN"), remap = false)
    private static void worldlineQueueEnd(Aero_MeshModel model, String texture,
            BlockEntity blockEntity, double x, double y, double z, float rotation,
            float brightness, Aero_RenderOptions options, CallbackInfo callback) {
        TimelineAeroMetrics.endEnqueue(worldlineEnqueueStart);
        worldlineEnqueueStart = 0L;
    }

    @Inject(method = "flush(DDD)V", at = @At("HEAD"), remap = false)
    private static void worldlineFlushBegin(double x, double y, double z,
            CallbackInfo callback) {
        worldlineFlushStart = TimelineAeroMetrics.begin();
    }

    @Inject(method = "flush(DDD)V", at = @At("RETURN"), remap = false)
    private static void worldlineFlushEnd(double x, double y, double z,
            CallbackInfo callback) {
        TimelineAeroMetrics.endFlush(worldlineFlushStart);
        worldlineFlushStart = 0L;
    }
}
