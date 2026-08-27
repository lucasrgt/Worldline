package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

/** Official b1.7.3 support-face state-domain rows. */
public final class B173AttachmentStateDomainScenarioFactory {
    private static final int HOTBAR = 1;
    private static final int INVENTORY = 37;

    private B173AttachmentStateDomainScenarioFactory() {
    }

    public static List<BlockStateDomainScenario> wallAttachmentFamily() {
        return Collections.unmodifiableList(Arrays.asList(
                torchAttachment(), ladderAttachment(), wallSignAttachment()));
    }

    public static BlockStateDomainScenario torchAttachment() {
        return attachmentPlacement("torch-attachment-metadata", "b1.7.3:block/050", 50, 50,
                Arrays.asList("support-dependent", "luminous", "directional"),
                new BlockFace[] {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH,
                        BlockFace.NORTH, BlockFace.UP}, new int[] {1, 2, 3, 4, 5});
    }

    public static BlockStateDomainScenario ladderAttachment() {
        return attachmentPlacement("ladder-attachment-metadata", "b1.7.3:block/065", 65, 65,
                Arrays.asList("support-dependent", "directional", "special-collision"),
                new BlockFace[] {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH},
                new int[] {5, 4, 3, 2});
    }

    public static BlockStateDomainScenario wallSignAttachment() {
        return attachmentPlacement("wall-sign-attachment-metadata", "b1.7.3:block/068", 323, 68,
                Arrays.asList("sign", "support-dependent", "directional", "tile-entity"),
                new BlockFace[] {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH},
                new int[] {5, 4, 3, 2});
    }

    private static BlockStateDomainScenario attachmentPlacement(String id, String subject,
            int itemId, int blockId, List<String> archetypes, BlockFace[] faces, int[] metadata) {
        if (faces.length != metadata.length || faces.length < 1) {
            throw new IllegalArgumentException("attachment metadata matrix drifted");
        }
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject, archetypes, false,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        "state-domain", ConformanceLayer.ARCHETYPE)));
        List<BlockState> domain = new ArrayList<BlockState>();
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        for (int index = 0; index < faces.length; index++) {
            BlockPosition support = support(faces[index]);
            BlockState state = new BlockState(blockId, metadata[index]);
            domain.add(state);
            steps.add(BlockStateDomainStep.place("place-face-"
                    + faces[index].name().toLowerCase(Locale.ROOT), support,
                    faces[index], 0F, 0F, Collections.singletonList(
                            new BlockStateObservation(faces[index].adjacent(support), state))));
        }
        return new BlockStateDomainScenario(id, plan.caseFor(subject, "state-domain"),
                new BlockLifecycleSlot(HOTBAR, INVENTORY,
                        new RemoteItemStack(itemId, faces.length, 0), null), domain, steps, 40);
    }

    private static BlockPosition support(BlockFace face) {
        if (face == BlockFace.EAST) {
            return B173StateDomainArena.SUPPORTS.get(1);
        }
        if (face == BlockFace.SOUTH) {
            return B173StateDomainArena.SUPPORTS.get(2);
        }
        if (face == BlockFace.UP) {
            return B173StateDomainArena.SUPPORTS.get(3);
        }
        return B173StateDomainArena.SUPPORTS.get(0);
    }
}
