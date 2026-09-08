package worldline.b173server;

import java.util.List;
import java.util.Map;
import java.util.Random;

/** Death dispatch retains contextual item identity and metadata under matched random seeds. */
final class B173EntityContextDrops {
    private B173EntityContextDrops() { }
    static void observe(B173NativeServerSymbols n, B173NativePlayerFactory players,
            Map<String, Long> out) throws Exception {
        for (int id : new int[] {90, 91}) for (int seed = 0; seed < 8; seed++) {
            for (int active = 0; active < 2; active++) {
                B173NativeEntityScene s = B173EntityStateCauses.scene(n, id,
                        id == 90 ? "EntityPig" : "EntitySheep");
                ((Random) n.get(s.entity, "rand")).setSeed(seed);
                if (id == 90) n.set(s.entity, "fire", active == 1 ? 40 : 0);
                else {
                    n.call(s.entity, "EntitySheep", "setFleeceColor", "(I)V", 5);
                    n.call(s.entity, "EntitySheep", "setSheared", "(Z)V", active == 1);
                }
                n.call(s.entity, "Entity", "attackEntityFrom", "(Lnet/minecraft/src/Entity;I)Z",
                        players.create(s.world), 1024);
                String key = "drops-" + id + '-' + seed + '-' + active;
                long count = 0, itemId = 0, damage = 0;
                for (Object entity : (List<?>) n.get(s.world, "loadedEntityList")) {
                    B173NativeEntityScene.require(n.type("EntityItem").isInstance(entity), "non-item death yield");
                    Object stack = n.get(entity, "item");
                    long current = ((Number) n.get(stack, "itemID")).longValue();
                    B173NativeEntityScene.require(itemId == 0 || itemId == current, "mixed death yield");
                    itemId = current; count += ((Number) n.get(stack, "stackSize")).longValue();
                    damage = ((Number) n.call(stack, "ItemStack", "getItemDamage", "()I")).longValue();
                }
                out.put(key + "-count", count); out.put(key + "-id", itemId);
                out.put(key + "-damage", damage); out.put(key + "-health", (long) s.number("health"));
            }
        }
    }
}
