package worldline.m777.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m777.MatrixState;

@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class MatrixFrameDriveMixin {
    @Shadow private Minecraft client;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
        MatrixState.frame(client);
    }
}
