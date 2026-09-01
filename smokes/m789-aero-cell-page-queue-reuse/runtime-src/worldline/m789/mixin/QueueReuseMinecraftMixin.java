package worldline.m789.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m789.QueueReuseState;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class QueueReuseMinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void worldlineDrive(CallbackInfo callback) {
        try {
            QueueReuseState.drive((Minecraft) (Object) this);
            if (QueueReuseState.freezeTicks()) callback.cancel();
        } catch (RuntimeException error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
