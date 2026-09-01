package worldline.smoke.b173sourcefluiddynamics;

import worldline.b173server.B173FluidDynamicsScenarioFactory;
import worldline.b173server.B173ServerFluidDynamicsTestRuntimeProvider;
import worldline.testkit.FluidDynamicsFamilyCycle;

/** Qualifies source water and lava placement, gated propagation, and persistence. */
public final class B173SourceFluidDynamicsSmoke {
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";

    private B173SourceFluidDynamicsSmoke() { }

    public static void main(String[] arguments) throws Exception {
        FluidDynamicsFamilyCycle.run(arguments, "source-fluid-dynamics",
                B173FluidDynamicsScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerFluidDynamicsTestRuntimeProvider(),
                B173FluidDynamicsScenarioFactory.rows());
    }
}
