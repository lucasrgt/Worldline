package worldline.m792.mixin;

import aero.modellib.Aero_AnimatedBatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m792.OcclusionStages;

@Mixin(value = Aero_AnimatedBatcher.class, remap = false)
public abstract class OcclusionBatchMixin {
  @Inject(method = "flush", at = @At("HEAD"))
  private static void worldlineBegin(CallbackInfo callback) {
    OcclusionStages.begin(0);
  }

  @Inject(method = "flush", at = @At("RETURN"))
  private static void worldlineEnd(CallbackInfo callback) {
    OcclusionStages.end(0);
  }
}
