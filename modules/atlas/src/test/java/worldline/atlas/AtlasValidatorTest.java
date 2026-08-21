package worldline.atlas;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import worldline.semantics.SemanticCatalog;

public final class AtlasValidatorTest {
    private AtlasValidatorTest() {}

    public static void main(String[] arguments) {
        SemanticCatalog catalog = SemanticCatalog.standard();
        failure(() -> AtlasStatus.parse("GUESSED"));
        failure(() -> AtlasKind.parse("wiki"));
        require(AtlasKind.MAPPING_SET.equals(AtlasKind.ofId("atlas.mapping-set.biny")));
        failure(() -> AtlasRecord.of("atlas.role.CLIENT_TICK_ROOT", AtlasKind.INVARIANT,
                AtlasStatus.STRONG, AtlasSchema.CLIENT, AtlasSchema.SCOPE, "x", "", 0,
                Collections.singletonList("lab-cycle"), Collections.<String>emptyList()));
        failure(() -> AtlasRecord.of("atlas.coverage-unit.worldgen.TESTABILITY",
                AtlasKind.COVERAGE_UNIT, AtlasStatus.UNKNOWN, AtlasSchema.WORLDLINE,
                AtlasSchema.SCOPE, "worldgen TESTABILITY", "0", 0,
                Collections.<String>emptyList(),
                Collections.singletonList("atlas.subsystem.worldgen")));
        AtlasRecord verified = AtlasRecord.of("atlas.invariant.item-conservation",
                AtlasKind.INVARIANT, AtlasStatus.VERIFIED, AtlasSchema.WORLDLINE,
                AtlasSchema.SCOPE, "item-conservation", "", 0,
                Collections.singletonList("handwave"), Collections.<String>emptyList());
        failure(() -> AtlasStore.of(Collections.singletonList(verified), catalog, Paths.get(".")));
        AtlasRecord worldgen = AtlasRecord.of("atlas.subsystem.worldgen", AtlasKind.SUBSYSTEM,
                AtlasStatus.UNKNOWN, AtlasSchema.CLIENT, AtlasSchema.SCOPE, "worldgen", "", 0,
                Collections.<String>emptyList(), Collections.<String>emptyList());
        failure(() -> AtlasStore.of(Arrays.asList(worldgen, worldgen), catalog, Paths.get(".")));
        AtlasRecord broken = AtlasRecord.of("atlas.subsystem.redstone", AtlasKind.SUBSYSTEM,
                AtlasStatus.UNKNOWN, AtlasSchema.CLIENT, AtlasSchema.SCOPE, "redstone", "", 0,
                Collections.<String>emptyList(),
                Collections.singletonList("atlas.role.NOT_A_ROLE"));
        failure(() -> AtlasStore.of(Collections.singletonList(broken), catalog, Paths.get(".")));
        System.out.println("AtlasValidatorTest passed");
    }

    private static void failure(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException error) {
            return;
        }
        throw new AssertionError("expected fail-closed atlas validation");
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError("requirement failed");
    }
}
