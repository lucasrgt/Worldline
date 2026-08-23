import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reviewed function-specific replacement for the broad placement behavior concentration. */
final class BehaviorFamilyAssignments {
    private BehaviorFamilyAssignments() { }

    static Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        group(values, "vegetation-placement",
                "m167-cactus,m198-dandelion,m199-rose,m200-brown-mushroom,m201-red-mushroom,"
                + "m202-sapling,m209-leaves,m289-spruce-sapling,m290-birch-sapling,"
                + "m291-spruce-leaves,m292-birch-leaves,m383-mushroom-place-set");
        group(values, "component-placement",
                "m174-ladder,m175-torch,m182-redstone-torch,m183-rails,m184-powered-rail,"
                + "m185-detector-rail,m242-lever-place,m243-redstone-wire,m399-wooden-button-set,"
                + "m400-remaining-torch-faces,m401-remaining-redstone-wire,"
                + "m429-remaining-attach-faces,m432-remaining-rail-geometry-set");
        group(values, "oriented-block-placement",
                "m171-pumpkin,m186-oak-stairs,m187-cobble-stairs,m231-dispenser-place,"
                + "m240-bed-place,m241-iron-door-place,m293-sticky-piston-place,m294-piston-place,"
                + "m393-stair-facing-set,m425-remaining-machine-faces,m427-remaining-piston-orient-set,"
                + "m428-remaining-door-orient-set,m431-remaining-bed-orient-set");
        group(values, "utility-block-placement",
                "m219-tnt-place,m220-workbench,m221-furnace,m232-chest-place,m233-note-block-place,"
                + "m244-cake-place,m433-remaining-chest-orient-set");
        group(values, "resource-block-placement",
                "m212-gold-block,m213-iron-block,m214-diamond-block,m215-lapis-block,m216-obsidian,"
                + "m217-mossy-cobble,m224-netherrack,m225-coal-ore,m226-iron-ore,m227-gold-ore,"
                + "m228-diamond-ore,m229-redstone-ore,m230-lapis-ore,"
                + "m419-remaining-netherrack-place,m439-remaining-ore-place-set");
        group(values, "decorative-block-placement",
                "m173-fence,m189-bookshelf,m190-jack-o-lantern,m191-glowstone,m192-soul-sand,"
                + "m193-ice,m194-snow-block,m195-cobweb,m196-glass,m197-wool,m203-snow-layer,"
                + "m204-clay,m205-brick,m206-sponge,m207-sandstone,m234-sandstone-slab,"
                + "m248-orange-wool,m249-yellow-wool,m250-red-wool,m251-black-wool,m252-blue-wool,"
                + "m253-green-wool,m280-magenta-wool,m281-light-blue-wool,m282-lime-wool,"
                + "m283-pink-wool,m284-gray-wool,m285-light-gray-wool,m286-cyan-wool,"
                + "m287-purple-wool,m288-brown-wool,m387-remaining-light-set,"
                + "m416-remaining-bookshelf-place,m434-remaining-sponge-glass-ice");
        group(values, "block-placement-persistence",
                "m188-stone-slab,m208-oak-log,m210-oak-planks,m211-double-slab,m218-gravel,"
                + "m222-cobble,m223-dirt,m235-wood-slab,m236-cobble-slab,m237-stone,m238-grass,"
                + "m239-sand,m246-spruce-log,m247-birch-log,m394-remaining-slab-place");
        require(values.size() == 109, "placement assignment census drift");
        return Collections.unmodifiableMap(values);
    }

    private static void group(Map<String, String> values, String token, String ids) {
        for (String id : ids.split(",")) require(values.put(id, token) == null,
                "duplicate placement assignment " + id);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
