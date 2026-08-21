package worldline.smoke.pigwanderb173;

import net.minecraft.src.EntityPig;
import net.minecraft.src.World;

/** Exposes only deterministic seeding of the pig's inherited behavior RNG. */
final class SeededPig extends EntityPig {
    SeededPig(World world) { super(world); }

    void seedBehavior(long seed) { rand.setSeed(seed); }
}
