package worldline.m788.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m788.OrderedTemplateProbe;
import worldline.m788.OrderedTemplateState;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class OrderedTemplateFrameMixin {
    @Shadow private Minecraft client;

    @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
    private float worldlineFixedDelta(float tickDelta) {
        return OrderedTemplateState.retaining() ? 1.0F : tickDelta;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
        OrderedTemplateState.frame(client);
        OrderedTemplateProbe.beginFrame();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineCaptureFrame(float tickDelta, CallbackInfo callback) {
        if (OrderedTemplateState.fixtureActive() && client.player != null) {
            double cameraX = client.player.x;
            double cameraY = client.player.y;
            double cameraZ = client.player.z;
            OrderedTemplateState.renderFixture(cameraX, cameraY, cameraZ);
        }
        OrderedTemplateProbe.sample(client);
    }
}
