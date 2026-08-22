package worldline.aero;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.world.World;

/** Marks loaded chunks modified so vanilla's 24-chunk autosave batch can fire. */
public final class WorldlineSaveForce {
    private WorldlineSaveForce() {}

    public static int markDirty(World world, int count) {
        int marked = 0;
        for (int cx = -8; marked < count && cx <= 8; cx++)
            for (int cz = -8; marked < count && cz <= 8; cz++)
                if (mark(world.getChunk(cx, cz))) marked++;
        return marked;
    }

    private static boolean mark(Object chunk) {
        if (chunk == null) return false;
        Class<?> type = chunk.getClass();
        String[] methods = new String[] {"setModified", "setChunkModified", "markDirty"};
        for (int i = 0; i < methods.length; i++) {
            try {
                Method method = type.getMethod(methods[i]);
                method.invoke(chunk);
                return true;
            } catch (Exception ignored) {
            }
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField("isModified");
                field.setAccessible(true);
                field.setBoolean(chunk, true);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
