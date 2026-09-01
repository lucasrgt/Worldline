package worldline.b173;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Narrow reflection primitives for mapped and official Beta client objects. */
final class B173ReflectionObjects {
    private B173ReflectionObjects() { }

    static Object allocate(Class<?> type) throws Exception {
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field singleton = unsafeType.getDeclaredField("theUnsafe");
        singleton.setAccessible(true);
        Object unsafe = singleton.get(null);
        Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
        return allocate.invoke(unsafe, type);
    }

    static void set(Object target, Class<?> owner, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
