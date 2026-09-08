package worldline.b173server;

import java.util.Map;

/** World visibility, light and actual player damage select native AI branches. */
final class B173EntityTargetContext {
    private B173EntityTargetContext() { }
    static void observe(B173NativeServerSymbols n, B173NativePlayerFactory players,
            Map<String, Long> out) throws Exception {
        for (int id : new int[] {54, 52, 57}) for (int active = 0; active < 2; active++) {
            String type = id == 54 ? "EntityZombie" : id == 52 ? "EntitySpider" : "EntityPigZombie";
            B173NativeEntityScene s = B173EntityStateCauses.scene(n, id, type);
            Object player = players.create(s.world);
            n.call(player, "Entity", "setPosition", "(DDD)V", 12D, 72D, 8.5D);
            n.call(s.world, "World", "entityJoinedWorld", "(Lnet/minecraft/src/Entity;)Z", player);
            if (id == 54 && active == 0) s.fill(10, 10, 70, 76, 5, 11, 1);
            if (id == 52) {
                Object provider = n.get(s.world, "worldProvider");
                java.util.Arrays.fill((float[]) n.get(provider, "lightBrightnessTable"), active == 1 ? 0F : 1F);
            }
            if (id == 57 && active == 1) n.call(s.entity, "Entity", "attackEntityFrom",
                    "(Lnet/minecraft/src/Entity;I)Z", player, 1);
            Object target = n.call(s.entity, "EntityCreature",
                    "findPlayerToAttack", "()Lnet/minecraft/src/Entity;");
            B173NativeEntityScene.require(target == null || target == player, "unexpected AI target");
            String key = "target-" + id + '-' + active;
            out.put(key, target == player ? 1L : 0L);
            if (id == 57) out.put(key + "-anger", (long) s.number("angerLevel"));
        }
    }
}
