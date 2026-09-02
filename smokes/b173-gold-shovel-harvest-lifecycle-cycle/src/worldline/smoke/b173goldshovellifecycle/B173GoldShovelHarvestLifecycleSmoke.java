package worldline.smoke.b173goldshovellifecycle;

import java.util.Arrays;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testapi.BlockLifecycleScenario;

/** Six independently signed gold-shovel harvest rows over the public lifecycle provider. */
public final class B173GoldShovelHarvestLifecycleSmoke {
    private static final int GOLD_SHOVEL = 284;
    private B173GoldShovelHarvestLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "gold-shovel-harvest", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                selfDrop("sand", "012", "gravity-block", 12),
                selfDrop("gravel", "013", "gravity-block", 13),
                harvest("snow-layer", "078", "snow", 78, item(332)),
                harvest("snow-block", "080", "snow", 80,
                        item(332), item(332), item(332), item(332)),
                harvest("clay", "082", "clay", 82,
                        item(337), item(337), item(337), item(337)),
                selfDrop("soul-sand", "088", "soul-sand", 88)));
    }

    private static BlockLifecycleScenario selfDrop(String id, String legacy,
            String archetype, int block) {
        return harvest(id, legacy, archetype, block, item(block));
    }

    private static BlockLifecycleScenario harvest(String id, String legacy,
            String archetype, int block, RemoteItemStack... drops) {
        return B173LifecycleScenarioFactory.harvest(id, "b1.7.3:block/" + legacy,
                archetype, false, block, 0, GOLD_SHOVEL, 1, 5, drops);
    }

    private static RemoteItemStack item(int id) {
        return new RemoteItemStack(id, 1, 0);
    }
}
