package worldline.smoke.b173supportfaceattachments;

import java.util.Arrays;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Four side-attached components over the public lifecycle provider. */
public final class B173SupportFaceAttachmentLifecycleSmoke {
    private B173SupportFaceAttachmentLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "support-face-attachments",
                17_320_110_707L, "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                row("ladder", "065", 65, 65, 5, 65),
                row("wall-sign", "068", 68, 323, 5, 323),
                row("lever", "069", 69, 69, 1, 69),
                row("stone-button", "077", 77, 77, 1, 77)));
    }

    private static BlockLifecycleScenario row(String id, String legacy, int block,
            int placementItem, int metadata, int drop) {
        return B173LifecycleScenarioFactory.harvestOnFace(id,
                "b1.7.3:block/" + legacy, "support-face-attachment", false,
                block, placementItem, 0, metadata, new BlockState(1, 0),
                BlockFace.EAST, 280, 0, 1, new RemoteItemStack(drop, 1, 0));
    }
}
