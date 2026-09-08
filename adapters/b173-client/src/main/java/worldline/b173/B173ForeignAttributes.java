package worldline.b173;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/** Copies the provider-owned semantic attributes, preserving legacy getters. */
final class B173ForeignAttributes {
    private B173ForeignAttributes() {}

    static Map<String, String> read(Object value) throws ReflectiveOperationException {
        Map<String, String> result = new LinkedHashMap<String, String>();
        Method attributes = optional(value.getClass(), "attributes");
        if (attributes != null) {
            Object raw = attributes.invoke(value);
            if (!(raw instanceof Map)) throw new IllegalStateException("Butter attributes must be a map");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) raw).entrySet()) {
                if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof String)) {
                    throw new IllegalStateException("Butter attributes must contain strings");
                }
                result.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        for (String key : new String[] {"id", "label", "enabled", "focused"}) {
            Method getter = optional(value.getClass(), key);
            if (getter == null) continue;
            Object raw = getter.invoke(value);
            if (raw != null && (!"label".equals(key) || !String.valueOf(raw).isEmpty())) {
                result.put(key, String.valueOf(raw));
            }
        }
        return result;
    }

    private static Method optional(Class<?> type, String name) {
        try { return type.getMethod(name); }
        catch (NoSuchMethodException ignored) { return null; }
    }
}
