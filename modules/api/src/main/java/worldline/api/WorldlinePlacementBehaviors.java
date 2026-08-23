package worldline.api;

import java.util.Map;

/** Function-specific placement identities that replace one over-broad Atlas concentration. */
public final class WorldlinePlacementBehaviors {
    public static final WorldlineBehavior VEGETATION = WorldlineBehavior.define(
            "vegetation-placement", WorldlineFamily.ENVIRONMENT,
            "Plants and foliage persist only on their valid placement support");
    public static final WorldlineBehavior COMPONENT = WorldlineBehavior.define(
            "component-placement", WorldlineFamily.WORLD,
            "Attached and rail-like components persist with exact support geometry");
    public static final WorldlineBehavior ORIENTED = WorldlineBehavior.define(
            "oriented-block-placement", WorldlineFamily.WORLD,
            "Facing-sensitive blocks persist with server-authored orientation metadata");
    public static final WorldlineBehavior UTILITY = WorldlineBehavior.define(
            "utility-block-placement", WorldlineFamily.WORLD,
            "Interactive utility blocks persist across a fresh login");
    public static final WorldlineBehavior RESOURCE = WorldlineBehavior.define(
            "resource-block-placement", WorldlineFamily.WORLD,
            "Ore and storage material blocks persist across a fresh login");
    public static final WorldlineBehavior DECORATIVE = WorldlineBehavior.define(
            "decorative-block-placement", WorldlineFamily.WORLD,
            "Decorative full and partial blocks persist with exact metadata");

    private WorldlinePlacementBehaviors() {}

    static Map<String, WorldlineBehavior> freeze() {
        if (VEGETATION == null || COMPONENT == null || ORIENTED == null || UTILITY == null
                || RESOURCE == null || DECORATIVE == null) throw new IllegalStateException("placement catalog");
        return WorldlineBehaviorRegistry.freeze();
    }
}
