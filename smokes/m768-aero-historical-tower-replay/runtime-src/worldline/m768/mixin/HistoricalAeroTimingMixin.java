package worldline.m768.mixin;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.WorldlineHistoricalCensus;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Measures real Aero enqueue and cell-page flush spans. */
@Mixin(Aero_BECellRenderer.class)
public abstract class HistoricalAeroTimingMixin {
    @Inject(method = "queueAtRest", at = @At("HEAD"), remap = false)
    private static void queueBegin(Aero_MeshModel model, String texture, BlockEntity be,
            double x, double y, double z, float rotation, float brightness,
            Aero_RenderOptions options, CallbackInfo callback) {
        WorldlineHistoricalCensus.enqueueBegin();
    }
    @Inject(method = "queueAtRest", at = @At("RETURN"), remap = false)
    private static void queueEnd(Aero_MeshModel model, String texture, BlockEntity be,
            double x, double y, double z, float rotation, float brightness,
            Aero_RenderOptions options, CallbackInfo callback) {
        WorldlineHistoricalCensus.enqueueEnd();
    }
    @Inject(method = "flush(DDD)V", at = @At("HEAD"), remap = false)
    private static void flushBegin(double x, double y, double z, CallbackInfo callback) {
        WorldlineHistoricalCensus.flushBegin();
    }
    @Inject(method = "flush(DDD)V", at = @At("RETURN"), remap = false)
    private static void flushEnd(double x, double y, double z, CallbackInfo callback) {
        WorldlineHistoricalCensus.flushEnd();
    }
}
