package worldline.api;

/** Qualified item boundaries kept outside the primary compatibility catalog. */
public final class WorldlineItemBehaviors {
    public static final WorldlineBehavior ITEM_STACK_MERGE = define("item-stack-merge",
            "Identical dropped items in contact remain two Packet21 stacks");
    public static final WorldlineBehavior PAINTING_SUPPORT_BREAK = define("painting-support-break",
            "Painting support-block break Packet29 destroy and Packet21 drop");
    public static final WorldlineBehavior BOW_SHOT_DURABILITY = define("bow-shot-durability",
            "Bow air-use remaining held-stack damage");
    public static final WorldlineBehavior TOOL_SHATTER = define("tool-shatter",
            "Last remaining durability on a valid block break destroys the held tool");
    public static final WorldlineBehavior FURNACE_SMELT_INTERRUPT = define("furnace-smelt-interrupt",
            "Removing furnace input or fuel mid-smelt prevents a completed output");
    public static final WorldlineBehavior CHEST_BREAK_SPILL = define("chest-break-spill",
            "Breaking a loaded chest spills contents as Packet21");
    public static final WorldlineBehavior CHEST_ACCESS_CONSTRAINTS = define(
            "chest-access-constraints",
            "A solid lid blocks chest access and a third chest cannot join an existing pair");
    public static final WorldlineBehavior REDSTONE_ORE_DUST_DROP = define(
            "redstone-ore-dust-drop",
            "A qualifying pick removes redstone ore and emits redstone dust within the vanilla drop bound");
    public static final WorldlineBehavior SHEARS_LEAF_DURABILITY = define(
            "shears-leaf-durability",
            "Harvesting one oak-leaf block with pristine shears drops the leaf stack "
                    + "and consumes exactly one durability point");

    private WorldlineItemBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.ITEM, subject);
    }
}
