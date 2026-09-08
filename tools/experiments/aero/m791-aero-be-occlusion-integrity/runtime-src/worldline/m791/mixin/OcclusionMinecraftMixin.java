package worldline.m791.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m791.OcclusionState;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class OcclusionMinecraftMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void worldlinePreventCaptureMenu(CallbackInfo callback) {
        if (OcclusionState.retaining()) callback.cancel();
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void worldlineDrive(CallbackInfo callback) {
        try {
            OcclusionState.drive((Minecraft) (Object) this);
            if (OcclusionState.freezeTicks()) callback.cancel();
        } catch (RuntimeException error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
