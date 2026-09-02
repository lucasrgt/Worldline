package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.BlockStateDomainScenario;
import worldline.testapi.BlockStateDomainStep;
import worldline.testapi.BlockStateObservation;
import worldline.testapi.ConformanceLayer;

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

    public static BlockStateDomainScenario furnaceFacing() {
        return horizontalPlacement("furnace-facing-metadata", "b1.7.3:block/061", 61,
                Arrays.asList("furnace", "directional", "stateful-metadata"), true,
                new int[] {2, 5, 3, 4});
    }

    public static List<BlockStateDomainScenario> cardinalPlacementFamily() {
        return Collections.unmodifiableList(Arrays.asList(
                dispenserFacing(), woodStairsFacing(), chestPlacementMetadata(), furnaceFacing(),
                cobblestoneStairsFacing(), pumpkinFacing(), jackOLanternFacing()));
    }

    public static BlockStateDomainScenario dispenserFacing() {
        return horizontalPlacement("dispenser-facing-metadata", "b1.7.3:block/023", 23,
                Arrays.asList("dispenser", "directional", "tile-entity"), true,
                new int[] {2, 5, 3, 4});
    }

    public static BlockStateDomainScenario woodStairsFacing() {
        return horizontalPlacement("wood-stairs-facing-metadata", "b1.7.3:block/053", 53,
                Arrays.asList("stairs", "directional"), false, new int[] {2, 1, 3, 0});
    }

    public static BlockStateDomainScenario chestPlacementMetadata() {
        return horizontalPlacement("chest-yaw-invariant-metadata", "b1.7.3:block/054", 54,
                Arrays.asList("chest", "container", "directional", "tile-entity"), true,
                new int[] {0, 0, 0, 0});
    }

    public static BlockStateDomainScenario cobblestoneStairsFacing() {
        return horizontalPlacement("cobblestone-stairs-facing-metadata",
                "b1.7.3:block/067", 67, Arrays.asList("stairs", "directional"),
                false, new int[] {2, 1, 3, 0});
    }

    public static BlockStateDomainScenario pumpkinFacing() {
        return horizontalPlacement("pumpkin-facing-metadata", "b1.7.3:block/086", 86,
                Arrays.asList("pumpkin", "directional"), false, new int[] {2, 3, 0, 1});
    }

    public static BlockStateDomainScenario jackOLanternFacing() {
        return horizontalPlacement("jack-o-lantern-facing-metadata", "b1.7.3:block/091", 91,
                Arrays.asList("pumpkin", "directional", "luminous"), false,
                new int[] {2, 3, 0, 1});
    }

    private static BlockStateDomainScenario horizontalPlacement(String id, String subject,
            int blockId, List<String> archetypes, boolean singular, int[] metadata) {
        if (metadata.length != 4) {
            throw new IllegalArgumentException("horizontal metadata matrix must contain four yaws");
        }
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject, archetypes, singular,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        "state-domain", ConformanceLayer.ARCHETYPE)));
        LinkedHashSet<BlockState> distinct = new LinkedHashSet<BlockState>();
        for (int value : metadata) {
            distinct.add(new BlockState(blockId, value));
        }
        List<BlockState> domain = new ArrayList<BlockState>(distinct);
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        float[] yaw = {0F, 90F, 180F, -90F};
        String[] names = {"0", "90", "180", "neg90"};
        for (int index = 0; index < yaw.length; index++) {
            BlockPosition support = B173StateDomainArena.SUPPORTS.get(index);
            steps.add(BlockStateDomainStep.place("place-yaw-" + names[index], support,
                    BlockFace.UP, yaw[index], 0F, Collections.singletonList(
                            new BlockStateObservation(BlockFace.UP.adjacent(support),
                                    new BlockState(blockId, metadata[index])))));
        }
        return new BlockStateDomainScenario(id,
                plan.caseFor(subject, "state-domain"),
                new BlockLifecycleSlot(HOTBAR, INVENTORY,
                        new RemoteItemStack(blockId, 4, 0), null), domain, steps, 40);
    }

}
