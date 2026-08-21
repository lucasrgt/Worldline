package worldline.smoke.piston;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Powers one piston and ticks until the head is placed. */
public final class PistonExtendSmoke {
    private static final long SEED = 17320110707L;

    private PistonExtendSmoke() {}

    public static void main(String[] arguments) {
        PistonExtendBackend backend = new PistonExtendBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
            CanonicalTrace trace = new CanonicalTrace(SEED);
            backend.snapshot(trace, "initial");
            backend.placeCircuit();
            backend.snapshot(trace, "placed");
            for (int tick = 1; tick <= 8; tick++) {
                runtime.tick();
                backend.snapshot(trace, "tick" + tick);
            }
            backend.assertFinalState();
            trace.emitTo(System.out);
        } finally {
            runtime.close();
        }
    }
}
