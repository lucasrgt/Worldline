package worldline.b173;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** Creates and invokes mapped or official sign and piston tile renderers. */
final class B173TileEntityRuntime {
    private final String[] names;
    private final B173HeadlessClientResources resources;
    private final Class<?> blockType, tileType, specialType;
    private final Object blocks, air;
    private final Field material;
    private final Method renderType;

    B173TileEntityRuntime(String[] names, int terrainTexture, int signTexture) throws Exception {
        this.names = names;
        resources = new B173HeadlessClientResources(names, terrainTexture, signTexture);
        blockType = Class.forName(names[16]);
        tileType = Class.forName(names[4]);
        specialType = Class.forName(names[5]);
        blocks = blockType.getField(names[18]).get(null);
        material = blockType.getField(names[19]);
        air = Class.forName(names[20]).getField(names[21]).get(null);
        renderType = blockType.getMethod(names[43]);
    }

    Object renderer(int legacyId, int metadata, String route) throws Exception {
        Object block = Array.get(blocks, legacyId);
        require(block != null && ((Integer) renderType.invoke(block)).intValue() == -1,
                "block does not use a tile renderer: " + legacyId);
        boolean sign = "sign".equals(route);
        Class<?> rendererType = Class.forName(names[sign ? 0 : 1]);
        Object renderer = rendererType.getConstructor().newInstance();
        specialType.getMethod(names[26], resources.dispatcherType())
                .invoke(renderer, resources.dispatcher());
        return renderer;
    }

    Object entity(int legacyId, int metadata, String route, Object renderer) throws Exception {
        boolean sign = "sign".equals(route);
        Class<?> entityType = Class.forName(names[sign ? 2 : 3]);
        Object entity;
        if (sign) {
            entity = entityType.getConstructor().newInstance();
            B173ReflectionObjects.set(entity, tileType, names[36],
                    B173TileWorld.create(names, legacyId, metadata));
        } else {
            entity = entityType.getConstructor(int.class, int.class, int.class,
                    boolean.class, boolean.class).newInstance(Integer.valueOf(1),
                            Integer.valueOf(0), Integer.valueOf(metadata), Boolean.TRUE,
                            Boolean.FALSE);
            Class<?> accessType = Class.forName(names[17]);
            Object access = Proxy.newProxyInstance(accessType.getClassLoader(),
                    new Class<?>[] {accessType}, new B173WorldBlockAccess(blocks, material, air,
                            names[22], names[23], 1, 0));
            Object renderBlocks = Class.forName(names[15]).getConstructor(accessType)
                    .newInstance(access);
            B173ReflectionObjects.set(renderer, Class.forName(names[1]), names[42], renderBlocks);
        }
        B173ReflectionObjects.set(entity, tileType, names[37], Integer.valueOf(0));
        B173ReflectionObjects.set(entity, tileType, names[38], Integer.valueOf(64));
        B173ReflectionObjects.set(entity, tileType, names[39], Integer.valueOf(0));
        return entity;
    }

    String invoke(Object renderer, Object entity, String route) throws Exception {
        boolean sign = "sign".equals(route);
        Class<?> entityType = Class.forName(names[sign ? 2 : 3]);
        Method render = renderer.getClass().getMethod(names[sign ? 24 : 25], entityType,
                double.class, double.class, double.class, float.class);
        render.invoke(renderer, entity, Double.valueOf(0.0), Double.valueOf(0.0),
                Double.valueOf(0.0), Float.valueOf(0.0f));
        return renderer.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
