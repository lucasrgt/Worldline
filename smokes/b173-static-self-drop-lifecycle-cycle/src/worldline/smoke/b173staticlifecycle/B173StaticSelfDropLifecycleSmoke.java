package worldline.smoke.b173staticlifecycle;

import java.util.Arrays;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Five independently signed static self-drop rows over the public lifecycle provider. */
public final class B173StaticSelfDropLifecycleSmoke {
    private B173StaticSelfDropLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "static-self-drop", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                row("sponge", "019", "sponge", true, 19, 278, 1, 30),
                row("white-wool", "035", "wool", false, 35, 359, 0, 30),
                row("tnt", "046", "tnt", true, 46, 278, 1, 30),
                row("fence", "085", "fence", false, 85, 258, 1, 40),
                row("netherrack", "087", "netherrack", false, 87, 278, 1, 20)));
    }

    private static BlockLifecycleScenario row(String id, String legacy, String archetype,
            boolean singular, int block, int tool, int toolAfterDamage, int ticks) {
        return B173LifecycleScenarioFactory.selfDrop(id, "b1.7.3:block/" + legacy,
                archetype, singular, block, 0, tool, toolAfterDamage, ticks);
    }
}
