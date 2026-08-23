package worldline.m74.mixin;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import worldline.m74.client.*;

/** Preserves or suppresses the exact Aero boundary for the dispatch treatments. */
@Mixin(WorldlineCensusRenderer.class)
public abstract class WorldlineDecompositionRendererMixin {
  @Redirect(method = "render",
      at = @At(value = "INVOKE",
          target =
              "Laero/modellib/Aero_BECellRenderer;queueAtRest(Laero/modellib/model/Aero_MeshModel;Ljava/lang/String;Lnet/minecraft/block/entity/BlockEntity;DDDFFLaero/modellib/render/Aero_RenderOptions;)V"))
  private void
  worldline$decompose(Aero_MeshModel model, String texture, BlockEntity be, double x, double y,
      double z, float rotation, float brightness, Aero_RenderOptions options) {
    if (WorldlineDecompositionClient.treatment().equals("aero16"))
      Aero_BECellRenderer.queueAtRest(model, texture, be, x, y, z, rotation, brightness, options);
  }
}
