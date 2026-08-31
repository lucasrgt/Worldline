package worldline.m784.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m784.HighMemoryProbe;
import worldline.m784.HighMemoryState;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class HighMemoryFrameMixin {
    @Shadow private Minecraft client;

    @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
    private float worldlineFixedDelta(float tickDelta) {
        return HighMemoryState.retaining() ? 1.0F : tickDelta;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
        HighMemoryState.frame(client);
        HighMemoryProbe.beginFrame();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineCaptureFrame(float tickDelta, CallbackInfo callback) {
        if (HighMemoryState.fixtureActive() && client.player != null) {
            Vec3d camera = client.player.getCameraPos(1.0F);
            HighMemoryState.submitFixture(camera.x, camera.y, camera.z);
            HighMemoryProbe.flush(camera.x, camera.y, camera.z);
        }
        HighMemoryProbe.sample(client);
    }
}
