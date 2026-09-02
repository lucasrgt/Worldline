package worldline.testkit;
import worldline.testapi.BlockTickPolicyEvidence;
import worldline.testapi.BlockTickPolicyFixture;
import worldline.testapi.BlockTickPolicyMechanism;
import worldline.testapi.BlockTickPolicyObservation;
import worldline.testapi.BlockTickPolicyScenario;
import worldline.testapi.ConformanceLayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Locks multi-mechanism routing, canonical evidence, and fail-closed matching. */
public final class BlockTickPolicyFixtureTest {
    private BlockTickPolicyFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
    }

    static void execute() {
        BlockTickPolicyScenario sand = scenario("sand-fall", "012", false,
                BlockTickPolicyMechanism.SCHEDULED_BLOCK, "12:0-supported",
                "entity-70>12:0-landed");
        BlockTickPolicyScenario furnace = scenario("furnace-progress", "061", true,
                BlockTickPolicyMechanism.TILE_ENTITY, "61:4-burn1600-cook0",
                "62:4-burn1401-cook200-output20x1");
        List<BlockTickPolicyEvidence> evidence = BlockTickPolicyFixture.execute(
                Arrays.asList(sand, furnace), Arrays.asList(observation(sand),
                        observation(furnace)));
        require(evidence.get(0).layer() == ConformanceLayer.ARCHETYPE
                        && evidence.get(1).layer() == ConformanceLayer.SINGULAR,
                "tick-policy layer routing drifted");
        require(BlockTickPolicyFixture.canonical(evidence).equals(
                "schema=worldline.block-tick-policy-evidence.v1\nrows=2\n"
                + "scenario=sand-fall\nclaim=b1.7.3:block/012#tick-policy|ARCHETYPE\n"
                + "mechanism=SCHEDULED_BLOCK\ninitial=12:0-supported\n"
                + "effect=entity-70>12:0-landed\npersisted=true\n"
                + "scenario=furnace-progress\n"
                + "claim=b1.7.3:block/061#tick-policy|SINGULAR\n"
                + "mechanism=TILE_ENTITY\ninitial=61:4-burn1600-cook0\n"
                + "effect=62:4-burn1401-cook200-output20x1\npersisted=true\n"),
                "tick-policy canonical evidence drifted");
        rejects(() -> BlockTickPolicyFixture.execute(Collections.singletonList(sand),
                Collections.singletonList(new BlockTickPolicyObservation("sand-fall",
                        BlockTickPolicyMechanism.RANDOM_BLOCK, sand.initial(), sand.effect(), true))));
        rejects(() -> new BlockTickPolicyScenario("bad", "bad-subject",
                Collections.singletonList("gravity-block"), false,
                BlockTickPolicyMechanism.SCHEDULED_BLOCK, "12:0", "landed", true));
        System.out.println("BlockTickPolicyFixtureTest passed");
    }

    private static BlockTickPolicyScenario scenario(String id, String legacyId,
            boolean singular, BlockTickPolicyMechanism mechanism, String initial,
            String effect) {
        return new BlockTickPolicyScenario(id, "b1.7.3:block/" + legacyId,
                Collections.singletonList(singular ? "tile-machine" : "gravity-block"),
                singular, mechanism, initial, effect, true);
    }

    private static BlockTickPolicyObservation observation(BlockTickPolicyScenario scenario) {
        return new BlockTickPolicyObservation(scenario.id(), scenario.mechanism(),
                scenario.initial(), scenario.effect(), scenario.persisted());
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid tick-policy row was accepted");
        } catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
