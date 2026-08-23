package worldline.m74.mixin;

import aero.modellib.*;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.*;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import worldline.m74.*;
import worldline.m74.client.*;

/** Routes the exact fixture through Aero's managed pre-dispatch queue. */
@Mixin(WorldlineCensusBlockEntity.class)
public abstract class WorldlinePagedBlockEntityMixin implements Aero_CellPageRenderableBE {
  @Override
  public Aero_MeshModel aeroCellModel() {
    return WorldlineManagedModel.model();
  }
  @Override
  public String aeroCellTexturePath() {
    return "/terrain.png";
  }
  @Override
  public float aeroCellBrightness() {
    BlockEntity be = (BlockEntity) (Object) this;
    return be.world.method_1782(be.x, be.y + 1, be.z);
  }
  @Override
  public double aeroCellVisualRadius() {
    return 64D;
  }
  @Override
  public double aeroCellAnimatedDistance() {
    return 0D;
  }
  @Override
  public int aeroRenderStateHash() {
    return 0;
  }
  @Override
  public int aeroOrientationHash() {
    return 0;
  }
  @Override
  public boolean aeroCanCellPage() {
    return true;
  }
  @Override
  public boolean aeroWantsAnimation() {
    return false;
  }
  public double distanceFrom(double x, double y, double z) {
    BlockEntity be = (BlockEntity) (Object) this;
    if (Aero_BECellRenderer.SKIP_INDIVIDUAL_RENDERERS) {
      WorldlinePagedTimer.queueBegin();
      boolean queued = Aero_BECellRenderer.tryQueueManagedAtRest(be, this);
      WorldlinePagedTimer.queueEnd();
      if (queued) {
        int nonce = ((WorldlineCensusBlockEntity) be).nonce();
        if (WorldlineCensusSync.index(be.x, be.y, be.z, nonce) >= 0)
          WorldlineCensusProbe.rendered(be.x, be.y, be.z, nonce);
        return Double.POSITIVE_INFINITY;
      }
    }
    double dx = be.x + .5D - x, dy = be.y + .5D - y, dz = be.z + .5D - z;
    return dx * dx + dy * dy + dz * dz;
  }
}
