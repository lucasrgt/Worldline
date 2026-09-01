package worldline.b173;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** Supplies one immutable chunk to an allocated headless Beta world. */
final class B173ChunkProviderAccess implements InvocationHandler {
    private final Object chunk;

    B173ChunkProviderAccess(Object chunk) { this.chunk = chunk; }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) {
        Class<?> result = method.getReturnType();
        if (result.isInstance(chunk)) return chunk;
        if (result == boolean.class) return Boolean.TRUE;
        if (result == int.class) return Integer.valueOf(0);
        if (result == long.class) return Long.valueOf(0L);
        if (result == float.class) return Float.valueOf(0.0f);
        if (result == double.class) return Double.valueOf(0.0d);
        if (result == String.class) return "worldline-headless-chunk";
        return null;
    }
}
