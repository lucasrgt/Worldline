package worldline.b173;

import java.nio.file.Path;
import java.nio.file.Paths;
import worldline.api.WorldSource;
import worldline.modtest.ModTestResult;
import worldline.modtest.ModTestRunner;
import worldline.mods.LoadedMod;
import worldline.mods.ModArtifact;
import worldline.mods.ModLoader;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateTrace;

/** Executes one mod JAR in the controlled b1.7.3 runtime and attests the trace. */
public final class B173ModTestRunner implements ModTestRunner {
    private static final String RUNTIME = "b1.7.3", API = "1";

    @Override
    public ModTestResult run(Path modJar, long seed, int ticks) {
        ModArtifact artifact;
        try (LoadedMod<B173Mod> loaded = ModLoader.load(modJar, RUNTIME, API, B173Mod.class)) {
            artifact = loaded.artifact();
            B173Runtime runtime = B173Runtimes.create(seed);
            runtime.bootHeadless();
            try {
                runtime.loadWorld(WorldSource.at(Paths.get("memory", "mod-test-run")));
                runtime.installMod(loaded.instance());
                CanonicalStateTrace trace = new CanonicalStateTrace(seed,
                        "tick", "block65", "entityCount");
                for (int index = 1; index <= ticks; index++) {
                    runtime.tick();
                    B173Observation state = runtime.observe();
                    trace.record("t" + index, state.clientTick(),
                            state.blockColumn()[1], runtime.world().entities().size());
                }
                return ModTestResult.createExecuted(artifact,
                        CanonicalStateDocument.parse(trace.value()), seed, ticks);
            } finally { runtime.close(); }
        } catch (java.io.IOException | ReflectiveOperationException failure) {
            throw new IllegalStateException("attested mod run failed", failure);
        }
    }
}
