package worldline.m780.mixin;

import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_TextureBinder;
import aero.modellib.test.MegaModelBlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m780.SmoothLightProbe;
import worldline.m780.SmoothLightState;

@Mixin(value = MegaModelBlockEntityRenderer.class, priority = 1200)
public abstract class SmoothMegaRendererMixin {
    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;DDDF)V",
        at = @At("HEAD"), cancellable = true)
    private void worldlineRender(BlockEntity block, double x, double y, double z,
            float tickDelta, CallbackInfo callback) {
        if (!SmoothLightState.ENABLED) return;
        SmoothLightProbe.renderCall();
        Aero_TextureBinder.bind(MegaModelBlockEntityRenderer.TEXTURE);
        Aero_MeshRenderer.renderModel(MegaModelBlockEntityRenderer.MODEL,
            x, y, z, 0.0F, block.world, block.x, block.y + 1, block.z);
        callback.cancel();
    }
}
