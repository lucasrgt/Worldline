package worldline.b173;

import java.lang.reflect.Field;

/** Checked reflective access to the mapped fields controlled by the adapter. */
final class B173Reflect {
    private B173Reflect() {}

    static int getInt(Class<?> owner, String name, Object target) {
        try { return field(owner, name).getInt(target); }
        catch (IllegalAccessException error) { throw new IllegalStateException(error); }
    }

    static long getLong(Class<?> owner, String name, Object target) {
        try { return field(owner, name).getLong(target); }
        catch (IllegalAccessException error) { throw new IllegalStateException(error); }
    }

    static void setInt(Class<?> owner, String name, Object target, int value) {
        try { field(owner, name).setInt(target, value); }
        catch (IllegalAccessException error) { throw new IllegalStateException(error); }
    }

    static void setFloat(Class<?> owner, String name, Object target, float value) {
        try { field(owner, name).setFloat(target, value); }
        catch (IllegalAccessException error) { throw new IllegalStateException(error); }
    }

    static Object get(Class<?> owner, String name, Object target) {
        try { return field(owner, name).get(target); }
        catch (IllegalAccessException error) { throw new IllegalStateException(error); }
    }

    private static Field field(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot access mapped field " + name, error);
        }
    }
}
