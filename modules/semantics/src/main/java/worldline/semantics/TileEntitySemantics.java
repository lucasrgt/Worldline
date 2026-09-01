package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public TestKit boundary for four qualified Beta 1.7.3 tile-entity archetypes. */
final class TileEntitySemantics {
    private TileEntitySemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("tile-entity", "TILE_ENTITY_FURNACE_TESTKIT",
                        "worldline/testkit/FurnaceSubsystemFixture", "method", "execute",
                        "(Lworldline/testkit/FurnaceSubsystemScenario;)"
                                + "Lworldline/testkit/FurnaceSubsystemEvidence;",
                        "TILE_ENTITY", "TILE_ENTITY", "TILE_ENTITY",
                        "b173-furnace-subsystem-conformance-cycle", "", 9998),
                SemanticMapping.of("tile-entity", "TILE_ENTITY_MOB_SPAWNER_TESTKIT",
                        "worldline/testkit/MobSpawnerSubsystemFixture", "method", "execute",
                        "(Lworldline/testkit/MobSpawnerSubsystemScenario;)"
                                + "Lworldline/testkit/MobSpawnerSubsystemEvidence;",
                        "TILE_ENTITY", "TILE_ENTITY", "TILE_ENTITY",
                        "b173-mob-spawner-subsystem-conformance-cycle", "", 9998),
                SemanticMapping.of("tile-entity", "TILE_ENTITY_PISTON_TESTKIT",
                        "worldline/testkit/PistonSubsystemScenario", "method", "observe",
                        "()Lworldline/testkit/PistonSubsystemObservation;",
                        "TILE_ENTITY", "TILE_ENTITY", "TILE_ENTITY",
                        "b173-piston-subsystem-conformance-cycle", "", 9998),
                SemanticMapping.of("tile-entity", "TILE_ENTITY_SIGN_TESTKIT",
                        "worldline/testkit/SignSubsystemFixture", "method", "execute",
                        "(Lworldline/testkit/SignSubsystemScenario;)"
                                + "Lworldline/testkit/SignSubsystemEvidence;",
                        "TILE_ENTITY", "TILE_ENTITY", "TILE_ENTITY",
                        "b173-sign-subsystem-lifecycle-cycle", "", 9998)));
    }
}
