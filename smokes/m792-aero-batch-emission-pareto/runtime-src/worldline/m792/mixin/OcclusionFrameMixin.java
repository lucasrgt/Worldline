package worldline.m792.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m792.OcclusionProbe;
import worldline.m792.OcclusionProfile;
import worldline.m792.OcclusionState;
import worldline.m792.ParetoFramebuffer;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class OcclusionFrameMixin {
  @Shadow private Minecraft client;

  @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
  private float worldlineFixedDelta(float tickDelta) {
    return OcclusionState.retaining() ? 1.0F : tickDelta;
  }

  @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
  private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
    OcclusionState.frame(client);
    ParetoFramebuffer.begin(client);
    OcclusionProfile.begin();
  }

  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void worldlineCaptureFrame(float tickDelta, CallbackInfo callback) {
    OcclusionProfile.sample();
    OcclusionProbe.sample(client);
    ParetoFramebuffer.end();
  }
}
