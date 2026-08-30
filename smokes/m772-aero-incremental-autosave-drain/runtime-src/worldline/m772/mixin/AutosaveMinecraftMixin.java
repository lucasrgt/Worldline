package worldline.m772.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m772.AutosaveState;

/** Drives M772 immediately around each official client tick. */
@Mixin(value = Minecraft.class, priority = 900)
public abstract class AutosaveMinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineBeforeTick(CallbackInfo callback) throws Exception {
        AutosaveState.beforeTick((Minecraft) (Object) this);
    }
}
