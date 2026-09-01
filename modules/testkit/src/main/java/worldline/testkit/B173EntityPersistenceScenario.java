package worldline.testkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Executes native NBT reconstruction against exact mapped Beta 1.7.3 classes. */
public final class B173EntityPersistenceScenario implements EntityPersistenceScenario {
    private static final double EPSILON = 0.000001D;
    private final String version;

    public B173EntityPersistenceScenario(String version) {
        if (!"b1.7.3".equals(version)) throw new IllegalArgumentException("version");
        this.version = version;
    }

    @Override public List<EntityPersistenceObservation> observe() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            initialize(loader, "net.minecraft.src.Block");
            initialize(loader, "net.minecraft.src.Item");
            Class<?> entity = initialize(loader, "net.minecraft.src.Entity");
            Class<?> world = initialize(loader, "net.minecraft.src.World");
            Class<?> list = initialize(loader, "net.minecraft.src.EntityList");
            Class<?> compound = initialize(loader, "net.minecraft.src.NBTTagCompound");
            Class<?> streams = initialize(loader, "net.minecraft.src.CompressedStreamTools");
            Field registry = list.getDeclaredField("IDtoClassMapping");
            registry.setAccessible(true);
            TreeMap<Integer, Class<?>> types = new TreeMap<Integer, Class<?>>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) registry.get(null)).entrySet()) {
                types.put((Integer) entry.getKey(), (Class<?>) entry.getValue());
            }
            require(types.size() == 24 && Modifier.isAbstract(types.get(48).getModifiers()),
                    "EntityList persistence boundary drifted");
            List<EntityPersistenceObservation> result =
                    new ArrayList<EntityPersistenceObservation>();
            for (Map.Entry<Integer, Class<?>> entry : types.entrySet()) {
                int id = entry.getKey().intValue();
                if (id == 48) continue;
                Object original = entry.getValue().getConstructor(world)
                        .newInstance(new Object[] {null});
                seedCommon(entity, original);
                seedSpecific(loader, id, original);
                Object first = compound.getConstructor().newInstance();
                boolean identified = ((Boolean) entity.getMethod("addEntityID", compound)
                        .invoke(original, first)).booleanValue();
                String registryName = (String) list.getMethod("getEntityString", entity)
                        .invoke(null, original);
                byte[] before = binary(streams, compound, first);
                Object restored = list.getMethod("createEntityFromNBT", compound, world)
                        .invoke(null, first, null);
                boolean reconstructed = identified && restored != null;
                boolean typeExact = reconstructed && restored.getClass() == original.getClass();
                boolean commonExact = typeExact && commonExact(entity, original, restored);
                Object second = compound.getConstructor().newInstance();
                boolean reidentified = reconstructed && ((Boolean) entity.getMethod(
                        "addEntityID", compound).invoke(restored, second)).booleanValue();
                byte[] after = reidentified ? binary(streams, compound, second) : new byte[0];
                result.add(new EntityPersistenceObservation(subject(id), registryName,
                        original.getClass().getSimpleName(), reconstructed, typeExact, commonExact,
                        Arrays.equals(before, after), sha(before)));
            }
            require(result.size() == 23, "concrete entity persistence width drifted");
            return Collections.unmodifiableList(result);
        } catch (ReflectiveOperationException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new IllegalStateException("Beta 1.7.3 entity persistence capture failed", cause);
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Beta 1.7.3 entity NBT encoding failed", error);
        }
    }

    private static void seedCommon(Class<?> entity, Object value)
            throws ReflectiveOperationException {
        entity.getMethod("setPosition", double.class, double.class, double.class)
                .invoke(value, 12.25D, 64.5D, -3.75D);
        set(entity, value, "motionX", 0.125D);
        set(entity, value, "motionY", -0.25D);
        set(entity, value, "motionZ", 0.5D);
        set(entity, value, "rotationYaw", 137.5F);
        set(entity, value, "rotationPitch", -22.25F);
        set(entity, value, "fallDistance", 3.75F);
        set(entity, value, "fire", 19);
        set(entity, value, "air", 211);
        set(entity, value, "onGround", true);
    }

    private static void seedSpecific(ClassLoader loader, int id, Object value)
            throws ReflectiveOperationException {
        Class<?> type = value.getClass();
        if (id == 1) {
            Class<?> stack = initialize(loader, "net.minecraft.src.ItemStack");
            type.getField("item").set(value,
                    stack.getConstructor(int.class, int.class, int.class).newInstance(4, 3, 2));
            type.getField("age").setInt(value, 37);
        } else if (id == 9) {
            Class<?> art = initialize(loader, "net.minecraft.src.EnumArt");
            type.getField("art").set(value, art.getEnumConstants()[3]);
            type.getField("xPosition").setInt(value, 17);
            type.getField("yPosition").setInt(value, 66);
            type.getField("zPosition").setInt(value, -8);
            type.getMethod("func_412_b", int.class).invoke(value, 2);
        } else if (id == 10) {
            type.getField("arrowShake").setInt(value, 4);
            type.getField("doesArrowBelongToPlayer").setBoolean(value, true);
            set(type, value, "inTile", 5);
            set(type, value, "inGround", true);
        } else if (id == 11) {
            type.getField("shakeSnowball").setInt(value, 5);
            set(type, value, "inTileSnowball", 20);
            set(type, value, "inGroundSnowball", true);
        } else if (id == 20) {
            type.getField("fuse").setInt(value, 37);
        } else if (id == 21) {
            type.getField("blockID").setInt(value, 12);
            type.getField("fallTime").setInt(value, 8);
        } else if (id == 40) {
            type.getField("minecartType").setInt(value, 2);
            type.getField("fuel").setInt(value, 91);
            type.getField("pushX").setDouble(value, 0.25D);
            type.getField("pushZ").setDouble(value, -0.5D);
        } else if (id >= 49) {
            set(type, value, "health", 17);
        }
        if (id == 55) type.getMethod("setSlimeSize", int.class).invoke(value, 3);
        if (id == 57) set(type, value, "angerLevel", 73);
        if (id == 90) type.getMethod("setSaddled", boolean.class).invoke(value, true);
        if (id == 91) {
            type.getMethod("setFleeceColor", int.class).invoke(value, 5);
            type.getMethod("setSheared", boolean.class).invoke(value, true);
        }
        if (id == 95) {
            type.getMethod("setWolfOwner", String.class).invoke(value, "Worldline");
            type.getMethod("setWolfTamed", boolean.class).invoke(value, true);
            type.getMethod("setWolfAngry", boolean.class).invoke(value, true);
            type.getMethod("setWolfSitting", boolean.class).invoke(value, true);
        }
    }

    private static boolean commonExact(Class<?> entity, Object left, Object right)
            throws ReflectiveOperationException {
        return close(number(entity, left, "posX"), number(entity, right, "posX"))
                && close(number(entity, left, "posY"), number(entity, right, "posY"))
                && close(number(entity, left, "posZ"), number(entity, right, "posZ"))
                && close(number(entity, left, "motionX"), number(entity, right, "motionX"))
                && close(number(entity, left, "motionY"), number(entity, right, "motionY"))
                && close(number(entity, left, "motionZ"), number(entity, right, "motionZ"))
                && close(number(entity, left, "rotationYaw"), number(entity, right, "rotationYaw"))
                && close(number(entity, left, "rotationPitch"), number(entity, right, "rotationPitch"))
                && close(number(entity, left, "fallDistance"), number(entity, right, "fallDistance"))
                && ((Integer) field(entity, "fire").get(left)).intValue()
                        == ((Integer) field(entity, "fire").get(right)).intValue()
                && ((Integer) field(entity, "air").get(left)).intValue()
                        == ((Integer) field(entity, "air").get(right)).intValue()
                && field(entity, "onGround").getBoolean(left)
                        == field(entity, "onGround").getBoolean(right);
    }

    private static byte[] binary(Class<?> streams, Class<?> compound, Object value)
            throws ReflectiveOperationException, java.io.IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        Method write = streams.getMethod("func_1139_a", compound, DataOutput.class);
        write.invoke(null, value, output);
        output.flush();
        return bytes.toByteArray();
    }

    private static String sha(byte[] value) {
        try {
            StringBuilder result = new StringBuilder();
            for (byte item : MessageDigest.getInstance("SHA-256").digest(value)) {
                result.append(String.format("%02x", item & 255));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    private String subject(int id) { return String.format("%s:entity/%03d", version, id); }

    private static Class<?> initialize(ClassLoader loader, String name)
            throws ClassNotFoundException {
        return Class.forName(name, true, loader);
    }

    private static void set(Class<?> type, Object target, String name, Object value)
            throws ReflectiveOperationException {
        Field field = field(type, name);
        if (value instanceof Integer) field.setInt(target, ((Integer) value).intValue());
        else if (value instanceof Float) field.setFloat(target, ((Float) value).floatValue());
        else if (value instanceof Double) field.setDouble(target, ((Double) value).doubleValue());
        else if (value instanceof Boolean) field.setBoolean(target, ((Boolean) value).booleanValue());
        else field.set(target, value);
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field result = current.getDeclaredField(name);
                result.setAccessible(true);
                return result;
            } catch (NoSuchFieldException absent) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static double number(Class<?> type, Object target, String name)
            throws ReflectiveOperationException {
        return ((Number) field(type, name).get(target)).doubleValue();
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) <= EPSILON;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
