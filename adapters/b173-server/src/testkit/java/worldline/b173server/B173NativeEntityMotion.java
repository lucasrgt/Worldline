package worldline.b173server;

import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Native displacement and world-condition contrasts for concrete entity instances. */
final class B173NativeEntityMotion {
    private B173NativeEntityMotion() { }
    static void observe(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        if (scene.rule() == NativeEntityDynamicsCase.Rule.MOTION) motion(names, scene, out);
        else if (scene.rule() == NativeEntityDynamicsCase.Rule.HANGING_MOTION) hanging(names, scene, out);
        else if (scene.rule() == NativeEntityDynamicsCase.Rule.IMMERSION
                || scene.rule() == NativeEntityDynamicsCase.Rule.SHORT_IMMERSION) immersion(names, scene, out);
        else if (scene.rule() == NativeEntityDynamicsCase.Rule.RAIL_DRAG) rails(names, scene, out);
        else support(names, scene, out);
    }
    private static void hanging(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        for (boolean moved : new boolean[] {false, true}) {
            B173NativeEntityScene f = new B173NativeEntityScene(names, scene);
            long before = f.milli("posX");
            f.move(moved ? 1D : 0D, 0D, 0D);
            String prefix = moved ? "moved" : "control";
            out.put(prefix + "-dead", f.flag("isDead"));
            out.put(prefix + "-delta", f.milli("posX") - before);
            out.put(prefix + "-drops", (long) ((java.util.List<?>) names.get(f.world, "loadedEntityList")).size());
        }
    }
    private static void motion(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        for (boolean wall : new boolean[] {false, true}) {
            B173NativeEntityScene f = new B173NativeEntityScene(names, scene);
            if (wall) f.fill(12, 12, 65, 100, 0, 16, 1);
            names.set(f.entity, "motionX", 1D);
            long before = f.milli("posX");
            f.move(5D, 0D, 0D);
            out.put(wall ? "blocked-delta" : "open-delta", f.milli("posX") - before);
            if (wall) {
                out.put("collision", f.flag("isCollidedHorizontally"));
                out.put("blocked-motion", f.milli("motionX"));
            }
        }
    }
    private static void immersion(B173NativeServerSymbols names, NativeEntityDynamicsCase scene,
            Map<String, Long> out) throws Exception {
        for (boolean wet : new boolean[] {false, true}) {
            B173NativeEntityScene f = new B173NativeEntityScene(names, scene);
            if (wet) f.fill(4, 12, 68, 88, 4, 12, 9);
            String prefix = wet ? "wet" : "dry";
            out.put(prefix + "-before", f.flag("inWater"));
            out.put(prefix + "-inside", Boolean.TRUE.equals(names.call(f.entity, "Entity", "isInsideOfMaterial",
                    "(Lnet/minecraft/src/Material;)Z", names.constant("Material", "water"))) ? 1L : 0L);
            f.tick();
            out.put(prefix + "-after", f.flag("inWater"));
        }
    }
    private static void support(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        for (boolean floor : new boolean[] {false, true}) {
            B173NativeEntityScene f = new B173NativeEntityScene(names, scene);
            if (!floor) f.fill(7, 9, 62, 64, 7, 9, 0);
            f.position(8.5, 65 + f.number("yOffset"), 8.5);
            names.set(f.entity, "motionY", -0.2D);
            f.tick();
            String prefix = floor ? "floor" : "air";
            if (scene.rule() == NativeEntityDynamicsCase.Rule.SUPPORT_ARREST) {
                out.put(prefix + "-ground", f.flag("onGround"));
                out.put(prefix + "-vertical", f.milli("motionY"));
            } else {
                out.put(prefix + "-dead", f.flag("isDead"));
                out.put(prefix + "-block", (long) f.block(8, 65, 8));
            }
        }
    }
    private static void rails(B173NativeServerSymbols names, NativeEntityDynamicsCase scene, Map<String, Long> out)
            throws Exception {
        for (boolean rails : new boolean[] {false, true}) {
            B173NativeEntityScene f = new B173NativeEntityScene(names, scene);
            if (rails) for (int x = 4; x <= 14; x++) f.block(x, 65, 8, 66, 1);
            f.position(8.5, 65 + f.number("yOffset"), 8.5);
            names.set(f.entity, "motionX", 0.3D);
            long before = f.milli("posX");
            for (int tick = 0; tick < 3; tick++) f.tick();
            String prefix = rails ? "rail" : "bare";
            out.put(prefix + "-speed", Math.abs(f.milli("motionX")));
            out.put(prefix + "-displacement", f.milli("posX") - before);
        }
    }
}
