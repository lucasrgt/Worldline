package worldline.benchmark;

import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Makes premature initialization of an incompatible entrypoint observable. */
public final class InitializationTrapMod implements B173Mod {
    private static final boolean INITIALIZED = fail();

    @Override public void onTick(B173ModContext context) { require(INITIALIZED); }

    private static boolean fail() { throw new AssertionError("incompatible mod was initialized"); }
    private static void require(boolean value) { if (!value) throw new AssertionError("unreachable"); }
}
