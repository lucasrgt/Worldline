package worldline.testapi;

import java.util.Arrays;
import java.util.List;
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
        failure(() -> expect(1).toEqual(2), "equality assertion passed");
        failure(() -> test("outside", context -> {}), "DSL worked outside collection");
        System.out.println("WorldlineDslTest passed");
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
