package worldline.smoke.b173wallattachmentstatedomain;

import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.b173server.B173AttachmentStateDomainScenarioFactory;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Complete wall-attachment metadata domains through the official-server TestKit provider. */
public final class B173WallAttachmentStateDomainSmoke {
    private B173WallAttachmentStateDomainSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(arguments, "wall-attachment", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerStateDomainTestRuntimeProvider(),
                B173AttachmentStateDomainScenarioFactory.wallAttachmentFamily());
    }
}
