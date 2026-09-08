package worldline.b173server;

import java.lang.reflect.Proxy;

/** Constructs native server worlds with native chunks and original memory-only storage facades. */
final class B173NativeMemoryWorld {
    private final B173NativeServerSymbols names;
    B173NativeMemoryWorld(B173NativeServerSymbols names) { this.names = names; }

    Object create() throws Exception {
        Class<?> saveType = names.type("ISaveHandler"), playerType = names.type("IPlayerFileData");
        Class<?> chunkType = names.type("IChunkLoader"), infoType = names.type("WorldInfo");
        Object info = infoType.getConstructor(long.class, String.class).newInstance(17350907L, "native-dynamics");
        names.call(info, "WorldInfo", "setSpawnPosition", "(III)V", 8, 64, 8);
        Object chunks = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {chunkType},
                (proxy, method, args) -> {
                    if (method.getReturnType() == names.type("Chunk")) {
                        return chunk(args[0], (Integer) args[1], (Integer) args[2]);
                    }
                    if (method.getReturnType() == void.class) return null;
                    throw new UnsupportedOperationException("unexpected chunk storage call: " + method);
                });
        Object save = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {saveType, playerType},
                (proxy, method, args) -> {
                    if (method.getReturnType() == infoType) return info;
                    if (method.getReturnType() == chunkType) return chunks;
                    if (method.getReturnType() == playerType) return proxy;
                    if (method.getReturnType() == void.class || method.getReturnType() == java.io.File.class)
                        return null;
                    throw new UnsupportedOperationException("unexpected world storage call: " + method);
                });
        Object world = names.type("World").getConstructor(saveType, String.class, long.class,
                names.type("WorldProvider")).newInstance(save, "native-dynamics", 17350907L, null);
        names.set(world, "difficultySetting", 2);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            names.call(world, "World", "getChunkFromChunkCoords", "(II)Lnet/minecraft/src/Chunk;", x, z);
        }
        return world;
    }
    private Object chunk(Object world, int x, int z) throws Exception {
        byte[] blocks = new byte[32768];
        for (int bx = 0; bx < 16; bx++) for (int bz = 0; bz < 16; bz++) {
            for (int y = 0; y <= 64; y++) blocks[bx << 11 | bz << 7 | y] = (byte) (y == 0 ? 7 : 1);
        }
        Object chunk = names.type("Chunk").getConstructor(names.type("World"), byte[].class,
                int.class, int.class).newInstance(world, blocks, x, z);
        names.set(chunk, "isTerrainPopulated", true); names.set(chunk, "neverSave", true);
        names.call(chunk, "Chunk", "func_353_b", "()V");
        return chunk;
    }
}
