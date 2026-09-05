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
@Mixin(targets = "aero.modellib.Aero_TessellatorBulkWriter", remap = false)
abstract class DiagnosticUniqueMixin {
    @Inject(method = "writeUniquePose", at = @At("HEAD"))
    private static void worldlineBegin(CallbackInfoReturnable<Boolean> callback) { ProductionStages.begin(5); }
    @Inject(method = "writeUniquePose", at = @At("RETURN"))
    private static void worldlineEnd(CallbackInfoReturnable<Boolean> callback) { ProductionStages.end(5); }
}
