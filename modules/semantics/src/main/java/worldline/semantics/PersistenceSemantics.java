package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Save-handler, chunk-loader extras, world-info, and spawn symbols.
 */
final class PersistenceSemantics {
    private PersistenceSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("persistence", "WORLD_SAVE",
                        "worldline/b173/B173MemoryWorld", "method", "saveWorldInfo",
                        "(Lnet/minecraft/src/WorldInfo;)V", "WORLD", "FILESYSTEM", "FILESYSTEM",
                        "lab-cycle", "", 9920),
                SemanticMapping.of("persistence", "CHUNK_SAVE",
                        "worldline/b173/B173MemoryWorld", "method", "saveChunk",
                        "(Lnet/minecraft/src/World;Lnet/minecraft/src/Chunk;)V",
                        "CHUNK", "FILESYSTEM", "FILESYSTEM", "lab-cycle", "", 9920),
                SemanticMapping.of("persistence", "PLAYER_SAVE",
                        "worldline/b173/B173MemoryWorld", "method", "saveWorldInfoAndPlayer",
                        "(Lnet/minecraft/src/WorldInfo;Ljava/util/List;)V",
                        "PLAYER", "FILESYSTEM", "FILESYSTEM", "lab-cycle", "", 9920),
                SemanticMapping.of("persistence", "LOAD_INFO",
                        "net/minecraft/src/WorldInfo", "class", "WorldInfo", "-",
                        "FILESYSTEM", "WORLD", "FILESYSTEM",
                        "lab-cycle,controlled-client-tick,deterministic-world-tick", "", 9998),
                SemanticMapping.of("persistence", "SAVE_INTERFACE",
                        "net/minecraft/src/ISaveHandler", "class", "ISaveHandler", "-",
                        "", "", "FILESYSTEM", "deterministic-world-tick,lab-cycle", "", 9998),
                SemanticMapping.of("persistence", "EXTRA_CHUNK",
                        "net/minecraft/src/IChunkLoader", "method", "saveExtraChunkData",
                        "(Lnet/minecraft/src/World;Lnet/minecraft/src/Chunk;)V",
                        "CHUNK", "FILESYSTEM", "FILESYSTEM",
                        "deterministic-world-tick,lab-cycle", "b", 9998),
                SemanticMapping.of("persistence", "CHUNK_FLUSH",
                        "worldline/b173/B173MemoryWorld", "method", "func_814_a", "()V",
                        "CHUNK", "FILESYSTEM", "FILESYSTEM", "lab-cycle", "", 9200),
                SemanticMapping.of("persistence", "EXTRA_DATA",
                        "net/minecraft/src/IChunkLoader", "method", "saveExtraData", "()V",
                        "CHUNK", "FILESYSTEM", "FILESYSTEM",
                        "deterministic-world-tick,lab-cycle", "b", 9998),
                SemanticMapping.of("persistence", "SPAWN_SET",
                        "net/minecraft/src/WorldInfo", "method", "setSpawn", "(III)V",
                        "", "WORLD", "FILESYSTEM", "lab-cycle", "", 9920),
                SemanticMapping.of("persistence", "SPAWN_POSITION",
                        "net/minecraft/src/WorldInfo", "method", "setSpawnPosition", "(III)V",
                        "", "WORLD", "FILESYSTEM", "deterministic-world-tick", "", 9200),
                SemanticMapping.of("persistence", "AUTOSAVE_PERIOD",
                        "net/minecraft/src/World", "field", "autosavePeriod", "I",
                        "CLOCK", "PERSISTENCE", "PERSISTENCE",
                        "controlled-client-tick,symbols.map", "p", 9998),
                SemanticMapping.of("persistence", "NATIVE_WORLD_SAVE",
                        "net/minecraft/src/World", "method", "saveWorld",
                        "(ZLnet/minecraft/src/IProgressUpdate;)V",
                        "CHUNK", "FILESYSTEM", "CHUNK,FILESYSTEM",
                        "controlled-client-tick,symbols.map", "a", 9998)));
    }
}
