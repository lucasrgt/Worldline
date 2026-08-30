package worldline.m773.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m773.SchedulerProbe;
import worldline.m773.SchedulerState;

@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class SchedulerFrameMixin {
    @Shadow private Minecraft client;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineFrameBegin(float tickDelta, CallbackInfo callback) {
        SchedulerProbe.beginFrame(SchedulerState.retaining());
        SchedulerState.frame(client);
    }
}
