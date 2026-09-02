package worldline.smoke.spiderdaylightb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.scenario.SpiderDaylightAggressionActions;
import worldline.api.scenario.SpiderDaylightAggressionEvidence;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Drives one same-spider daylight-to-night target-selection differential. */
public final class SpiderDaylightAggressionSmoke {
    private static final long SEED = 66120260826L;

    private SpiderDaylightAggressionSmoke() {
    }

    public static void main(String[] arguments) {
        SpiderDaylightAggressionBackend backend =
                new SpiderDaylightAggressionBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        CanonicalTrace trace = new CanonicalTrace(SEED);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "spider-daylight")));
            runtime.tick();
            SpiderDaylightAggressionEvidence evidence =
                    SpiderDaylightAggressionEvidence.capture(
                            backend.trial(
                                    SpiderDaylightAggressionActions.MAXIMUM_TARGET_ATTEMPTS),
                            SpiderDaylightAggressionActions.MAXIMUM_TARGET_ATTEMPTS);
            backend.record(trace, evidence);
            trace.emitTo(System.out);
        } finally {
            runtime.close();
        }
    }
}
