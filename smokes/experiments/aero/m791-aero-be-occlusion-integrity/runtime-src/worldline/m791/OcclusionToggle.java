package worldline.m791;

import aero.modellib.Aero_BECellOcclusion;
import aero.modellib.WorldlineM791OcclusionBridge;
import java.lang.reflect.Field;

/** Toggles the candidate only inside the counterbalanced runtime oracle. */
final class OcclusionToggle {
    private static final Field ENABLED = field();

    private OcclusionToggle() {}

    static void set(boolean enabled) {
        try {
            ENABLED.setBoolean(null, enabled);
            WorldlineM791OcclusionBridge.clearHistory();
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("cannot toggle M791 occlusion", error);
        }
    }

    private static Field field() {
        try {
            Field result = Aero_BECellOcclusion.class.getDeclaredField("ENABLED");
            result.setAccessible(true);
            return result;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("M791 occlusion field drift", error);
        }
    }
}
