package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Filesystem-category mappings for the b1.7.3 semantic catalog. Roles cover
 * the virtual filesystem, save handler, stat writer, journal, and world files.
 */
final class FilesystemSemantics {
    private FilesystemSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("filesystem", "VIRTUAL_FILESYSTEM",
                        "worldline/b173/B173VirtualFileSystem", "class", "B173VirtualFileSystem",
                        "-", "", "FILESYSTEM", "FILESYSTEM", "lab-cycle", "", 9990),
                SemanticMapping.of("filesystem", "SAVE_HANDLER",
                        "worldline/b173/B173MemoryWorld", "class", "B173MemoryWorld", "-",
                        "FILESYSTEM", "FILESYSTEM", "FILESYSTEM",
                        "lab-cycle,controlled-client-tick", "", 9990),
                SemanticMapping.of("filesystem", "STAT_FILE",
                        "net/minecraft/src/StatFileWriter", "class", "StatFileWriter", "-",
                        "FILESYSTEM", "", "FILESYSTEM",
                        "controlled-client-tick,symbols.map", "", 9998),
                SemanticMapping.of("filesystem", "STAT_WRITER",
                        "net/minecraft/src/StatFileWriter", "method", "func_27178_d", "()V",
                        "FILESYSTEM", "FILESYSTEM", "FILESYSTEM",
                        "controlled-client-tick,symbols.map", "d", 9850),
                SemanticMapping.of("filesystem", "FS_FAIL",
                        "worldline/b173/B173VirtualFileSystem", "method", "failNext",
                        "(Ljava/lang/String;)V", "", "FILESYSTEM", "FILESYSTEM", "lab-cycle",
                        "", 9920),
                SemanticMapping.of("filesystem", "FS_JOURNAL",
                        "worldline/b173/B173VirtualFileSystem", "method", "operations",
                        "()Ljava/util/List;", "FILESYSTEM", "", "FILESYSTEM", "lab-cycle",
                        "", 9920),
                SemanticMapping.of("filesystem", "WORLD_LOAD",
                        "worldline/b173/B173MemoryWorld", "method", "loadWorldInfo",
                        "()Lnet/minecraft/src/WorldInfo;", "FILESYSTEM", "WORLD", "FILESYSTEM",
                        "lab-cycle", "", 9920),
                SemanticMapping.of("filesystem", "WORLD_LOCK",
                        "worldline/b173/B173MemoryWorld", "method", "func_22150_b", "()V",
                        "FILESYSTEM", "FILESYSTEM", "FILESYSTEM", "lab-cycle", "", 9200),
                SemanticMapping.of("filesystem", "WORLD_FILE",
                        "worldline/b173/B173MemoryWorld", "method", "func_28113_a",
                        "(Ljava/lang/String;)Ljava/io/File;", "FILESYSTEM", "FILESYSTEM",
                        "FILESYSTEM", "lab-cycle", "", 9200),
                SemanticMapping.of("filesystem", "CHUNK_LOAD",
                        "worldline/b173/B173MemoryWorld", "method", "loadChunk",
                        "(Lnet/minecraft/src/World;II)Lnet/minecraft/src/Chunk;",
                        "FILESYSTEM", "CHUNK", "FILESYSTEM,CHUNK", "lab-cycle", "", 9920)));
    }
}
