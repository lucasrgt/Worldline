package worldline.m74.mixin;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.WorldlineStageTimer;

/** Times Aero enqueue and flush boundaries without retaining per-call objects. */
@Mixin(Aero_BECellRenderer.class)
public abstract class WorldlineStageAeroMixin {
    @Inject(method = "queueAtRest", at = @At("HEAD"), remap = false) private static void queueBegin(Aero_MeshModel model, String texture, BlockEntity be,
            double x, double y, double z, float rotation, float brightness, Aero_RenderOptions options, CallbackInfo ci) { WorldlineStageTimer.queueBegin(); }
    @Inject(method = "queueAtRest", at = @At("RETURN"), remap = false) private static void queueEnd(Aero_MeshModel model, String texture, BlockEntity be,
            double x, double y, double z, float rotation, float brightness, Aero_RenderOptions options, CallbackInfo ci) { WorldlineStageTimer.queueEnd(); }
    @Inject(method = "flush(DDD)V", at = @At("HEAD"), remap = false) private static void flushBegin(double x, double y, double z, CallbackInfo ci) { WorldlineStageTimer.flushBegin(); }
    @Inject(method = "flush(DDD)V", at = @At("RETURN"), remap = false) private static void flushEnd(double x, double y, double z, CallbackInfo ci) { WorldlineStageTimer.flushEnd(); }
}
