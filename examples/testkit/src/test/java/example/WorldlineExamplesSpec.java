package example;

import java.util.Arrays;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.*;

/** Ten ordinary Java examples covering the experimental TestKit 0.x surface. */
public final class WorldlineExamplesSpec extends WorldlineSpec {
    @Override protected void define() {
        describe("blocks", () -> {
            test("places glass", context -> {
                context.setBlock(pos(8, 65, 8), block("b1.7.3:glass"));
                expect(context.block(pos(8, 65, 8))).toEqual(block("b1.7.3:glass"));
            }).tag("block");
            it("persists across a tick", context -> {
                context.setBlock(pos(8, 65, 8), block("b1.7.3:glass")); context.tick();
                expect(context.block(8, 65, 8)).toEqual(block("b1.7.3:glass"));
            });
        });
        describe("player", () -> {
            test("starts healthy", context -> expect(context.runtime().player().health()).toEqual(20));
            test("has a selected slot", context ->
                    expect(context.runtime().player().selectedHotbarSlot()).toEqual(0));
            test("health snapshot", context ->
                    expect(context.runtime().player().health()).toMatchSnapshot("initial-health"));
        });
        test("records named steps", context -> {
            context.step("place glass", step ->
                    step.setBlock(pos(8, 65, 8), block("b1.7.3:glass")));
            context.step("tick", step -> step.tick());
        }).tag("scenario");
        test("attaches diagnostics", context -> context.attach("note.txt", "example attachment"));
        test("detects change", context -> expect(() -> context.runtime().world().time())
                .toChange(() -> context.tick()));
        each(Arrays.asList(1, 2)).test("ticks %# = %s", (context, ticks) -> {
            context.tick(ticks); expect(context.runtime().world().time()).toBeGreaterThan(0);
        });
        test("documents a known gap", context -> {}).skip();
        test("reserves a future contract", context -> {}).todo();
    }
}
