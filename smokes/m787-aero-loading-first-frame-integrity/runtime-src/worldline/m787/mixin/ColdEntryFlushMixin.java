package worldline.m787.mixin;

import aero.modellib.Aero_BECellRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m787.ColdEntryProbe;
import worldline.m787.ColdEntryState;

/** Enforces one controlled Cell Page flush after complete fixture submission. */
@Mixin(value = Aero_BECellRenderer.class, priority = 1300, remap = false)
public abstract class ColdEntryFlushMixin {
    @Inject(method = "flush", at = @At("HEAD"), cancellable = true, remap = false)
    private static void worldlineSuppressAutomaticFlush(double x, double y, double z,
            CallbackInfo callback) {
        if (ColdEntryState.fixtureActive() && !ColdEntryProbe.controlledFlush()) callback.cancel();
    }
}
