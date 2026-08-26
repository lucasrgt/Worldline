package worldline.smoke.skybrightnesscycleb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Drives the mapped clear-sky brightness cycle through the controlled runtime. */
public final class SkyBrightnessCycleSmoke {
    private static final long SEED = 65420260825L;

    private SkyBrightnessCycleSmoke() { }

    public static void main(String[] arguments) {
        SkyBrightnessCycleBackend backend = new SkyBrightnessCycleBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        CanonicalTrace trace = new CanonicalTrace(SEED);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "sky-brightness-cycle")));
            runtime.tick();
            backend.record(trace);
            trace.emitTo(System.out);
        } finally {
            runtime.close();
        }
    }
}
