package worldline.smoke.flowingwaterfreezeb173;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/** In-memory player for the mapped freeze fixture. */
final class FlowingWaterPlayer extends EntityPlayer {
  FlowingWaterPlayer(World world, int x, int z) {
    super(world);
    setLocationAndAngles(x + 0.5D, 66D, z + 0.5D, 0F, 0F);
  }

  public void func_6420_o() {
  }
}
