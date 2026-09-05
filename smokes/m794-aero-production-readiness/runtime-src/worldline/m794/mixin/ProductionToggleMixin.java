package worldline.m794.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m794.ProductionState;
/** Optional target: the historical baseline must remain byte-for-byte unchanged. */
@Pseudo
@Mixin(targets = "aero.modellib.Aero_MultiPoseReuse", remap = false)
abstract class ProductionToggleMixin {
  @Inject(method = "enabled", at = @At("HEAD"), cancellable = true, require = 0)
  private static void worldlineToggle(CallbackInfoReturnable<Boolean> callback) {
    callback.setReturnValue(ProductionState.acquiring() && ProductionState.candidateEnabled());
  }
}
