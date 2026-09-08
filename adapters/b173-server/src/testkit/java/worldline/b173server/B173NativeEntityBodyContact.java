package worldline.b173server;

import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Actual native body collision and snowball ray/entity contact; no assigned result states. */
final class B173NativeEntityBodyContact {
    private B173NativeEntityBodyContact() { }
    static void observe(B173NativeServerSymbols n, NativeEntityDynamicsCase c, Map<String, Long> out)
            throws Exception {
        for (int active = 0; active < 2; active++) {
            B173NativeEntityScene s = new B173NativeEntityScene(n, c);
            Object target = n.type("EntityCow").getConstructor(n.type("World")).newInstance(s.world);
            String p = active == 0 ? "control-" : "active-";
            if (c.legacyId() == 11) {
                n.call(target, "Entity", "setPosition", "(DDD)V", 9.5D, 72D, 8.5D);
                if (active == 1) n.call(s.world, "World", "entityJoinedWorld", "(Lnet/minecraft/src/Entity;)Z", target);
                n.set(s.entity, "motionX", 1D);
                out.put(p + "health-before", ((Number) n.get(target, "health")).longValue());
                s.tick();
                out.put(p + "health-after", ((Number) n.get(target, "health")).longValue());
                out.put(p + "dead", s.flag("isDead"));
            } else {
                n.call(target, "Entity", "setPosition", "(DDD)V", active == 1 ? 8.9D : 8.5D, 72D, 8.5D);
                out.put(p + "pushable", (Boolean) n.call(s.entity, "Entity", "canBePushed", "()Z") ? 1L : 0L);
                n.call(s.entity, "Entity", "applyEntityCollision", "(Lnet/minecraft/src/Entity;)V", target);
                out.put(p + "motion", s.milli("motionX"));
                out.put(p + "target-motion", Math.round(((Number) n.get(target, "motionX")).doubleValue() * 1000));
            }
        }
    }
}
