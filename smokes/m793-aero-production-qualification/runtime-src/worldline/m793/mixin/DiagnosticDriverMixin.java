package worldline.m793.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m793.ProductionStages;
/** Separate diagnostic session only; durations are inclusive CPU call costs. */
@Pseudo
@Mixin(targets = "net.minecraft.client.render.Tessellator", remap = false)
abstract class DiagnosticDriverMixin {
    @Inject(method = "draw", at = @At("HEAD"))
    private void worldlineBegin(CallbackInfo callback) { ProductionStages.begin(6); }
    @Inject(method = "draw", at = @At("RETURN"))
    private void worldlineEnd(CallbackInfo callback) { ProductionStages.end(6); }
}
