package worldline.benchmark;

import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Independently packaged benchmark mod that places one glass block. */
public final class ProbeMod implements B173Mod {
    @Override
    public void onTick(B173ModContext context) {
        if (context.blockAt(8, 65, 8) == 0) context.setBlock(8, 65, 8, 20);
    }
}
