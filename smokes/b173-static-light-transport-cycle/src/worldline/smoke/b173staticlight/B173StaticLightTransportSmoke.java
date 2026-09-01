package worldline.smoke.b173staticlight;

import worldline.b173server.B173LightScenarioFactory;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.testkit.BlockLightFamilyCycle;

/** Static skylight attenuation and block-light propagation through the public TestKit. */
public final class B173StaticLightTransportSmoke {
    private B173StaticLightTransportSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLightFamilyCycle.run(arguments, "static-transport", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLightTestRuntimeProvider(),
                B173LightScenarioFactory.staticTransportFamily());
    }
}
