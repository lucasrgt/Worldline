package worldline.m789.mixin;

import aero.modellib.Aero_BECellRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m789.QueueReuseProbe;
import worldline.m789.QueueReuseState;

/** Enforces the fixture's single controlled production flush boundary. */
@Mixin(value = Aero_BECellRenderer.class, priority = 1300, remap = false)
public abstract class QueueReuseFlushMixin {
    @Inject(method = "flush", at = @At("HEAD"), cancellable = true, remap = false)
    private static void worldlineSuppressAutomaticFlush(double x, double y, double z,
            CallbackInfo callback) {
        if (QueueReuseState.fixtureActive() && !QueueReuseProbe.controlledFlush()) {
            callback.cancel();
        }
    }
}
