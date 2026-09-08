package worldline.b173server;

import java.util.List;
import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Native attack cooldown, ignition, ghast firing and projectile-contact contrasts. */
final class B173NativeEntityCombat {
    private B173NativeEntityCombat() { }
    static void observe(B173NativeServerSymbols n, B173NativePlayerFactory players,
            NativeEntityDynamicsCase c, Map<String, Long> out) throws Exception {
        for (int active = 0; active < 2; active++) {
            B173NativeEntityScene s = new B173NativeEntityScene(n, c);
            Object target = players.create(s.world);
            String p = active == 0 ? "control-" : "active-";
            s.position(8.5, 65 + s.number("yOffset"), 8.5);
            s.fill(0, 16, 100, 100, 0, 16, 1);
            double offset = ((Number) n.getAs(target, "Entity", "yOffset")).doubleValue();
            n.call(target, "Entity", "setPosition", "(DDD)V", 9D, 65D + offset, 8.5D);
            out.put(p + "health-before", ((Number) n.getAs(target, "EntityLiving", "health")).longValue());
            if (c.legacyId() == 50) {
                n.call(target, "Entity", "setPosition", "(DDD)V", active == 1 ? 9D : 15D, 65D + offset, 8.5D);
                n.call(s.entity, "EntityCreature", "attackEntity", "(Lnet/minecraft/src/Entity;F)V", target,
                        n.call(s.entity, "Entity", "getDistanceToEntity", "(Lnet/minecraft/src/Entity;)F", target));
                out.put(p + "state", ((Number) n.call(s.entity, "EntityCreeper",
                        "getCreeperState", "()I")).longValue());
            } else if (c.legacyId() == 56) {
                n.set(s.entity, "targetedEntity", target); n.set(s.entity, "aggroCooldown", 20);
                n.set(s.entity, "courseChangeCooldown", 2);
                n.set(s.entity, "waypointX", 8.5D); n.set(s.entity, "waypointY", s.number("posY"));
                n.set(s.entity, "waypointZ", 8.5D); n.set(s.entity, "attackCounter", active == 1 ? 19 : 18);
                n.call(s.entity, "EntityLiving", "updatePlayerActionState", "()V");
                out.put(p + "counter", (long) s.number("attackCounter"));
                long fireballs = 0;
                for (Object entity : (List<?>) n.get(s.world, "loadedEntityList")) {
                    if (n.type("EntityFireball").isInstance(entity)) fireballs++;
                }
                out.put(p + "fireballs", fireballs);
            } else {
                n.set(s.entity, "attackTime", active == 1 ? 0 : 2);
                n.call(s.entity, "EntityCreature", "attackEntity", "(Lnet/minecraft/src/Entity;F)V", target,
                        n.call(s.entity, "Entity", "getDistanceToEntity", "(Lnet/minecraft/src/Entity;)F", target));
                out.put(p + "cooldown", (long) s.number("attackTime"));
            }
            out.put(p + "health-after", ((Number) n.getAs(target, "EntityLiving", "health")).longValue());
        }
    }
}
