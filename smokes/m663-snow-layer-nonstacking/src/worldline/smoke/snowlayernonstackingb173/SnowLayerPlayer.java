package worldline.smoke.snowlayernonstackingb173;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/** Minimal observer that activates the native ambient scheduler. */
final class SnowLayerPlayer extends EntityPlayer {
    SnowLayerPlayer(World world, int x, int z) {
        super(world);
        setLocationAndAngles(x + 0.5D, 66D, z + 0.5D, 0F, 0F);
    }

    public void func_6420_o() {
    }
}
