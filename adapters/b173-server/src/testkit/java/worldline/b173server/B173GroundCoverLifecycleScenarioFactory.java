package worldline.b173server;

import java.util.Arrays;
import java.util.List;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testapi.BlockLifecycleDropMatrix;
import worldline.testapi.BlockLifecycleScenario;

/** Caller-owned tall-grass, dead-bush, and crop rows over the shared lifecycle factory. */
public final class B173GroundCoverLifecycleScenarioFactory {
    private static final BlockState DIRT = new BlockState(3, 0);
    private static final BlockState SAND = new BlockState(12, 0);
    private static final BlockState FARMLAND = new BlockState(60, 0);
    private static final RemoteItemStack SEEDS = new RemoteItemStack(295, 1, 0);
    private static final RemoteItemStack STICK_DROP = new RemoteItemStack(280, 1, 0);
    private static final int STICK = 280;

    private B173GroundCoverLifecycleScenarioFactory() {
    }

    public static List<BlockLifecycleScenario> rows() {
        return Arrays.asList(tallGrass(), deadBush(), crops());
    }

    public static BlockLifecycleScenario tallGrass() {
        return B173LifecycleScenarioFactory.harvestRepeatedDropOnSupport("tall-grass",
                "b1.7.3:block/031", "ground-cover", false, 31, 0, 0, DIRT, STICK, 0, 1,
                SEEDS, 0, 1);
    }

    public static BlockLifecycleScenario deadBush() {
        return B173LifecycleScenarioFactory.harvestRepeatedDropOnSupport("dead-bush",
                "b1.7.3:block/032", "ground-cover", false, 32, 0, 0, SAND, STICK, 0, 1,
                STICK_DROP, 0, 2);
    }

    public static BlockLifecycleScenario crops() {
        BlockLifecycleScenario planted = B173LifecycleScenarioFactory.harvestFromItem("crops",
                "b1.7.3:block/059", "ground-cover", false, 59, 295, 0, 0, FARMLAND, STICK, 0, 1);
        return new BlockLifecycleScenario(planted.id(), planted.placement(), planted.persistence(),
                planted.transition(), planted.drops(), planted.support(), planted.supportState(),
                planted.overheadState(), planted.neighbor(), planted.face(), planted.placedState(),
                planted.placementSlot(), planted.breakSlot(),
                BlockLifecycleDropMatrix.repeated(SEEDS, 0, 3), planted.breakTicks(),
                planted.observationTicks());
    }
}
