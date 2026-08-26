package worldline.smoke.poweredcreeperb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.PoweredCreeperEvidence;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Drives one native lightning transformation through the public action boundary. */
public final class PoweredCreeperSmoke {
    private static final long SEED = 65920260826L;

    private PoweredCreeperSmoke() { }

    public static void main(String[] arguments) {
        PoweredCreeperBackend backend = new PoweredCreeperBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        CanonicalTrace trace = new CanonicalTrace(SEED);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "powered-creeper")));
            backend.recordInitial(trace);
            PoweredCreeperEvidence.Trial trial = backend.strike();
            runtime.tick();
            PoweredCreeperEvidence evidence =
                    PoweredCreeperEvidence.capture(trial, backend.current());
            backend.recordOutcome(trace, evidence);
            trace.emitTo(System.out);
        } finally {
            runtime.close();
        }
    }
}
