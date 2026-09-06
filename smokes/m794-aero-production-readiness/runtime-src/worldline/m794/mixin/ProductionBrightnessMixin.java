package worldline.m794.mixin;
import aero.modellib.test.MegaModelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import worldline.m794.ProductionState;
/** Deterministic lighting signature changes exercise actual Cell Page invalidation. */
@Mixin(value = MegaModelBlockEntity.class, remap = false)
abstract class ProductionBrightnessMixin {
  @ModifyReturnValue(method = "aeroCellBrightness", at = @At("RETURN"))
  private float worldlineBrightness(float original) {
    if (ProductionState.scene() != 10)
      return original;
    return (ProductionState.localFrame() / 60 & 1) == 0 ? 0.5F : 1F;
  }
}
