package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Display, render-engine, entity-renderer, world-render, and client fields.
 * EFFECT_UPDATE observes rendererTick rather than sharing updateEffects.
 */
final class RenderSemantics {
    private RenderSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("render", "DISPLAY", "org/lwjgl/opengl/Display", "class",
                        "Display", "-", "", "RENDER", "RENDER",
                        "controlled-client-tick,lab-cycle", "", 9998),
                SemanticMapping.of("render", "DISPLAY_CREATED", "org/lwjgl/opengl/Display",
                        "method", "isCreated", "()Z", "RENDER", "", "RENDER",
                        "controlled-client-tick,lab-cycle", "", 9998),
                SemanticMapping.of("render", "RENDER_ENGINE", "net/minecraft/src/RenderEngine",
                        "class", "RenderEngine", "-", "RESOURCE", "RENDER", "RENDER,RESOURCE",
                        "controlled-client-tick,symbols.map", "", 9920),
                SemanticMapping.of("render", "RENDER_ENGINE_FIELD",
                        "net/minecraft/client/Minecraft", "field", "renderEngine",
                        "Lnet/minecraft/src/RenderEngine;", "RENDER", "RENDER", "RENDER",
                        "controlled-client-tick,lab-cycle", "p", 9920),
                SemanticMapping.of("render", "ENTITY_RENDERER",
                        "net/minecraft/src/EntityRenderer", "class", "EntityRenderer", "-",
                        "WORLD,PLAYER", "RENDER", "RENDER",
                        "controlled-client-tick,symbols.map", "", 9920),
                SemanticMapping.of("render", "ENTITY_RENDERER_FIELD",
                        "net/minecraft/client/Minecraft", "field", "entityRenderer",
                        "Lnet/minecraft/src/EntityRenderer;", "RENDER", "RENDER", "RENDER",
                        "controlled-client-tick,lab-cycle", "t", 9920),
                SemanticMapping.of("render", "RENDERER_UPDATE",
                        "net/minecraft/src/EntityRenderer", "method", "updateRenderer", "()V",
                        "WORLD,PLAYER", "RENDER", "RENDER",
                        "controlled-client-tick,symbols.map", "a", 9920),
                SemanticMapping.of("render", "RENDERER_COUNTER",
                        "net/minecraft/src/EntityRenderer", "field", "rendererUpdateCount", "I",
                        "RENDER", "RENDER", "RENDER",
                        "lab-cycle,controlled-client-tick", "", 9920),
                SemanticMapping.of("render", "MOUSE_OVER", "net/minecraft/src/EntityRenderer",
                        "method", "getMouseOver", "(F)V", "PLAYER,WORLD", "RENDER", "RENDER",
                        "controlled-client-tick,symbols.map", "a", 9850),
                SemanticMapping.of("render", "RENDER_GLOBAL", "net/minecraft/src/RenderGlobal",
                        "class", "RenderGlobal", "-", "WORLD", "RENDER", "RENDER",
                        "controlled-client-tick,symbols.map", "", 9920),
                SemanticMapping.of("render", "RENDER_GLOBAL_FIELD",
                        "net/minecraft/client/Minecraft", "field", "renderGlobal",
                        "Lnet/minecraft/src/RenderGlobal;", "RENDER", "RENDER", "RENDER",
                        "controlled-client-tick,lab-cycle", "g", 9920),
                SemanticMapping.of("render", "CLOUD_UPDATE", "net/minecraft/src/RenderGlobal",
                        "method", "updateClouds", "()V", "WORLD", "RENDER", "RENDER",
                        "controlled-client-tick,symbols.map", "d", 9850),
                SemanticMapping.of("render", "CLOUD_OFFSET", "net/minecraft/src/RenderGlobal",
                        "field", "cloudOffsetX", "I", "RENDER", "RENDER", "RENDER",
                        "lab-cycle,controlled-client-tick", "", 9920),
                SemanticMapping.of("render", "EFFECT_RENDERER",
                        "net/minecraft/src/EffectRenderer", "class", "EffectRenderer", "-",
                        "RENDER", "RENDER", "RENDER",
                        "controlled-client-tick,symbols.map", "", 9850),
                SemanticMapping.of("render", "EFFECT_UPDATE",
                        "worldline/b173/B173Observation", "method", "rendererTick", "()I",
                        "RENDER", "", "RENDER", "lab-cycle", "", 9920),
                SemanticMapping.of("render", "HUD_FIELD", "net/minecraft/client/Minecraft",
                        "field", "ingameGUI", "Lnet/minecraft/src/GuiIngame;", "GUI", "GUI",
                        "RENDER,GUI", "controlled-client-tick,lab-cycle", "v", 9920),
                SemanticMapping.of("render", "PLAYER_CONTROLLER_FIELD",
                        "net/minecraft/client/Minecraft", "field", "playerController",
                        "Lnet/minecraft/src/PlayerController;", "PLAYER", "PLAYER", "TICK",
                        "controlled-client-tick,lab-cycle,gui-tree", "c", 9920)));
    }
}
