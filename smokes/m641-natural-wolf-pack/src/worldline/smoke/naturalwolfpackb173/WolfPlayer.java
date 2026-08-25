package worldline.smoke.naturalwolfpackb173;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/** Minimal observer for the native peaceful-spawn radius. */
final class WolfPlayer extends EntityPlayer {
  WolfPlayer(World world, int x, int z) {
    super(world);
    setLocationAndAngles(x + 0.5D, 66D, z + 0.5D, 0F, 0F);
  }
  public void func_6420_o() {}
}
