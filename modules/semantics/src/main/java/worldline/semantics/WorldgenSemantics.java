package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public TestKit boundaries for bounded Beta 1.7.3 terrain and structure generation. */
final class WorldgenSemantics {
    private WorldgenSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("worldgen", "WORLDGEN_TERRAIN_CENSUS_TESTKIT",
                        "worldline/testkit/TerrainGenerationFixture", "method", "observe",
                        "(Lworldline/api/RemoteWorldView;IIII)"
                                + "Lworldline/testkit/TerrainGenerationFixture$Evidence;",
                        "WORLDGEN", "", "WORLDGEN",
                        "m621-save-worldgen-set", "", 9998),
                SemanticMapping.of("worldgen", "WORLDGEN_TERRAIN_REPLAY_EVIDENCE",
                        "worldline/testkit/TerrainGenerationFixture$Evidence", "method",
                        "replayEquals",
                        "(Lworldline/testkit/TerrainGenerationFixture$Evidence;)Z",
                        "WORLDGEN", "", "WORLDGEN",
                        "m621-save-worldgen-set", "", 9998),
                SemanticMapping.of("worldgen", "WORLDGEN_TERRAIN_CANONICAL_EVIDENCE",
                        "worldline/testkit/TerrainGenerationFixture$Evidence", "method",
                        "describe", "()Ljava/lang/String;",
                        "WORLDGEN", "", "WORLDGEN",
                        "m621-save-worldgen-set", "", 9998),
                SemanticMapping.of("worldgen", "WORLDGEN_DUNGEON_CENSUS_TESTKIT",
                        "worldline/testkit/DungeonGenerationFixture", "method", "observe",
                        "(Lworldline/api/RemoteWorldView;IIII)"
                                + "Lworldline/testkit/DungeonGenerationFixture$Evidence;",
                        "WORLDGEN", "", "WORLDGEN",
                        "m626-dungeon-generation-census", "", 9998)));
    }
}
