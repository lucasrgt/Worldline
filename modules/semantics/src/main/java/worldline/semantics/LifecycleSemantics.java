package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Lifecycle-category mappings for the b1.7.3 semantic catalog. Roles cover
 * runtime factory, headless boot, world load, manual tick, and close.
 */
final class LifecycleSemantics {
    private LifecycleSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("lifecycle", "CLIENT_TYPE",
                        "net/minecraft/client/Minecraft", "class", "Minecraft", "-",
                        "", "", "LIFECYCLE",
                        "controlled-client-tick,symbols.map", "", 9998),
                SemanticMapping.of("lifecycle", "CLIENT_WORLD",
                        "net/minecraft/client/Minecraft", "field", "theWorld",
                        "Lnet/minecraft/src/World;", "WORLD", "WORLD", "LIFECYCLE",
                        "controlled-client-tick,m3-domain-api,lab-cycle", "f", 9998),
                SemanticMapping.of("lifecycle", "CLIENT_PLAYER",
                        "net/minecraft/client/Minecraft", "field", "thePlayer",
                        "Lnet/minecraft/src/EntityPlayerSP;", "PLAYER", "PLAYER", "LIFECYCLE",
                        "controlled-client-tick,m3-domain-api,lab-cycle", "h", 9998),
                SemanticMapping.of("lifecycle", "CLIENT_SESSION",
                        "net/minecraft/client/Minecraft", "field", "session",
                        "Lnet/minecraft/src/Session;", "NETWORK", "NETWORK", "LIFECYCLE",
                        "controlled-client-tick,lab-cycle,gui-tree", "k", 9998),
                SemanticMapping.of("lifecycle", "RUNTIME_FACTORY",
                        "worldline/b173/B173Runtimes", "method", "create",
                        "(J)Lworldline/b173/B173Runtime;", "", "LIFECYCLE", "LIFECYCLE",
                        "lab-cycle,controlled-client-tick", "", 9998),
                SemanticMapping.of("lifecycle", "BOOT_HEADLESS",
                        "worldline/api/MinecraftRuntime", "method", "bootHeadless", "()V",
                        "", "LIFECYCLE", "LIFECYCLE",
                        "controlled-client-tick,m3-domain-api", "", 9998),
                SemanticMapping.of("lifecycle", "LOAD_WORLD",
                        "worldline/api/MinecraftRuntime", "method", "loadWorld",
                        "(Lworldline/api/WorldSource;)V", "", "LIFECYCLE",
                        "LIFECYCLE,FILESYSTEM", "controlled-client-tick,m3-domain-api",
                        "", 9998),
                SemanticMapping.of("lifecycle", "MANUAL_TICK",
                        "worldline/api/MinecraftRuntime", "method", "tick", "()V",
                        "CLOCK", "WORLD,PLAYER,GUI", "LIFECYCLE,CLOCK",
                        "controlled-client-tick,m3-domain-api", "", 9998),
                SemanticMapping.of("lifecycle", "CLOSE",
                        "worldline/api/MinecraftRuntime", "method", "close", "()V",
                        "", "LIFECYCLE", "LIFECYCLE", "controlled-client-tick",
                        "", 9998)));
    }
}
