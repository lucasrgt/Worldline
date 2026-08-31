package worldline.m783.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m783.VisualState;

@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class VisualFrameDriveMixin {
    @Shadow private Minecraft client;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
        VisualState.frame(client);
    }
}
