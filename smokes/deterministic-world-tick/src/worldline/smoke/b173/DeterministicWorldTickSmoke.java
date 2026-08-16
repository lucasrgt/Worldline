package worldline.smoke.b173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Executes one canonical scenario against vanilla b1.7.3 server classes. */
public final class DeterministicWorldTickSmoke {
    private static final long SEED = 17320110707L;

    private DeterministicWorldTickSmoke() {}

    public static void main(String[] arguments) {
        executeScenario().emitTo(System.out);
    }

    private static CanonicalTrace executeScenario() {
        VanillaWorldBackend backend = new VanillaWorldBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
            CanonicalTrace trace = new CanonicalTrace(SEED);
            backend.snapshot(trace, "initial");
            backend.placeSand();
            backend.snapshot(trace, "placed");
            for (int tick = 1; tick <= 8; tick++) {
                runtime.tick();
                backend.snapshot(trace, "tick" + tick);
            }
            backend.assertFinalState();
            return trace;
        } finally {
            runtime.close();
        }
    }

}
