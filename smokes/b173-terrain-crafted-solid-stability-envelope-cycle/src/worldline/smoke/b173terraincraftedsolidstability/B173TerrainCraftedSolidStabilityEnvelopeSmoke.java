package worldline.smoke.b173terraincraftedsolidstability;

import worldline.b173server.B173TerrainCraftedSolidStabilityScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockStabilityFamilyCycle;

/** Qualifies one bounded terrain-crafted solid tick and direct-neighbor stability family. */
public final class B173TerrainCraftedSolidStabilityEnvelopeSmoke {
    private B173TerrainCraftedSolidStabilityEnvelopeSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockStabilityFamilyCycle.run(arguments, "terrain-crafted-solid-stability-envelope",
                B173TerrainCraftedSolidStabilityScenarioFactory.SEED,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(),
                B173TerrainCraftedSolidStabilityScenarioFactory.rows());
    }
}
