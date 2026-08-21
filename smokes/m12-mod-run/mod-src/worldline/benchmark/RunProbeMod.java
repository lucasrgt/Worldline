package worldline.benchmark;

import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Places one glass block on the first controlled tick of a run. */
public final class RunProbeMod implements B173Mod {
    @Override public void onTick(B173ModContext context) {
        if (context.clientTick() == 1) context.setBlock(8, 65, 8, 20);
    }
}
