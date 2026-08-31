package worldline.m784.mixin;

import aero.modellib.test.MegaModelBlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m784.HighMemoryState;

@Mixin(value = MegaModelBlockEntityRenderer.class, priority = 1200)
public abstract class HighMemoryMegaRendererMixin {
    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;DDDF)V",
        at = @At("HEAD"), cancellable = true)
    private void worldlineBeginScene(BlockEntity block, double x, double y, double z,
            float tickDelta, CallbackInfo callback) {
        if (HighMemoryState.ENABLED) callback.cancel();
    }
}
