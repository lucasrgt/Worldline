package worldline.smoke.b173staticcollision;

import worldline.b173server.B173CollisionScenarioFactory;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.testkit.BlockCollisionFamilyCycle;

/** Static collision envelopes through the official-server TestKit provider. */
public final class B173StaticCollisionEnvelopeSmoke {
    private B173StaticCollisionEnvelopeSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockCollisionFamilyCycle.run(arguments, "static-envelope", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerCollisionTestRuntimeProvider(),
                B173CollisionScenarioFactory.staticEnvelopeFamily());
    }
}
