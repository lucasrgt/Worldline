package worldline.testkit;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.ConformanceLayer;
import worldline.testapi.FluidDynamicsEvidence;
import worldline.testapi.FluidDynamicsScenario;

import java.util.List;
import java.util.Map;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Locks the four-claim public contract used by source-fluid dynamics rows. */
public final class FluidDynamicsContractTest {
    private FluidDynamicsContractTest() { }

    public static void main(String[] arguments) {
        execute();
    }

    static void execute() {
        BlockConformancePlan plan = plan();
        require(plan.cases().size() == 8, "fluid dynamics claim routing drifted");
        require(plan.caseFor("b1.7.3:block/009", "gameplay-placement").layer()
                        == ConformanceLayer.UNIVERSAL
                && plan.caseFor("b1.7.3:block/009", "tick-policy").layer()
                        == ConformanceLayer.ARCHETYPE,
                "fluid dynamics layers drifted");
        FluidDynamicsScenario water = row(plan, 9, "still-water", new BlockState(9, 1));
        FluidDynamicsScenario lava = row(plan, 11, "still-lava", new BlockState(11, 2));
        require(water.source().equals(new BlockPosition(4, 72, 4))
                && water.flow().equals(new BlockPosition(5, 72, 4))
                && lava.flowState().equals(new BlockState(11, 2)),
                "fluid dynamics channel geometry drifted");
        String evidence = new FluidDynamicsEvidence(water, ReloadBoundary.FRESH_LOGIN).canonical();
        require(evidence.contains("claim.gameplay-placement=b1.7.3:block/009#gameplay-placement")
                && evidence.contains("gate=5:72:4:1:0->0:0->9:1")
                && evidence.endsWith("reload=FRESH_LOGIN\n"),
                "fluid dynamics canonical evidence drifted");
        System.out.println("FluidDynamicsContractTest passed");
    }

    private static FluidDynamicsScenario row(BlockConformancePlan plan,
            int id, String name, BlockState flow) {
        String subject = String.format("b1.7.3:block/%03d", id);
        return new FluidDynamicsScenario(name + "-source-fluid-dynamics",
                plan.caseFor(subject, "gameplay-placement"),
                plan.caseFor(subject, "save-reload"),
                plan.caseFor(subject, "tick-policy"),
                plan.caseFor(subject, "neighbor-response"), new BlockPosition(4, 71, 4),
                new BlockState(1, 0), new BlockState(id, 0), new BlockState(1, 0), flow,
                slot(1, id, null), slot(2, 278, new RemoteItemStack(278, 1, 1)), 40, 20, 70);
    }

    private static BlockConformancePlan plan() {
        return new BlockConformancePlan(List.of(
                new BlockConformanceProfile("b1.7.3:block/009",
                        List.of("fluid", "tick-driven"), false, Map.of()),
                new BlockConformanceProfile("b1.7.3:block/011",
                        List.of("fluid", "tick-driven"), false, Map.of())), List.of(
                new BlockConformanceTemplate("gameplay-placement", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("save-reload", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("tick-policy", ConformanceLayer.ARCHETYPE),
                new BlockConformanceTemplate("neighbor-response", ConformanceLayer.ARCHETYPE)));
    }

    private static BlockLifecycleSlot slot(int hotbar, int id, RemoteItemStack after) {
        return new BlockLifecycleSlot(hotbar, hotbar + 36,
                new RemoteItemStack(id, 1, 0), after);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
