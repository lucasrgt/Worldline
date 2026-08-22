package worldline.smoke.repeater;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Places one delay-1 repeater fixture and ticks the public runtime. */
public final class RepeaterDelaySmoke {
    private static final long SEED = 17320110707L;

    private RepeaterDelaySmoke() {}

    public static void main(String[] arguments) {
        executeScenario().emitTo(System.out);
    }

    private static CanonicalTrace executeScenario() {
        RepeaterWorldBackend backend = new RepeaterWorldBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
            CanonicalTrace trace = new CanonicalTrace(SEED);
            backend.snapshot(trace, "initial");
            backend.placeCircuit();
            backend.snapshot(trace, "placed");
            backend.assertPlacedState();
            for (int tick = 1; tick <= 6; tick++) {
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
