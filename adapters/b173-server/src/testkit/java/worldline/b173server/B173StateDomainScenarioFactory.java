package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.BlockStateDomainScenario;
import worldline.testkit.BlockStateDomainStep;
import worldline.testkit.BlockStateObservation;
import worldline.testkit.ConformanceLayer;

/** Official b1.7.3 state-domain rows bound to the provider's gameplay arena. */
public final class B173StateDomainScenarioFactory {
    public static final long SEED = B173StateDomainArena.SEED;
    private static final int HOTBAR = 1, INVENTORY = 37;

    private B173StateDomainScenarioFactory() { }

    public static BlockStateDomainScenario woodenDoor() {
        String subject = "b1.7.3:block/064";
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject, Collections.singletonList("directional"),
                        true, Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        "state-domain", ConformanceLayer.ARCHETYPE)));
        List<BlockState> domain = new ArrayList<BlockState>();
        for (int metadata = 0; metadata <= 15; metadata++) {
            domain.add(new BlockState(64, metadata));
        }
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        float[] yaw = {-90F, 0F, 90F, 180F};
        for (int direction = 0; direction < 4; direction++) {
            BlockPosition support = B173StateDomainArena.SUPPORTS.get(direction);
            BlockPosition lower = BlockFace.UP.adjacent(support);
            BlockPosition upper = BlockFace.UP.adjacent(lower);
            steps.add(BlockStateDomainStep.place("place-face-" + direction, support, BlockFace.UP,
                    yaw[direction], 0F, Arrays.asList(
                            new BlockStateObservation(lower, new BlockState(64, direction)),
                            new BlockStateObservation(upper, new BlockState(64, direction + 8)))));
        }
        for (int direction = 0; direction < 4; direction++) {
            BlockPosition lower = BlockFace.UP.adjacent(
                    B173StateDomainArena.SUPPORTS.get(direction));
            BlockPosition upper = BlockFace.UP.adjacent(lower);
            steps.add(BlockStateDomainStep.activate("open-face-" + direction, lower, BlockFace.UP,
                    Arrays.asList(
                            new BlockStateObservation(lower, new BlockState(64, direction + 4)),
                            new BlockStateObservation(upper, new BlockState(64, direction + 12)))));
        }
        return new BlockStateDomainScenario("wooden-door-complete-metadata",
                plan.caseFor(subject, "state-domain"),
                new BlockLifecycleSlot(HOTBAR, INVENTORY,
                        new RemoteItemStack(324, 4, 0), null), domain, steps, 40);
    }
}
