package worldline.b173server;

import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Damage is delivered by native virtual dispatch; health and delayed removal are read back. */
final class B173NativeEntityDamage {
    private B173NativeEntityDamage() { }
    static void observe(B173NativeServerSymbols n, B173NativePlayerFactory players,
            NativeEntityDynamicsCase c, Map<String, Long> out) throws Exception {
        for (int dose : new int[] {0, 1, 1024}) {
            B173NativeEntityScene s = new B173NativeEntityScene(n, c);
            s.position(8.5, 65 + s.number("yOffset"), 8.5);
            s.fill(0, 16, 64, 64, 0, 16, 1);
            s.fill(0, 16, 100, 100, 0, 16, 1);
            String prefix = dose == 0 ? "control-" : dose == 1 ? "low-" : "lethal-";
            boolean health = c.legacyId() == 1 || n.type("EntityLiving").isInstance(s.entity);
            out.put(prefix + "before", health ? (long) s.number("health") : -1L);
            boolean accepted = dose != 0 && (Boolean) n.call(s.entity, "Entity", "attackEntityFrom",
                    "(Lnet/minecraft/src/Entity;I)Z", players.create(s.world), dose);
            out.put(prefix + "accepted", accepted ? 1L : 0L);
            out.put(prefix + "after", health ? (long) s.number("health") : -1L);
            out.put(prefix + "immediate-dead", s.flag("isDead"));
            if (n.type("EntityLiving").isInstance(s.entity)) for (int tick = 0; tick < 21; tick++) s.tick();
            out.put(prefix + "dead", s.flag("isDead"));
        }
    }
}
