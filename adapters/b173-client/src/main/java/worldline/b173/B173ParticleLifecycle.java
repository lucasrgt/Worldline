package worldline.b173;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import worldline.testapi.ParticleLifecycleObservation;

/** Four-process callers use this native particle lifecycle without substituting particle behavior. */
public final class B173ParticleLifecycle {
    private B173ParticleLifecycle() { }
    public static List<ParticleLifecycleObservation> capture(Path mappings, boolean official,
            List<String> particles) throws Exception {
        B173NativeNames names = new B173NativeNames(mappings, official);
        List<ParticleLifecycleObservation> result = new ArrayList<ParticleLifecycleObservation>();
        try (B173ParticleCanvas canvas = new B173ParticleCanvas()) {
            B173ParticleScene scene = new B173ParticleScene(names);
            for (String type : particles) {
                Object manager = scene.manager(), particle = scene.particle(type);
                provenance(manager, official); provenance(particle, official);
                names.method("EffectRenderer", "addEffect", names.type("EntityFX")).invoke(manager, particle);
                for (int tick = 0; tick <= 9; tick++) {
                    if (tick > 0) names.method("EffectRenderer", "updateEffects").invoke(manager);
                    canvas.clear();
                    if (tick > 0) scene.draw(manager);
                    ByteBuffer pixels = canvas.pixels();
                    int count = Integer.parseInt((String) names.method("EffectRenderer", "getStatistics")
                            .invoke(manager));
                    result.add(new ParticleLifecycleObservation(type, tick,
                            (Integer) names.get(particle, "particleAge"), (Boolean) names.get(particle, "isDead"),
                            count, position(names, particle), canvas.geometry(pixels), B173RgbaDigest.hash(pixels)));
                }
            }
        }
        return result;
    }
    private static String position(B173NativeNames names, Object particle) throws Exception {
        return String.format(java.util.Locale.ROOT, "%016x,%016x,%016x",
                Double.doubleToLongBits((Double) names.get(particle, "posX")),
                Double.doubleToLongBits((Double) names.get(particle, "posY")),
                Double.doubleToLongBits((Double) names.get(particle, "posZ")));
    }
    private static void provenance(Object value, boolean official) {
        String source = value.getClass().getProtectionDomain().getCodeSource().getLocation()
                .toString().replace('\\', '/');
        if (!source.contains(official ? "jars/minecraft.jar" : "minecraft/bin/"))
            throw new IllegalStateException("native particle provenance drift");
    }
}
