package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockCollisionExpectation;
import worldline.testkit.BlockCollisionProbe;
import worldline.testkit.BlockConformanceCase;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.BlockRandomTickSpreadScenario;
import worldline.testkit.ConformanceLayer;

/** Brown and red mushroom rows over the shared roofed official arena. */
public final class B173MushroomRandomTickScenarioFactory {
    public static final long SEED = B173MushroomRandomTickArena.SEED;
    private static final BlockState STONE = new BlockState(1, 0);
    private B173MushroomRandomTickScenarioFactory() { }

    public static List<BlockRandomTickSpreadScenario> rows() {
        return Collections.unmodifiableList(Arrays.asList(row(39, "brown-mushroom"),
                row(40, "red-mushroom")));
    }
    private static BlockRandomTickSpreadScenario row(int id, String name) {
        String subject = String.format("b1.7.3:block/%03d", id);
        List<BlockConformanceTemplate> templates = new ArrayList<>();
        for (String template : Arrays.asList("state-domain", "collision-shape", "light-behavior",
                "tick-policy", "neighbor-response")) {
            templates.add(new BlockConformanceTemplate(template, ConformanceLayer.ARCHETYPE));
        }
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject,
                        Arrays.asList("vegetation", "support-dependent", "random-tick"), false,
                        Collections.emptyMap())), templates);
        List<BlockConformanceCase> claims = new ArrayList<BlockConformanceCase>();
        for (BlockConformanceTemplate template : templates) {
            claims.add(plan.caseFor(subject, template.id()));
        }
        List<BlockPosition> supports = B173MushroomRandomTickStructure.sourceSupports();
        return new BlockRandomTickSpreadScenario(name + "-random-tick-conformance", claims,
                new BlockState(id, 0), STONE, supports,
                B173MushroomRandomTickStructure.targets(),
                B173MushroomRandomTickStructure.CONTROL, supports.get(0),
                B173MushroomRandomTickStructure.LIGHT_PROBE,
                new BlockLifecycleSlot(5, 41, new RemoteItemStack(id, 32, 0),
                        new RemoteItemStack(id, 2, 0)),
                new BlockLifecycleSlot(8, 44, new RemoteItemStack(278, 1, 0),
                        new RemoteItemStack(278, 1, 1)),
                new BlockCollisionProbe("center-path", 0D, 0D, 2D, 10,
                        BlockCollisionExpectation.PASSABLE), 12, 9, 400, 40, 6, 40);
    }
}
