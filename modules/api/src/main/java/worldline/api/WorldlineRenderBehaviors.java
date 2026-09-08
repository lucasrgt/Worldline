package worldline.api;

/** Public native client rendering behavior identities. */
public final class WorldlineRenderBehaviors {
    public static final WorldlineBehavior SPECIAL_WORLD_BLOCKS = WorldlineBehavior.define(
            "native-special-world-render", WorldlineFamily.WORLD,
            "Official client textured world rendering through all special RenderBlocks routes");
    public static final WorldlineBehavior TILE_ENTITIES = WorldlineBehavior.define(
            "native-tile-entity-render", WorldlineFamily.WORLD,
            "Official client rendering for sign and moving-piston tile entities");
    public static final WorldlineBehavior ENTITIES = WorldlineBehavior.define(
            "native-entity-render", WorldlineFamily.WORLD,
            "Official client body rendering for every concrete registry entity in a canonical scene");

    private WorldlineRenderBehaviors() { }
}
