package worldline.smoke.b173redstonedevicelifecycle;

import java.util.Arrays;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Three craftable redstone devices over the public lifecycle provider. */
public final class B173RedstoneDeviceLifecycleSmoke {
    private static final BlockState STONE = new BlockState(1, 0);

    private B173RedstoneDeviceLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "redstone-devices",
                17_320_110_707L, "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                redstoneWire(), repeaterOff(), trapdoor()));
    }

    private static BlockLifecycleScenario redstoneWire() {
        return B173LifecycleScenarioFactory.harvestFromItem("redstone-wire",
                "b1.7.3:block/055", "redstone-device", false,
                55, 331, 0, 0, STONE, 280, 0, 1,
                new RemoteItemStack(331, 1, 0));
    }

    private static BlockLifecycleScenario repeaterOff() {
        return B173LifecycleScenarioFactory.harvestFromItem("repeater-off",
                "b1.7.3:block/093", "redstone-device", false,
                93, 356, 0, 2, STONE, 280, 0, 1,
                new RemoteItemStack(356, 1, 0));
    }

    private static BlockLifecycleScenario trapdoor() {
        return B173LifecycleScenarioFactory.harvestOnFace("trapdoor",
                "b1.7.3:block/096", "redstone-device", false,
                96, 96, 0, 3, STONE, BlockFace.EAST,
                280, 0, 1, new RemoteItemStack(96, 1, 0));
    }
}
