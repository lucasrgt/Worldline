package worldline.m794.mixin;
import aero.modellib.test.MegaModelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m794.ProductionState;
/** Deterministic lighting signature changes exercise actual Cell Page invalidation. */
@Mixin(value = MegaModelBlockEntity.class, remap = false)
abstract class ProductionBrightnessMixin {
  @Inject(method = "aeroCellBrightness", at = @At("RETURN"), cancellable = true)
  private void worldlineBrightness(CallbackInfoReturnable<Float> callback) {
    if (ProductionState.scene() == 10)
      callback.setReturnValue((ProductionState.localFrame() / 60 & 1) == 0 ? 0.5F : 1F);
  }
}
