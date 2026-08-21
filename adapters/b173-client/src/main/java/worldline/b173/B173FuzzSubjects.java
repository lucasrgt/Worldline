package worldline.b173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import worldline.api.WorldSource;
import worldline.fuzz.FuzzSubject;
import worldline.fuzz.FuzzSubjectProvider;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.minimization.ScenarioStep;
import worldline.mods.LoadedMod;
import worldline.mods.ModLoader;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateTrace;

/** Binds mod JARs and the vanilla runtime to differential fuzzing subjects. */
public final class B173FuzzSubjects implements FuzzSubjectProvider {
    private static final String RUNTIME = "b1.7.3", API = "1";

    @Override
    public List<FuzzSubject> subjects(List<Path> jars) {
        if (jars == null) throw new NullPointerException("jars");
        if (jars.size() > 2) throw new IllegalArgumentException("at most two mod subjects");
        List<FuzzSubject> result = new ArrayList<>();
        if (jars.isEmpty()) {
            result.add(vanilla());
            return result;
        }
        if (jars.size() == 1) result.add(vanilla());
        for (Path jar : jars) result.add(mod(jar));
        return result;
    }

    private static FuzzSubject vanilla() {
        return new FuzzSubject() {
            @Override public String label() { return "vanilla"; }
            @Override public CanonicalStateDocument run(Scenario scenario, long seed) {
                return new B173ScenarioRunner().run(scenario, seed);
            }
        };
    }

    private static FuzzSubject mod(Path jar) {
        worldline.mods.ModArtifact artifact;
        try { artifact = ModLoader.inspect(jar, RUNTIME, API); }
        catch (java.io.IOException failure) {
            throw new IllegalStateException("unreadable mod subject", failure);
        }
        require(artifact.compatible(), "incompatible fuzz subject: " + artifact.compatibility());
        final String label = "mod:" + artifact.descriptor().id() + "@"
                + artifact.descriptor().version();
        return new FuzzSubject() {
            @Override public String label() { return label; }
            @Override public CanonicalStateDocument run(Scenario scenario, long seed) {
                List<ScenarioStep> steps = ScenarioDsl.parseAll(scenario);
                try (LoadedMod<B173Mod> loaded = ModLoader.load(jar, RUNTIME, API, B173Mod.class)) {
                    B173Runtime runtime = B173Runtimes.create(seed);
                    runtime.bootHeadless();
                    try {
                        runtime.loadWorld(WorldSource.at(Paths.get("memory", "fuzz-run")));
                        runtime.installMod(loaded.instance());
                        CanonicalStateTrace trace = B173ScenarioOps.trace(runtime, seed);
                        for (ScenarioStep step : steps) B173ScenarioOps.apply(step, runtime, trace);
                        return CanonicalStateDocument.parse(trace.value());
                    } finally { runtime.close(); }
                } catch (java.io.IOException | ReflectiveOperationException failure) {
                    throw new IllegalStateException("fuzz subject execution failed", failure);
                }
            }
        };
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
