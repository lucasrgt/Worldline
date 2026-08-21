package worldline.b173;

import java.nio.file.Paths;
import java.util.List;
import worldline.api.WorldSource;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.minimization.ScenarioRunner;
import worldline.minimization.ScenarioStep;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateTrace;

/** Executes a public-grammar scenario against the controlled b1.7.3 runtime. */
public final class B173ScenarioRunner implements ScenarioRunner {
    @Override
    public CanonicalStateDocument run(Scenario scenario, long seed) {
        List<ScenarioStep> steps = ScenarioDsl.parseAll(scenario);
        B173Runtime runtime = B173Runtimes.create(seed);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "scenario-run")));
            CanonicalStateTrace trace = B173ScenarioOps.trace(runtime, seed);
            for (ScenarioStep step : steps) B173ScenarioOps.apply(step, runtime, trace);
            return CanonicalStateDocument.parse(trace.value());
        } finally { runtime.close(); }
    }
}
