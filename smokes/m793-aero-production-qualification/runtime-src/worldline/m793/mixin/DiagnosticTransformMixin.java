package worldline.m793.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m793.ProductionStages;
/** Separate diagnostic session only; durations are inclusive CPU call costs. */
@Pseudo
@Mixin(targets = "aero.modellib.Aero_VertexEmissionBucket", remap = false)
abstract class DiagnosticTransformMixin {
    @Inject(method = {"preparePose", "prepareRest"}, at = @At("HEAD"))
    private void worldlineBegin(CallbackInfo callback) { ProductionStages.begin(3); }
    @Inject(method = {"preparePose", "prepareRest"}, at = @At("RETURN"))
    private void worldlineEnd(CallbackInfo callback) { ProductionStages.end(3); }
}
