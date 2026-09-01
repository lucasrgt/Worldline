package worldline.m787.mixin;

import aero.modellib.Aero_RenderDistanceBlockEntity;
import aero.modellib.test.MegaModelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m787.ColdEntryState;

/** Keeps fixture machines out of vanilla block-entity dispatch. */
@Mixin(value = Aero_RenderDistanceBlockEntity.class, priority = 1200)
public abstract class ColdEntryDistanceMixin {
    @Inject(method = "distanceFrom(DDD)D", at = @At("HEAD"), cancellable = true)
    private void worldlineSuppressFixture(double x, double y, double z,
            CallbackInfoReturnable<Double> callback) {
        if (ColdEntryState.ENABLED && (Object) this instanceof MegaModelBlockEntity) {
            callback.setReturnValue(Double.POSITIVE_INFINITY);
        }
    }
}
