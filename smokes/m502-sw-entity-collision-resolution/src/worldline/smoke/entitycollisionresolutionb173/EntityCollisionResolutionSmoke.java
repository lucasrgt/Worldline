package worldline.smoke.entitycollisionresolutionb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Seeds two living entities in overlap and separated and observes bounded horizontal resolution. */
public final class EntityCollisionResolutionSmoke {
    private static final long SEED = 50220240820L;
    private static final int TICKS = 10;

    private EntityCollisionResolutionSmoke() {}

    public static void main(String[] arguments) {
        executeScenario().emitTo(System.out);
    }

    private static CanonicalTrace executeScenario() {
        CanonicalTrace trace = new CanonicalTrace(SEED);
        run(trace, "overlap", true);
        run(trace, "separated", false);
        return trace;
    }

    private static void run(CanonicalTrace trace, String label, boolean overlap) {
        CollisionWorldBackend backend = new CollisionWorldBackend(SEED, overlap);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
            backend.seed();
            backend.snapshot(trace, label + "-seed");
            for (int tick = 1; tick <= TICKS; tick++) {
                runtime.tick();
                backend.snapshot(trace, label + "-tick" + tick);
            }
            backend.assertOutcome();
        } finally {
            runtime.close();
        }
    }
}
