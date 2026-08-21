package worldline.smoke.lavadownwardflowb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Contrasts open, blocked, and floor-removed vertical lava columns. */
public final class LavaDownwardFlowSmoke {
    private static final long SEED = 51420240820L;
    private static final int TICKS = 240;
    private LavaDownwardFlowSmoke() { }
    public static void main(String[] arguments) { execute().emitTo(System.out); }

    private static CanonicalTrace execute() {
        CanonicalTrace trace = new CanonicalTrace(SEED);
        run(trace, "open", LavaMemoryChunkLoader.OPEN);
        run(trace, "blocked", LavaMemoryChunkLoader.BLOCKED);
        run(trace, "shaft", LavaMemoryChunkLoader.SHAFT);
        return trace;
    }

    private static void run(CanonicalTrace trace, String label, int fixture) {
        LavaWorldBackend backend = new LavaWorldBackend(SEED, fixture);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
            backend.snapshot(trace, label + "-seed");
            for (int tick = 1; tick <= TICKS; tick++) {
                runtime.tick();
                backend.snapshot(trace, label + "-tick" + tick);
            }
            backend.assertOutcome();
        } finally { runtime.close(); }
    }
}
