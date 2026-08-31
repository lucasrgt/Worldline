package worldline.m778;

import aero.modellib.render.Aero_FrustumCull;
import java.lang.reflect.Field;

/** Toggles the M778-only mutable culling switch and resets camera history. */
final class VisualCulling {
    private static final Field ENABLED = field();

    private VisualCulling() {}

    static void set(boolean enabled) {
        try {
            ENABLED.setBoolean(null, enabled);
            Aero_FrustumCull.clearCamera();
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("cannot toggle M778 culling", error);
        }
    }

    private static Field field() {
        try {
            Field result = Aero_FrustumCull.class.getDeclaredField("ENABLED");
            result.setAccessible(true);
            return result;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("M778 culling field drift", error);
        }
    }
}
