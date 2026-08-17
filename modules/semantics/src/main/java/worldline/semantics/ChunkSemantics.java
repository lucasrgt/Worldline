package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Chunk type, lookup, population flags, and chunk-loader interface.
 */
final class ChunkSemantics {
    private ChunkSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("chunk", "CHUNK_TYPE",
                "net/minecraft/src/Chunk", "class", "Chunk", "-",
                "", "", "CHUNK", "controlled-client-tick,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("chunk", "CHUNK_LOOKUP",
                "net/minecraft/src/World", "method", "getChunkFromChunkCoords",
                "(II)Lnet/minecraft/src/Chunk;",
                "WORLD", "", "CHUNK", "controlled-client-tick,symbols.map", "c", 9998));
        mappings.add(SemanticMapping.of("chunk", "CHUNK_POPULATE",
                "net/minecraft/src/Chunk", "method", "func_1024_c", "()V",
                "CHUNK", "CHUNK", "CHUNK,RNG", "controlled-client-tick,symbols.map", "c", 9850));
        mappings.add(SemanticMapping.of("chunk", "CHUNK_POPULATED",
                "net/minecraft/src/Chunk", "field", "isTerrainPopulated", "Z",
                "CHUNK", "CHUNK", "CHUNK", "deterministic-world-tick,lab-cycle", "n", 9998));
        mappings.add(SemanticMapping.of("chunk", "CHUNK_NEVER_SAVE",
                "net/minecraft/src/Chunk", "field", "neverSave", "Z",
                "CHUNK", "CHUNK", "CHUNK", "deterministic-world-tick,lab-cycle", "p", 9998));
        mappings.add(SemanticMapping.of("chunk", "CHUNK_RELIGHT",
                "net/minecraft/src/Chunk", "method", "func_353_b", "()V",
                "CHUNK", "CHUNK", "CHUNK", "deterministic-world-tick", "", 9200));
        mappings.add(SemanticMapping.of("chunk", "CHUNK_LOADER",
                "net/minecraft/src/IChunkLoader", "class", "IChunkLoader", "-",
                "", "", "CHUNK", "deterministic-world-tick,lab-cycle", "", 9998));
        mappings.add(SemanticMapping.of("chunk", "LOADER_LOAD",
                "net/minecraft/src/IChunkLoader", "method", "loadChunk",
                "(Lnet/minecraft/src/World;II)Lnet/minecraft/src/Chunk;",
                "FILESYSTEM", "CHUNK", "CHUNK", "deterministic-world-tick", "a", 9998));
        mappings.add(SemanticMapping.of("chunk", "LOADER_SAVE",
                "net/minecraft/src/IChunkLoader", "method", "saveChunk",
                "(Lnet/minecraft/src/World;Lnet/minecraft/src/Chunk;)V",
                "CHUNK", "FILESYSTEM", "FILESYSTEM", "deterministic-world-tick", "a", 9998));
        mappings.add(SemanticMapping.of("chunk", "LOADER_FLUSH",
                "net/minecraft/src/IChunkLoader", "method", "func_661_a", "()V",
                "CHUNK", "FILESYSTEM", "FILESYSTEM", "deterministic-world-tick", "", 9200));
        return Collections.unmodifiableList(mappings);
    }
}
