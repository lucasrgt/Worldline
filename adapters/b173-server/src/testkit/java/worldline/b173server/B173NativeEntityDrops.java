package worldline.b173server;

import java.util.List;
import java.util.Map;
import java.util.Random;
import worldline.testapi.NativeEntityDynamicsCase;

/** Eight seeded native death contrasts retain exact item IDs, counts and damage values. */
final class B173NativeEntityDrops {
    private B173NativeEntityDrops() { }
    static void observe(B173NativeServerSymbols n, B173NativePlayerFactory players,
            NativeEntityDynamicsCase c, Map<String, Long> out) throws Exception {
        for (int seed = 0; seed < 8; seed++) for (int hit = 0; hit < 2; hit++) {
            B173NativeEntityScene s = new B173NativeEntityScene(n, c);
            ((Random) n.get(s.entity, "rand")).setSeed(seed);
            String prefix = "seed" + seed + (hit == 0 ? "-control-" : "-death-");
            if (hit == 1) n.call(s.entity, "Entity", "attackEntityFrom",
                    "(Lnet/minecraft/src/Entity;I)Z", players.create(s.world), 1024);
            long items = 0, count = 0, other = 0;
            List<?> loaded = (List<?>) n.get(s.world, "loadedEntityList");
            for (Object entity : loaded) {
                if (!n.type("EntityItem").isInstance(entity)) { other++; continue; }
                Object stack = n.get(entity, "item");
                long id = ((Number) n.get(stack, "itemID")).longValue();
                long damage = ((Number) n.call(stack, "ItemStack", "getItemDamage", "()I")).longValue();
                long size = ((Number) n.get(stack, "stackSize")).longValue();
                String key = prefix + "item-" + id + '-' + damage;
                out.put(key, out.containsKey(key) ? out.get(key) + size : size);
                items++; count += size;
            }
            out.put(prefix + "entities", items); out.put(prefix + "count", count);
            out.put(prefix + "other", other);
            out.put(prefix + "health", n.type("EntityLiving").isInstance(s.entity)
                    ? (long) s.number("health") : -1L);
            out.put(prefix + "dead", s.flag("isDead"));
        }
    }
}
