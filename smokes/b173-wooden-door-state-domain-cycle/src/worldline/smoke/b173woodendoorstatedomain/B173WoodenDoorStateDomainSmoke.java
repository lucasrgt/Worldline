package worldline.smoke.b173woodendoorstatedomain;

import java.util.Collections;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.b173server.B173StateDomainScenarioFactory;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Full wooden-door metadata domain through the public official-server TestKit provider. */
public final class B173WoodenDoorStateDomainSmoke {
    private B173WoodenDoorStateDomainSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(arguments, "wooden-door", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerStateDomainTestRuntimeProvider(),
                Collections.singletonList(B173StateDomainScenarioFactory.woodenDoor()));
    }
}
