package worldline.stationapi.test;

import java.util.HashSet;
import java.util.Set;
import worldline.api.UiMinecraftRuntime;
import worldline.api.GameUiNode;
import worldline.test.WorldlineSpec;
import worldline.testkit.WorldControlEvidence;
import worldline.testkit.WorldControlFixture;
import worldline.stationapi.StationApiControlProtocolTest;
import static worldline.test.Worldline.test;
import static worldline.test.Worldline.afterAll;

/** Two real local StationAPI clients share public acceptance, never world files or UI identities. */
public final class StationApiWorldControlSpec extends WorldlineSpec {
    private static final Set<String> USERS = new HashSet<String>();
    private static WorldControlEvidence first;
    private static GameUiNode foreign;
    @Override protected void define() {
        test("first local StationAPI world control", context -> exercise((UiMinecraftRuntime) context.runtime()));
        test("second isolated StationAPI world control", context -> exercise((UiMinecraftRuntime) context.runtime()));
        afterAll(() -> {
            if (USERS.size() != 2) throw new AssertionError("session reuse");
        });
    }
    private static void exercise(UiMinecraftRuntime runtime) {
        StationApiControlProtocolTest.verify();
        if (!USERS.add(runtime.player().username())) throw new AssertionError("world identity was reused");
        if (foreign != null) {
            runtime.ui().openInventory();
            try {
                runtime.ui().click(foreign);
                throw new AssertionError("foreign node accepted");
            }
            catch (IllegalArgumentException expected) { }
            runtime.ui().close();
        }
        WorldControlEvidence evidence = WorldControlFixture.exercise(runtime);
        runtime.ui().openInventory();
        foreign = runtime.ui().slot(37);
        runtime.ui().close();
        if (first == null) first = evidence;
        else if (!first.equals(evidence)) throw new AssertionError("native control replica divergence");
        System.out.println("WORLDLINE_STATIONAPI_CONTROLS=" + evidence.canonical());
    }
}
