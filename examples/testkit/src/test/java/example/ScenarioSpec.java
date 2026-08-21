package example;

import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;

public final class ScenarioSpec extends WorldlineSpec {
    @Override protected void define() {
        test("records one step", context -> {
            context.step("tick once", step -> step.tick()); expect(context.attempt()).toEqual(1);
        });
        test("records ordered steps", context -> {
            context.step("first", step -> step.tick()); context.step("second", step -> step.tick());
            expect(context.runtime().world().time()).toBeGreaterThan(0);
        });
    }
}
