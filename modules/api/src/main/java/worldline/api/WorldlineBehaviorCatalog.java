package worldline.api;

import java.util.Map;

/** Initializes every cohesive behavior family before freezing public lookup. */
final class WorldlineBehaviorCatalog {
    private WorldlineBehaviorCatalog() {}

    static Map<String, WorldlineBehavior> freeze() {
        if (WorldlinePlacementBehaviors.DECORATIVE == null
                || WorldlineRenderBehaviors.SPECIAL_WORLD_BLOCKS == null
                || WorldlineRedstoneBehaviors.REPEATER_DIODE == null
                || WorldlineEnvironmentBehaviors.MUSHROOM_SPREAD == null
                || WorldlineWorldBehaviors.WHEAT_LIGHT_HALT == null
                || WorldlineVehicleBehaviors.BOAT_CURRENT_PUSH == null
                || WorldlineItemBehaviors.ITEM_STACK_MERGE == null
                || WorldlinePlayerBehaviors.WOLF_SIT == null
                || WorldlineHostileBehaviors.GHAST_FIREBALL_PUNCH == null)
            throw new IllegalStateException("behavior catalog initialization");
        return WorldlineBehaviorRegistry.freeze();
    }
}
