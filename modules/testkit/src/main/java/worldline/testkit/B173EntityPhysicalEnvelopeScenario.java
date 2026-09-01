package worldline.testkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Observes concrete EntityList entries through exact mapped Beta 1.7.3 classes. */
public final class B173EntityPhysicalEnvelopeScenario implements EntityPhysicalEnvelopeScenario {
    private static final double X = 12.25D, Y = 64.5D, Z = -3.75D, EPSILON = 0.000001D;
    private final String version;

    public B173EntityPhysicalEnvelopeScenario(String version) {
        if (!"b1.7.3".equals(version)) throw new IllegalArgumentException("version");
        this.version = version;
    }

    @Override public List<EntityPhysicalEnvelopeObservation> observe() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            initialize(loader, "net.minecraft.src.Block");
            initialize(loader, "net.minecraft.src.Item");
            Class<?> entity = initialize(loader, "net.minecraft.src.Entity");
            Class<?> world = initialize(loader, "net.minecraft.src.World");
            Class<?> list = initialize(loader, "net.minecraft.src.EntityList");
            Field registry = list.getDeclaredField("IDtoClassMapping");
            registry.setAccessible(true);
            Map<?, ?> raw = (Map<?, ?>) registry.get(null);
            TreeMap<Integer, Class<?>> types = new TreeMap<Integer, Class<?>>();
            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                types.put((Integer) entry.getKey(), (Class<?>) entry.getValue());
            }
            require(types.size() == 24, "EntityList width drifted");
            require(Modifier.isAbstract(types.get(48).getModifiers()),
                    "registered EntityLiving is no longer abstract");
            List<EntityPhysicalEnvelopeObservation> result =
                    new ArrayList<EntityPhysicalEnvelopeObservation>();
            for (Map.Entry<Integer, Class<?>> entry : types.entrySet()) {
                if (entry.getKey() == 48) continue;
                Object value = construct(entry.getKey(), entry.getValue(), world);
                if (entry.getKey() == 55) {
                    entry.getValue().getMethod("setSlimeSize", int.class).invoke(value, 1);
                }
                entity.getMethod("setPosition", double.class, double.class, double.class)
                        .invoke(value, X, Y, Z);
                float width = number(entity, "width", value);
                float height = number(entity, "height", value);
                float yOffset = number(entity, "yOffset", value);
                float ySize = number(entity, "ySize", value);
                Object box = entity.getField("boundingBox").get(value);
                boolean centered = close(decimal(box, "minX"), X - width / 2.0D)
                        && close(decimal(box, "maxX"), X + width / 2.0D)
                        && close(decimal(box, "minZ"), Z - width / 2.0D)
                        && close(decimal(box, "maxZ"), Z + width / 2.0D);
                double base = Y - yOffset + ySize;
                boolean vertical = close(decimal(box, "minY"), base)
                        && close(decimal(box, "maxY"), base + height);
                boolean collidable = callBoolean(entity, "canBeCollidedWith", value);
                boolean pushable = callBoolean(entity, "canBePushed", value);
                Method pair = entity.getMethod("getCollisionBox", entity);
                boolean pairBox = pair.invoke(value, value) != null;
                result.add(new EntityPhysicalEnvelopeObservation(subject(entry.getKey()),
                        width, height, yOffset, collidable, pushable, pairBox,
                        centered, vertical));
            }
            require(result.size() == 23, "concrete entity envelope width drifted");
            return Collections.unmodifiableList(result);
        } catch (ReflectiveOperationException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new IllegalStateException("Beta 1.7.3 entity envelope capture failed", cause);
        }
    }

    private static Object construct(int id, Class<?> type, Class<?> world)
            throws ReflectiveOperationException {
        if (id == 21) {
            Constructor<?> constructor = type.getConstructor(world, double.class, double.class,
                    double.class, int.class);
            return constructor.newInstance(null, X, Y, Z, 12);
        }
        return type.getConstructor(world).newInstance(new Object[] {null});
    }

    private String subject(int id) { return String.format("%s:entity/%03d", version, id); }

    private static Class<?> initialize(ClassLoader loader, String name)
            throws ClassNotFoundException {
        return Class.forName(name, true, loader);
    }

    private static float number(Class<?> entity, String field, Object value)
            throws ReflectiveOperationException {
        return entity.getField(field).getFloat(value);
    }

    private static double decimal(Object value, String field) throws ReflectiveOperationException {
        return value.getClass().getField(field).getDouble(value);
    }

    private static boolean callBoolean(Class<?> entity, String method, Object value)
            throws ReflectiveOperationException {
        return ((Boolean) entity.getMethod(method).invoke(value)).booleanValue();
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) <= EPSILON;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
