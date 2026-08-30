package worldline.m768.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m768.WorldlineHistoricalState;

/** Drives one fresh local-world M768 arm from the real client tick. */
@Mixin(value = Minecraft.class, priority = 1500)
public abstract class HistoricalMinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineHistoricalTick(CallbackInfo callback) {
        WorldlineHistoricalState.drive((Minecraft) (Object) this);
    }
}
