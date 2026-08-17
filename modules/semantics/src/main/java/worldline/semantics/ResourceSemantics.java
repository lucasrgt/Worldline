package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Texture lookup and dynamic-texture update symbols on RenderEngine.
 */
final class ResourceSemantics {
    private ResourceSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("resource", "TEXTURE_LOOKUP",
                        "net/minecraft/src/RenderEngine", "method", "getTexture",
                        "(Ljava/lang/String;)I", "RESOURCE", "RENDER", "RESOURCE",
                        "controlled-client-tick,symbols.map", "b", 9920),
                SemanticMapping.of("resource", "DYNAMIC_TEXTURE",
                        "net/minecraft/src/RenderEngine", "method", "updateDynamicTextures",
                        "()V", "RESOURCE", "RENDER", "RESOURCE",
                        "controlled-client-tick,symbols.map", "a", 9850)));
    }
}
