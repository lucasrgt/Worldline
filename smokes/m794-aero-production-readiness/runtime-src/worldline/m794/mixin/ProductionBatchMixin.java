package worldline.m794.mixin;

import aero.modellib.Aero_AnimatedBatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m794.ProductionStages;

@Mixin(value = Aero_AnimatedBatcher.class, remap = false)
public abstract class ProductionBatchMixin {
  @Inject(method = "flush", at = @At("HEAD"))
  private static void worldlineBegin(CallbackInfo callback) {
    ProductionStages.begin(0);
  }

  @Inject(method = "flush", at = @At("RETURN"))
  private static void worldlineEnd(CallbackInfo callback) {
    ProductionStages.end(0);
  }
}
