package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Client tick root, tick counter, world tick, entity-update, controller, and
 * effect-tick symbols for b1.7.3.
 */
final class TickSemantics {
    private TickSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("tick", "CLIENT_TICK_ROOT", "net/minecraft/client/Minecraft",
                        "method", "runTick", "()V", "INPUT,CLOCK", "WORLD,PLAYER,GUI",
                        "CLOCK,INPUT", "controlled-client-tick,symbols.map", "k", 9998),
                SemanticMapping.of("tick", "CLIENT_TICK_COUNTER", "net/minecraft/client/Minecraft",
                        "field", "ticksRan", "I", "CLOCK", "CLOCK", "CLOCK",
                        "controlled-client-tick,symbols.map", "V", 9998),
                SemanticMapping.of("tick", "WORLD_TICK", "net/minecraft/src/World", "method",
                        "tick", "()V", "WORLD,RNG", "WORLD,ENTITY", "CLOCK,RNG",
                        "deterministic-world-tick,symbols.map", "l", 9998),
                SemanticMapping.of("tick", "ENTITY_UPDATE", "net/minecraft/src/World", "method",
                        "updateEntities", "()V", "WORLD", "ENTITY", "CLOCK",
                        "controlled-client-tick,symbols.map", "g", 9990),
                SemanticMapping.of("tick", "CONTROLLER_TYPE", "net/minecraft/src/PlayerController",
                        "class", "PlayerController", "-", "PLAYER", "", "TICK",
                        "controlled-client-tick,symbols.map", "", 9998),
                SemanticMapping.of("tick", "CONTROLLER_TICK", "net/minecraft/src/PlayerController",
                        "method", "updateController", "()V", "PLAYER", "PLAYER", "TICK",
                        "controlled-client-tick,symbols.map", "c", 9990),
                SemanticMapping.of("tick", "EFFECT_TICK", "net/minecraft/src/EffectRenderer",
                        "method", "updateEffects", "()V", "RENDER", "RENDER", "TICK,RENDER",
                        "controlled-client-tick,symbols.map", "a", 9850)));
    }
}
