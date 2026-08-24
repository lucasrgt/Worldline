package worldline.smoke.lightningfireb173;

import net.minecraft.src.EntityLightningBolt;
import net.minecraft.src.World;

/** Seeds inherited lightning RNG after constructor ignition for stable ticks. */
final class SeededLightning extends EntityLightningBolt {
  SeededLightning(World world, double x, double y, double z, long seed) {
    super(world, x, y, z);
    rand.setSeed(seed);
  }
}
