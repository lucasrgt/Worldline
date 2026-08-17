package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * ISaveHandler and IPlayerFileData contract names from the world-tick map.
 */
final class SaveSemantics {
    private SaveSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("save", "HANDLER_LOAD", "net/minecraft/src/ISaveHandler",
                        "method", "func_22096_c", "()Lnet/minecraft/src/WorldInfo;",
                        "FILESYSTEM", "WORLD", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "HANDLER_LOCK", "net/minecraft/src/ISaveHandler",
                        "method", "func_22091_b", "()V", "FILESYSTEM", "FILESYSTEM",
                        "FILESYSTEM", "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "HANDLER_LOADER", "net/minecraft/src/ISaveHandler",
                        "method", "func_22092_a",
                        "(Lnet/minecraft/src/WorldProvider;)Lnet/minecraft/src/IChunkLoader;",
                        "FILESYSTEM", "CHUNK", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "HANDLER_PLAYERS", "net/minecraft/src/ISaveHandler",
                        "method", "func_22095_a",
                        "(Lnet/minecraft/src/WorldInfo;Ljava/util/List;)V",
                        "PLAYER", "FILESYSTEM", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "HANDLER_INFO", "net/minecraft/src/ISaveHandler",
                        "method", "func_22094_a", "(Lnet/minecraft/src/WorldInfo;)V",
                        "WORLD", "FILESYSTEM", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "HANDLER_PLAYER_DATA",
                        "net/minecraft/src/ISaveHandler", "method", "func_22090_d",
                        "()Lnet/minecraft/src/IPlayerFileData;", "PLAYER", "", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "HANDLER_CLOSE", "net/minecraft/src/ISaveHandler",
                        "method", "func_22093_e", "()V", "FILESYSTEM", "FILESYSTEM",
                        "FILESYSTEM", "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "HANDLER_FILE", "net/minecraft/src/ISaveHandler",
                        "method", "func_28111_b", "(Ljava/lang/String;)Ljava/io/File;",
                        "FILESYSTEM", "FILESYSTEM", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "PLAYER_FILES", "net/minecraft/src/IPlayerFileData",
                        "class", "IPlayerFileData", "-", "", "", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "PLAYER_WRITE", "net/minecraft/src/IPlayerFileData",
                        "method", "writePlayerData", "(Lnet/minecraft/src/EntityPlayer;)V",
                        "PLAYER", "FILESYSTEM", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200),
                SemanticMapping.of("save", "PLAYER_READ", "net/minecraft/src/IPlayerFileData",
                        "method", "readPlayerData", "(Lnet/minecraft/src/EntityPlayer;)V",
                        "FILESYSTEM", "PLAYER", "FILESYSTEM",
                        "deterministic-world-tick", "", 9200)));
    }
}
