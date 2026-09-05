package worldline.m793.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m793.ProductionProbe;
import worldline.m793.ProductionTerrain;
import worldline.m793.ProductionProfile;
import worldline.m793.ProductionState;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class ProductionFrameMixin {
    @Shadow private Minecraft client;

    @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
    private float worldlineFixedDelta(float tickDelta) {
        return ProductionState.retaining() ? 1.0F : tickDelta;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineDriveFrame(float tickDelta, CallbackInfo callback) {
        ProductionState.frame(client);
        ProductionProfile.begin();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineCaptureFrame(float tickDelta, CallbackInfo callback) {
        ProductionProfile.sample();
        ProductionProbe.sample(client);
        ProductionTerrain.sample(client);
    }
}
