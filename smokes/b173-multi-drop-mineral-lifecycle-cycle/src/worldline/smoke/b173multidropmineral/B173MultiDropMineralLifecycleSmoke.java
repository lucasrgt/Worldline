package worldline.smoke.b173multidropmineral;

import java.util.Arrays;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Three block-to-multiple-item mineral harvests over the public lifecycle provider. */
public final class B173MultiDropMineralLifecycleSmoke {
    private B173MultiDropMineralLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "multi-drop-minerals",
                17_320_110_707L, "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                mineral("lapis-ore", "021", 21, drops(351, 4, 8)),
                mineral("redstone-ore", "073", 73, drops(331, 0, 4)),
                mineral("glowstone", "089", 89, drops(348, 0, 2))));
    }

    private static BlockLifecycleScenario mineral(String id, String legacy,
            int block, RemoteItemStack[] drops) {
        return B173LifecycleScenarioFactory.harvestOnSupport(id,
                "b1.7.3:block/" + legacy, "multi-drop-mineral", false,
                block, 0, 0, new BlockState(1, 0), 278, 1, 20, drops);
    }

    private static RemoteItemStack[] drops(int item, int damage, int count) {
        RemoteItemStack[] result = new RemoteItemStack[count];
        Arrays.fill(result, new RemoteItemStack(item, 1, damage));
        return result;
    }
}
