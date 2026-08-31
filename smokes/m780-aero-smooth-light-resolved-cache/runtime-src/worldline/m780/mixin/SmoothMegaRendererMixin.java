package worldline.m780.mixin;

import aero.modellib.Aero_MeshRenderer;
import aero.modellib.test.MegaModelBlockEntityRenderer;
import aero.modellib.test.SmoothLightContract;
import aero.modellib.test.WorldlineM780Rehydrator;
import aero.modellib.model.Aero_MeshModel;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.opengl.GL11;
import worldline.m780.SmoothLightProbe;
import worldline.m780.SmoothLightState;

@Mixin(value = MegaModelBlockEntityRenderer.class, priority = 1200)
public abstract class SmoothMegaRendererMixin {
    private static final Aero_MeshModel WORLDLINE_SMOOTH_MODEL =
        SmoothLightContract.denseGrid();

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;DDDF)V",
        at = @At("HEAD"), cancellable = true)
    private void worldlineRender(BlockEntity block, double x, double y, double z,
            float tickDelta, CallbackInfo callback) {
        if (!SmoothLightState.ENABLED) return;
        if (!WorldlineM780Rehydrator.contains(block.x, block.y, block.z)) {
            callback.cancel();
            return;
        }
        SmoothLightProbe.beginScene();
        SmoothLightProbe.beginModel(block.x, block.y, block.z);
        SmoothLightProbe.renderCall();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_TEXTURE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        try {
            Aero_MeshRenderer.renderModel(WORLDLINE_SMOOTH_MODEL,
                x, y, z, 0.0F, block.world, block.x, block.y + 1, block.z);
        } finally {
            GL11.glPopAttrib();
        }
        callback.cancel();
    }
}
