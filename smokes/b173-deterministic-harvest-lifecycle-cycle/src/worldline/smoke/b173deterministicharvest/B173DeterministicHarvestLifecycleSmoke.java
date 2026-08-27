package worldline.smoke.b173deterministicharvest;

import java.util.Arrays;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Deterministic legacy harvests whose drops differ from their placed block. */
public final class B173DeterministicHarvestLifecycleSmoke {
    private static final BlockState STONE = new BlockState(1, 0);

    private B173DeterministicHarvestLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "deterministic-harvest", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                        grass(), cobweb(), bookshelf(), cake()));
    }

    private static BlockLifecycleScenario grass() {
        return row("grass", "002", "simple-solid", false, 2, 2, 256, 1, 10,
                new RemoteItemStack(3, 1, 0));
    }

    private static BlockLifecycleScenario cobweb() {
        return row("cobweb", "030", "transparent-solid", false, 30, 30, 276, 2, 20,
                new RemoteItemStack(287, 1, 0));
    }

    private static BlockLifecycleScenario bookshelf() {
        return row("bookshelf", "047", "simple-solid", false, 47, 47, 258, 1, 40);
    }

    private static BlockLifecycleScenario cake() {
        return row("cake", "092", "cake", true, 92, 354, 280, 0, 1);
    }

    private static BlockLifecycleScenario row(String id, String legacy, String archetype,
            boolean singular, int block, int placementItem, int tool, int toolDamage,
            int breakTicks, RemoteItemStack... drops) {
        return B173LifecycleScenarioFactory.harvestFromItem(id, "b1.7.3:block/" + legacy,
                archetype, singular, block, placementItem, 0, 0, STONE,
                tool, toolDamage, breakTicks, drops);
    }
}
