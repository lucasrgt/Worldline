package worldline.m787.mixin;

import net.minecraft.client.render.ProgressRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m787.ColdEntryLoadTrace;

/** Observes the vanilla progress display without changing it. */
@Mixin(value = ProgressRenderer.class, priority = 1200)
public abstract class ColdEntryProgressMixin {
    @Inject(method = "progressStart(Ljava/lang/String;)V", at = @At("HEAD"))
    private void worldlineTitle(String value, CallbackInfo callback) {
        ColdEntryLoadTrace.title(value);
    }

    @Inject(method = "progressStage(Ljava/lang/String;)V", at = @At("HEAD"))
    private void worldlineStage(String value, CallbackInfo callback) {
        ColdEntryLoadTrace.stage(value);
    }
}
