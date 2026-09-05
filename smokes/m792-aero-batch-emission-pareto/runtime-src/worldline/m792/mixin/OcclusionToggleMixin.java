package worldline.m792.mixin;

import aero.modellib.Aero_BatchEmissionCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m792.OcclusionState;

/** Counterbalanced override of the default-off emission candidate. */
@Mixin(value = Aero_BatchEmissionCache.class, remap = false)
abstract class OcclusionToggleMixin {
    @Inject(method = "enabled", at = @At("HEAD"), cancellable = true)
    private static void worldlineToggle(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(OcclusionState.retaining() && OcclusionState.candidateEnabled());
    }
}
