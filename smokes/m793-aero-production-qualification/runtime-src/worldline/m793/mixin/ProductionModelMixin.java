package worldline.m793.mixin;
import aero.modellib.test.AnimatedMegaModelBlockEntity;
import aero.modellib.test.AnimatedMegaModelBlockEntityRenderer;
import aero.modellib.test.ProductionModels;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m793.ProductionState;
@Mixin(value = AnimatedMegaModelBlockEntityRenderer.class, remap = false)
abstract class ProductionModelMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void worldlineModel(BlockEntity block, double x, double y, double z, float delta, CallbackInfo callback) {
        if (!ProductionState.acquiring()) return;
        ProductionModels.render((AnimatedMegaModelBlockEntity) block, x, y, z, delta);
        callback.cancel();
    }
}
