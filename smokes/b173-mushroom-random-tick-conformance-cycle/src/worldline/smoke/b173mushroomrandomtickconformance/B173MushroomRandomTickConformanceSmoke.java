package worldline.smoke.b173mushroomrandomtickconformance;

import worldline.b173server.B173MushroomRandomTickScenarioFactory;
import worldline.b173server.B173ServerMushroomRandomTickTestRuntimeProvider;
import worldline.testkit.BlockRandomTickSpreadFamilyCycle;

/** Brown and red mushroom physical, random-tick, and support conformance. */
public final class B173MushroomRandomTickConformanceSmoke {
    private B173MushroomRandomTickConformanceSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockRandomTickSpreadFamilyCycle.run(arguments, "mushroom-random-tick-conformance",
                B173MushroomRandomTickScenarioFactory.SEED,
                B173ServerMushroomRandomTickTestRuntimeProvider.SERVER_PROPERTY,
                new B173ServerMushroomRandomTickTestRuntimeProvider(),
                B173MushroomRandomTickScenarioFactory.rows());
    }
}
