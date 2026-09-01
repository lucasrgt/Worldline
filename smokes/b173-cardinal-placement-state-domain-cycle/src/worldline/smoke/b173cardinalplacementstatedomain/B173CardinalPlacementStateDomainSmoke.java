package worldline.smoke.b173cardinalplacementstatedomain;

import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.b173server.B173StateDomainScenarioFactory;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Complete cardinal placement metadata domains through the public official-server TestKit. */
public final class B173CardinalPlacementStateDomainSmoke {
    private B173CardinalPlacementStateDomainSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(arguments, "cardinal-placement", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerStateDomainTestRuntimeProvider(),
                B173StateDomainScenarioFactory.cardinalPlacementFamily());
    }
}
