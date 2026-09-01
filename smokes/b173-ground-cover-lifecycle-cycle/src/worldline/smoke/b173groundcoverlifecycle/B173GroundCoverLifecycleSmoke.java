package worldline.smoke.b173groundcoverlifecycle;

import worldline.b173server.B173GroundCoverLifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;

/** Tall-grass, dead-bush, and age-zero wheat over the public lifecycle provider. */
public final class B173GroundCoverLifecycleSmoke {
    private B173GroundCoverLifecycleSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "ground-cover", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(),
                B173GroundCoverLifecycleScenarioFactory.rows());
    }
}
