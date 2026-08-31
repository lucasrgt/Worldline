package worldline.m779.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m779.AnimatedVisualState;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class AnimatedMinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void worldlineDrive(CallbackInfo callback) {
        try {
            AnimatedVisualState.drive((Minecraft) (Object) this);
            if (AnimatedVisualState.freezeTicks()) callback.cancel();
        } catch (RuntimeException error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
