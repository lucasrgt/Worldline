package worldline.m773.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m773.SchedulerState;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class SchedulerMinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineDrive(CallbackInfo callback) {
        try {
            SchedulerState.drive((Minecraft) (Object) this);
        } catch (RuntimeException error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
