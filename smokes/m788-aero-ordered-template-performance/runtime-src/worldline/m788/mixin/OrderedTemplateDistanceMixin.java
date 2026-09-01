package worldline.m788.mixin;

import aero.modellib.Aero_RenderDistanceBlockEntity;
import aero.modellib.test.MegaModelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m788.OrderedTemplateState;

/** Keeps the fixture out of vanilla dispatch so only the controlled queue submits it. */
@Mixin(value = Aero_RenderDistanceBlockEntity.class, priority = 1200)
public abstract class OrderedTemplateDistanceMixin {
    @Inject(method = "distanceFrom(DDD)D", at = @At("HEAD"), cancellable = true)
    private void worldlineSuppressNativeFixture(double x, double y, double z,
            CallbackInfoReturnable<Double> callback) {
        if (OrderedTemplateState.ENABLED && (Object) this instanceof MegaModelBlockEntity) {
            callback.setReturnValue(Double.POSITIVE_INFINITY);
        }
    }
}
