package worldline.m74.mixin;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.client.WorldlineCensusRenderer;

/** Selects a nested 0/1/4/16 subset at the exact Aero queue boundary. */
@Mixin(WorldlineCensusRenderer.class)
public abstract class WorldlineDensityRendererMixin {
  @Unique private boolean worldline$announced;
  @Redirect(method = "render",
      at = @At(value = "INVOKE",
          target =
              "Laero/modellib/Aero_BECellRenderer;queueAtRest(Laero/modellib/model/Aero_MeshModel;Ljava/lang/String;Lnet/minecraft/block/entity/BlockEntity;DDDFFLaero/modellib/render/Aero_RenderOptions;)V"))
  private void
  worldline$select(Aero_MeshModel model, String texture, BlockEntity raw, double x, double y,
      double z, float rotation, float brightness, Aero_RenderOptions options) {
    int level = Integer.getInteger("worldline.density.level", -1),
        root = Integer.getInteger("worldline.census.nonce", 0);
    if (!(level == 0 || level == 1 || level == 4 || level == 16)
        || !(raw instanceof WorldlineCensusBlockEntity be))
      throw new IllegalStateException("invalid M75 density boundary");
    int index = be.nonce() - root * 100 - 1;
    if (index < 0 || index > 15)
      throw new IllegalStateException("invalid M75 identity");
    if (!worldline$announced) {
      worldline$announced = true;
      System.out.println("[WorldlineDensity] level=" + level + " root=" + root);
    }
    if (index < level)
      Aero_BECellRenderer.queueAtRest(model, texture, raw, x, y, z, rotation, brightness, options);
  }
}
