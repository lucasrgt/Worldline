package worldline.b173server;

import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Paired native ticks around cooldown, despawn and periodic support-check boundaries. */
final class B173NativeEntityTimedState {
    private B173NativeEntityTimedState() { }
    static void observe(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        if (scene.rule() == NativeEntityDynamicsCase.Rule.COOLDOWN) cooldown(names, scene, out);
        else expiry(names, scene, out);
    }
    private static void cooldown(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        String field = scene.legacyId() == 40 ? "field_9167_b" : scene.legacyId() == 41 ? "field_9177_b" : "hurtTime";
        for (boolean active : new boolean[] {false, true}) {
            B173NativeEntityScene f = new B173NativeEntityScene(names, scene);
            f.fill(4, 12, 90, 90, 4, 12, 1);
            names.set(f.entity, field, active ? 2 : 0);
            String prefix = active ? "active" : "control";
            out.put(prefix + "-before", (long) f.number(field));
            f.tick();
            if (active) out.put("active-one", (long) f.number(field));
            f.tick();
            out.put(prefix + "-two", (long) f.number(field));
            B173NativeEntityScene.require(f.flag("isDead") == 0, "cooldown subject unexpectedly removed");
        }
    }
    private static void expiry(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        int boundary = scene.rule() == NativeEntityDynamicsCase.Rule.ITEM_EXPIRY ? 6000
                : scene.rule() == NativeEntityDynamicsCase.Rule.PROJECTILE_EXPIRY ? 1200 : 101;
        String field = scene.legacyId() == 1 ? "age" : scene.legacyId() == 9 ? "field_452_ad"
                : scene.legacyId() == 10 ? "ticksInGround" : "ticksOnGround";
        for (boolean active : new boolean[] {false, true}) {
            B173NativeEntityScene f = new B173NativeEntityScene(names, scene);
            if (scene.rule() == NativeEntityDynamicsCase.Rule.PROJECTILE_EXPIRY) ground(f);
            names.set(f.entity, field, boundary - (active ? 1 : 2));
            String prefix = active ? "active" : "control";
            out.put(prefix + "-before", (long) f.number(field));
            f.tick();
            out.put(prefix + "-after", (long) f.number(field));
            out.put(prefix + "-dead", f.flag("isDead"));
        }
    }
    private static void ground(B173NativeEntityScene f) throws Exception {
        boolean arrow = f.scene.legacyId() == 10;
        f.block(8, 72, 8, 1);
        f.position(8.5, 72.5, 8.5);
        f.names.set(f.entity, arrow ? "xTile" : "xTileSnowball", 8);
        f.names.set(f.entity, arrow ? "yTile" : "yTileSnowball", 72);
        f.names.set(f.entity, arrow ? "zTile" : "zTileSnowball", 8);
        f.names.set(f.entity, arrow ? "inTile" : "inTileSnowball", 1);
        f.names.set(f.entity, arrow ? "inGround" : "inGroundSnowball", true);
    }
}
