package worldline.b173;

import java.nio.file.Paths;
import java.util.List;
import worldline.api.WorldSource;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.minimization.ScenarioRunner;
import worldline.minimization.ScenarioStep;
import worldline.minimization.ScenarioTimeTravel;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateTrace;

/** Executes public-grammar scenarios and deterministic time-travel prefixes. */
public final class B173ScenarioRunner implements ScenarioRunner, ScenarioTimeTravel {
    @Override
    public CanonicalStateDocument run(Scenario scenario, long seed) {
        return prefix(scenario, seed, scenario.size());
    }

    @Override
    public CanonicalStateDocument prefix(Scenario scenario, long seed, int steps) {
        if (steps < 0 || steps > scenario.size()) throw new IllegalArgumentException("invalid prefix");
        List<ScenarioStep> parsed = ScenarioDsl.parseAll(scenario);
        B173Runtime runtime = B173Runtimes.create(seed);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "scenario-run")));
            CanonicalStateTrace trace = B173ScenarioOps.trace(runtime, seed);
            for (int index = 0; index < steps; index++) {
                B173ScenarioOps.apply(parsed.get(index), runtime, trace);
            }
            return CanonicalStateDocument.parse(trace.value());
        } finally { runtime.close(); }
    }
}
