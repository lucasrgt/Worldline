package worldline.smoke.b173furnacestatedomain;

import java.util.Collections;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.b173server.B173StateDomainScenarioFactory;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Complete furnace facing domain through the public official-server TestKit provider. */
public final class B173FurnaceStateDomainSmoke {
    private B173FurnaceStateDomainSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(arguments, "furnace", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerStateDomainTestRuntimeProvider(),
                Collections.singletonList(B173StateDomainScenarioFactory.furnaceFacing()));
    }
}
