package worldline.m779.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m779.AnimatedVisualProbe;
import worldline.m779.AnimatedVisualState;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class AnimatedFrameMixin {
    @Shadow private Minecraft client;

    @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
    private float worldlineFixedDelta(float tickDelta) {
        return AnimatedVisualState.retaining() ? 1.0F : tickDelta;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
        AnimatedVisualState.frame(client);
        AnimatedVisualProbe.beginFrame();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineCaptureFrame(float tickDelta, CallbackInfo callback) {
        AnimatedVisualProbe.sample(client);
    }
}
