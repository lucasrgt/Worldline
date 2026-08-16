package worldline.benchmark;

import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Version 1 changes block 65 to glass on its first controlled tick. */
public final class VersionOneMod implements B173Mod {
    @Override public void onTick(B173ModContext context) {
        if (context.clientTick() == 1) context.setBlock(8, 65, 8, 20);
    }
}
