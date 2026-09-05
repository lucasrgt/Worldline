package worldline.m793.mixin;

import aero.modellib.Aero_BECellRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m793.ProductionStages;

@Mixin(value = Aero_BECellRenderer.class, remap = false)
public abstract class ProductionPageMixin {
    @Inject(method = "flush", at = @At("HEAD"))
    private static void worldlineBegin(CallbackInfo callback) { ProductionStages.begin(1); }

    @Inject(method = "flush", at = @At("RETURN"))
    private static void worldlineEnd(CallbackInfo callback) { ProductionStages.end(1); }
}
