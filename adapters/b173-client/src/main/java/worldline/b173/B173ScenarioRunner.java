package worldline.b173;

import java.nio.file.Paths;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.WorldSource;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.minimization.ScenarioRunner;
import worldline.minimization.ScenarioStep;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateTrace;

/** Executes a public-grammar scenario against the controlled b1.7.3 runtime. */
public final class B173ScenarioRunner implements ScenarioRunner {
    private static final String[] SCHEMA = {"tick", "block65"};

    @Override
    public CanonicalStateDocument run(Scenario scenario, long seed) {
        java.util.List<ScenarioStep> steps = ScenarioDsl.parseAll(scenario);
        B173Runtime runtime = B173Runtimes.create(seed);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "scenario-run")));
            CanonicalStateTrace trace = new CanonicalStateTrace(seed, SCHEMA);
            for (ScenarioStep step : steps) apply(step, runtime, trace);
            return CanonicalStateDocument.parse(trace.value());
        } finally { runtime.close(); }
    }

    private static void apply(ScenarioStep step, B173Runtime runtime,
            CanonicalStateTrace trace) {
        switch (step.kind()) {
            case TICK:
                for (int index = 0; index < step.count(); index++) runtime.tick();
                return;
            case RESEED:
                runtime.reseed(step.seed()); return;
            case TAP:
                runtime.tap(step.key()); return;
            case OBSERVE:
                B173Observation state = runtime.observe();
                trace.record(step.label(), state.clientTick(), state.blockColumn()[1]);
                return;
            case BLOCK:
                runtime.world().setBlock(new BlockPosition(step.x(), step.y(), step.z()),
                        new BlockState(step.blockId(), step.metadata()));
                return;
            default:
                throw new IllegalStateException("unexecutable scenario step");
        }
    }
}
