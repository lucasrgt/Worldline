package worldline.smoke.redstoneore;
import net.minecraft.src.*;
/** Exposes the otherwise wall-clock-seeded random-tick cursor for differential fixtures. */
final class DeterministicWorld extends World {
  DeterministicWorld(ISaveHandler save, String name, long seed) {
    super(save, name, seed, null);
  }
  void freezeRandom(long seed) {
    distHashCounter = (int) seed;
    rand.setSeed(seed);
  }
}
