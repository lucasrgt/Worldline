package worldline.b173;

import java.lang.reflect.Proxy;

/** Allocates the smallest concrete Beta world needed by a sign tile entity. */
final class B173TileWorld {
    private B173TileWorld() { }

    static Object create(String[] names, int legacyId, int metadata) throws Exception {
        Class<?> worldType = Class.forName(names[12]);
        Class<?> chunkType = Class.forName(names[13]);
        Class<?> providerType = Class.forName(names[14]);
        Object world = B173ReflectionObjects.allocate(worldType);
        byte[] blocks = new byte[16 * 16 * 128];
        blocks[64] = (byte) legacyId;
        Object chunk = chunkType.getConstructor(worldType, byte[].class, int.class, int.class)
                .newInstance(world, blocks, Integer.valueOf(0), Integer.valueOf(0));
        chunkType.getMethod(names[41], int.class, int.class, int.class, int.class)
                .invoke(chunk, Integer.valueOf(0), Integer.valueOf(64), Integer.valueOf(0),
                        Integer.valueOf(metadata));
        Object provider = Proxy.newProxyInstance(providerType.getClassLoader(),
                new Class<?>[] {providerType}, new B173ChunkProviderAccess(chunk));
        B173ReflectionObjects.set(world, worldType, names[40], provider);
        return world;
    }
}
