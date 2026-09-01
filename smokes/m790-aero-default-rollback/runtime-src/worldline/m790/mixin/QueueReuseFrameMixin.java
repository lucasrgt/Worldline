package worldline.m790.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m790.QueueReuseProbe;
import worldline.m790.QueueReuseState;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class QueueReuseFrameMixin {
    @Shadow private Minecraft client;

    @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
    private float worldlineFixedDelta(float tickDelta) {
        return QueueReuseState.retaining() ? 1.0F : tickDelta;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
        QueueReuseState.frame(client);
        QueueReuseProbe.beginFrame();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineCaptureFrame(float tickDelta, CallbackInfo callback) {
        if (QueueReuseState.fixtureActive() && client.player != null) {
            double cameraX = client.player.x;
            double cameraY = client.player.y;
            double cameraZ = client.player.z;
            QueueReuseState.renderFixture(cameraX, cameraY, cameraZ);
        }
        QueueReuseProbe.sample(client);
    }
}
