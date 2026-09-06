package worldline.m794;

import aero.modellib.test.AnimatedMegaModelBlockEntity;
import aero.modellib.test.AnimatedMegaModelBlockEntityRenderer;
import aero.modellib.test.ProductionModels;
import net.minecraft.block.entity.BlockEntity;

/** Direct consumer dispatch avoids allocating a cancellable callback per model. */
public final class ProductionModelRenderer extends AnimatedMegaModelBlockEntityRenderer {
  @Override
  public void render(BlockEntity block, double x, double y, double z, float delta) {
    if (ProductionState.acquiring())
      ProductionModels.render((AnimatedMegaModelBlockEntity) block, x, y, z, delta);
    else
      super.render(block, x, y, z, delta);
  }
}
