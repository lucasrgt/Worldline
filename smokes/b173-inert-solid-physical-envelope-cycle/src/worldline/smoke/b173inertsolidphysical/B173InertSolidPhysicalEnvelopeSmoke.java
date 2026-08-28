package worldline.smoke.b173inertsolidphysical;

import java.nio.file.Paths;
import worldline.b173server.B173InertSolidPhysicalScenarioFactory;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Qualifies one inert-solid physical envelope across three public TestKit dimensions. */
public final class B173InertSolidPhysicalEnvelopeSmoke {
    private static final String FAMILY = "inert-solid-envelope";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";

    private B173InertSolidPhysicalEnvelopeSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), FAMILY,
                B173InertSolidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173InertSolidPhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), FAMILY,
                B173InertSolidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173InertSolidPhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), FAMILY,
                B173InertSolidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173InertSolidPhysicalScenarioFactory.lights());
    }

    private static String[] scoped(String[] arguments, String layer) {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: inert solid physical family server.jar workspace port seed");
        String[] result = arguments.clone();
        result[1] = Paths.get(arguments[1]).resolve(layer).toString();
        return result;
    }
}
