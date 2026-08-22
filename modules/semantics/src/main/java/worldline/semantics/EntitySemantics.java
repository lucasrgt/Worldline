package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Entity type, identity, position, death flag, and location methods for
 * b1.7.3.
 */
final class EntitySemantics {
    private EntitySemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("entity", "ENTITY_TYPE", "net/minecraft/src/Entity", "class",
                        "Entity", "-", "", "", "ENTITY",
                        "controlled-client-tick,symbols.map,m3-domain-api", "", 9998),
                SemanticMapping.of("entity", "ENTITY_ID", "net/minecraft/src/Entity", "field",
                        "entityId", "I", "ENTITY", "", "ENTITY",
                        "controlled-client-tick,symbols.map,m3-domain-api", "aD", 9998),
                SemanticMapping.of("entity", "ENTITY_POS_X", "net/minecraft/src/Entity", "field",
                        "posX", "D", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map,m3-domain-api", "aM", 9998),
                SemanticMapping.of("entity", "ENTITY_POS_Y", "net/minecraft/src/Entity", "field",
                        "posY", "D", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map,m3-domain-api", "aN", 9998),
                SemanticMapping.of("entity", "ENTITY_POS_Z", "net/minecraft/src/Entity", "field",
                        "posZ", "D", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map,m3-domain-api", "aO", 9998),
                SemanticMapping.of("entity", "ENTITY_YAW", "net/minecraft/src/Entity", "field",
                        "rotationYaw", "F", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "aS", 9990),
                SemanticMapping.of("entity", "ENTITY_MOTION_X", "net/minecraft/src/Entity", "field",
                        "motionX", "D", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "aP", 9990),
                SemanticMapping.of("entity", "ENTITY_MOTION_Y", "net/minecraft/src/Entity", "field",
                        "motionY", "D", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "aQ", 9990),
                SemanticMapping.of("entity", "ENTITY_MOTION_Z", "net/minecraft/src/Entity", "field",
                        "motionZ", "D", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "aR", 9990),
                SemanticMapping.of("entity", "ENTITY_ON_GROUND", "net/minecraft/src/Entity", "field",
                        "onGround", "Z", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "aX", 9990),
                SemanticMapping.of("entity", "ENTITY_HORIZONTAL_COLLISION", "net/minecraft/src/Entity",
                        "field", "isCollidedHorizontally", "Z", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "aY", 9990),
                SemanticMapping.of("entity", "ENTITY_IN_WEB", "net/minecraft/src/Entity", "field",
                        "isInWeb", "Z", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "bc", 9990),
                SemanticMapping.of("entity", "ENTITY_FALL_DISTANCE", "net/minecraft/src/Entity", "field",
                        "fallDistance", "F", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "bk", 9990),
                SemanticMapping.of("entity", "ENTITY_DEAD", "net/minecraft/src/Entity", "field",
                        "isDead", "Z", "ENTITY", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map,m3-domain-api", "be", 9998),
                SemanticMapping.of("entity", "ENTITY_SET_POSITION", "net/minecraft/src/Entity",
                        "method", "setPosition", "(DDD)V", "", "ENTITY", "ENTITY",
                        "m3-domain-api,symbols.map", "e", 9998),
                SemanticMapping.of("entity", "ENTITY_SET_LOCATION", "net/minecraft/src/Entity",
                        "method", "setLocationAndAngles", "(DDDFF)V", "", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "c", 9990),
                SemanticMapping.of("entity", "LIVING_MOVE_HEADING", "net/minecraft/src/EntityLiving",
                        "method", "moveEntityWithHeading", "(FF)V", "", "ENTITY", "ENTITY",
                        "controlled-client-tick,symbols.map", "a_", 9990),
                SemanticMapping.of("entity", "ENTITY_ITEM", "net/minecraft/src/EntityItem",
                        "class", "EntityItem", "-", "", "", "ENTITY",
                        "invariants,lab-cycle", "", 9920)));
    }
}
