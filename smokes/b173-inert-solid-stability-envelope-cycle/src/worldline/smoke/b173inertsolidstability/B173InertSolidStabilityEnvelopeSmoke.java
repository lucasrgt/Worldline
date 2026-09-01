package worldline.smoke.b173inertsolidstability;

import worldline.b173server.B173InertSolidStabilityScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockStabilityFamilyCycle;

/** Qualifies one bounded inert-solid tick and direct-neighbor stability subsystem. */
public final class B173InertSolidStabilityEnvelopeSmoke {
    private B173InertSolidStabilityEnvelopeSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockStabilityFamilyCycle.run(arguments, "inert-solid-stability-envelope",
                B173InertSolidStabilityScenarioFactory.SEED,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(),
                B173InertSolidStabilityScenarioFactory.rows());
    }
}
