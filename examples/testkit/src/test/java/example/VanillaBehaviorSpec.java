package example;

import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineEvidence;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;

/** End-to-end Java 8 example comparing mod evidence with a frozen vanilla pin. */
public final class VanillaBehaviorSpec extends WorldlineSpec {
    private static final String SIGNAL =
            "walk-off=cap9,steps=7,pose-y<0,health=20->0->20,packet8=0,packet9=09:00,"
            + "dimension=0,spawn-y>=0,persisted=20,clients=1,disconnect=clean";
    private static final String SIGNATURE =
            "52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98";

    @Override
    protected void define() {
        test("mod preserves vanilla void death", context -> {
            WorldlineEvidence observed = WorldlineEvidence.of(WorldlineBehavior.VOID_DEATH,
                    WorldlineEvidence.MOD, SIGNAL, SIGNATURE);
            expect(observed).toMatchVanilla(WorldlineBehavior.VOID_DEATH, SIGNAL, SIGNATURE);
        }).tag("behavior", "vanilla-pin");
    }
}
