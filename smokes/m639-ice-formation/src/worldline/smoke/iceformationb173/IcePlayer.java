package worldline.smoke.iceformationb173;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/** Minimal observer whose position activates the native 19-by-19 chunk scheduler. */
final class IcePlayer extends EntityPlayer {
  IcePlayer(World world, int x, int z) {
    super(world);
    setLocationAndAngles(x + 0.5D, 66D, z + 0.5D, 0F, 0F);
  }
  public void func_6420_o() {}
}
