package worldline.benchmark;

import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Version 2 delays its change until tick two and places gold. */
public final class VersionTwoMod implements B173Mod {
    @Override public void onTick(B173ModContext context) {
        if (context.clientTick() == 2) context.setBlock(8, 65, 8, 41);
    }
}
