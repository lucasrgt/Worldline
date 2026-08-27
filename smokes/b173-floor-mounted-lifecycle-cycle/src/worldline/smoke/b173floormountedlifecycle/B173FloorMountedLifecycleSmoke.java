package worldline.smoke.b173floormountedlifecycle;

import java.util.Arrays;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Six floor-mounted directional and attachment rows over the public lifecycle provider. */
public final class B173FloorMountedLifecycleSmoke {
    private B173FloorMountedLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "floor-mounted", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                row("wood-stairs", "053", "stairs", 53, 2, 279, 1, 40),
                row("cobblestone-stairs", "067", "stairs", 67, 2, 278, 1, 40),
                row("pumpkin", "086", "pumpkin", 86, 2, 279, 1, 40),
                row("jack-o-lantern", "091", "pumpkin", 91, 2, 279, 1, 40),
                row("torch", "050", "torch", 50, 5, 280, 0, 1),
                row("redstone-torch", "076", "redstone-torch", 76, 5, 280, 0, 1)));
    }

    private static BlockLifecycleScenario row(String id, String legacy, String archetype,
            int block, int metadata, int tool, int toolAfterDamage, int ticks) {
        return B173LifecycleScenarioFactory.selfDrop(id, "b1.7.3:block/" + legacy,
                archetype, false, block, metadata, tool, toolAfterDamage, ticks);
    }
}
