package worldline.testkit;

import java.util.List;
import java.util.Map;

/** Proves deterministic three-layer expansion and explicit per-subject overrides. */
public final class BlockConformancePlanTest {
    private BlockConformancePlanTest() {
    }

    static void execute() {
        BlockConformanceProfile stone = new BlockConformanceProfile(
                "b1.7.3:block/001", List.of("simple-solid"), false, Map.of());
        BlockConformanceProfile piston = new BlockConformanceProfile(
                "b1.7.3:block/033", List.of("piston", "redstone-component"), true,
                Map.of("registry-presence", ConformanceLayer.UNIVERSAL));
        List<BlockConformanceTemplate> templates = List.of(
                new BlockConformanceTemplate("registry-presence", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("state-domain", ConformanceLayer.ARCHETYPE));
        List<BlockConformanceCase> cases = new BlockConformancePlan(
                List.of(stone, piston), templates).cases();
        require(cases.size() == 4, "matrix size drifted");
        require(cases.get(0).claimId().equals("b1.7.3:block/001#registry-presence")
                && cases.get(0).layer() == ConformanceLayer.UNIVERSAL,
                "universal route drifted");
        require(cases.get(1).layer() == ConformanceLayer.ARCHETYPE,
                "archetype route drifted");
        require(cases.get(3).layer() == ConformanceLayer.SINGULAR,
                "singular route drifted");
        rejects(() -> new BlockConformancePlan(List.of(stone, stone), templates));
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid conformance plan was accepted");
        } catch (IllegalArgumentException expected) {
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
