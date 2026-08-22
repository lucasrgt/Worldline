package worldline.testapi;

import java.util.Arrays;
import java.util.List;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineEvidence;
import worldline.test.TestDefinition;
import worldline.test.TestNode;
import worldline.test.TestPlan;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.describe;
import static worldline.test.Worldline.block;
import static worldline.test.Worldline.entity;
import static worldline.test.Worldline.each;
import static worldline.test.Worldline.it;
import static worldline.test.Worldline.test;

/** Collection, aliases, immutability, and assertion contract tests. */
public final class WorldlineDslTest {
    private WorldlineDslTest() {}
    public static void main(String[] arguments) throws Exception {
        TestPlan plan = new Sample().collect();
        List<TestNode> children = plan.root().children();
        require(children.size() == 4, "root declaration order");
        require(children.get(0).name().equals("first") && children.get(1).name().equals("group")
                && children.get(2).name().equals("row 0 a"), "aliases and each order");
        TestDefinition first = (TestDefinition) children.get(0);
        require(first.tags().equals(Arrays.asList("fast")) && first.retries() == 1,
                "test modifiers");
        failure(first::skip, "collected plan remained mutable");
        expect("worldline").toContain("line"); expect(4).toBeGreaterThan(3);
        final int[] value = {1};
        expect(() -> value[0]).toChangeFromTo(1, 2, () -> value[0]++);
        require(block("b1.7.3:glass").legacyId() == 20
                && entity("b1.7.3:pig").legacyId() == 90, "semantic selectors");
        failure(() -> block("b1.7.3:guess"), "unknown mapping was accepted");
        behaviorEvidence();
        failure(() -> expect(1).toEqual(2), "equality assertion passed");
        failure(() -> test("outside", context -> {}), "DSL worked outside collection");
        System.out.println("WorldlineDslTest passed");
    }

    private static void behaviorEvidence() {
        String signal = "walk-off=cap9,steps=7,pose-y<0,health=20->0->20,packet8=0,packet9=09:00,"
                + "dimension=0,spawn-y>=0,persisted=20,clients=1,disconnect=clean";
        String signature = "52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98";
        WorldlineEvidence mod = WorldlineEvidence.of(
                WorldlineBehavior.VOID_DEATH, WorldlineEvidence.MOD, signal, signature);
        expect(mod).toMatchVanilla("atlas.scenario.void-death", signal, signature);
        failure(() -> expect(WorldlineEvidence.of(WorldlineBehavior.VOID_DEATH, WorldlineEvidence.MOD,
                signal, "702d4dc074d1db9a965d74f49f1318cb05a4397c343a59b8fde15a3ab8f15505"))
                .toMatchVanilla(WorldlineBehavior.VOID_DEATH, signal, signature),
                "divergent behavior evidence was accepted");
    }

    private static final class Sample extends WorldlineSpec {
        @Override protected void define() {
            test("first", context -> {}).tag("fast").retry(1);
            describe("group", () -> it("alias", context -> {}).todo());
            each(Arrays.asList("a", "b")).test("row %# %s", (context, value) -> {}).get(1).skip();
        }
    }
    private static void failure(Action action, String message) {
        try { action.run(); throw new AssertionError(message); }
        catch (IllegalArgumentException | IllegalStateException | AssertionError expected) {
            if (expected instanceof AssertionError && expected.getMessage().equals(message)) throw expected;
        } catch (Exception expected) { /* expected checked failure */ }
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
