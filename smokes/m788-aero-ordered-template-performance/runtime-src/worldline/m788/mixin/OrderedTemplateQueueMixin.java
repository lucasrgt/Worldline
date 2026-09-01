package worldline.m788.mixin;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.Aero_CellPageRenderableBE;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;
import aero.modellib.test.MegaModelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m788.OrderedTemplateState;

/** Rejects duplicate fixture producers at the public production queue boundary. */
@Mixin(value = Aero_BECellRenderer.class, priority = 1200, remap = false)
public abstract class OrderedTemplateQueueMixin {
    @Inject(method = "tryQueueManagedAtRest", at = @At("HEAD"), cancellable = true,
        remap = false)
    private static void worldlineIsolateManagedQueue(BlockEntity block,
            Aero_CellPageRenderableBE renderable, CallbackInfoReturnable<Boolean> callback) {
        if (OrderedTemplateState.ENABLED && block instanceof MegaModelBlockEntity) {
            callback.setReturnValue(Boolean.FALSE);
        }
    }

    @Inject(method = "queueAtRest", at = @At("HEAD"), cancellable = true, remap = false)
    private static void worldlineIsolateFixtureQueue(Aero_MeshModel model, String texturePath,
            BlockEntity block, double x, double y, double z, float rotation, float brightness,
            Aero_RenderOptions options, CallbackInfo callback) {
        if (OrderedTemplateState.ENABLED && block instanceof MegaModelBlockEntity
                && !OrderedTemplateState.controlledSubmission()) callback.cancel();
    }
}
