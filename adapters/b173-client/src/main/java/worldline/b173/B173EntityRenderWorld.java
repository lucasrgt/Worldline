package worldline.b173;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Random;

/** Canonical full-bright chunk and official texture engine for isolated native entity bodies. */
final class B173EntityRenderWorld {
    private final Object world, manager;

    B173EntityRenderWorld(B173NativeNames names) throws Exception {
        Class<?> worldType = names.type("World");
        world = B173ReflectionObjects.allocate(worldType);
        Object provider = names.type("WorldProviderSurface").getConstructor().newInstance();
        float[] brightness = new float[16]; Arrays.fill(brightness, 1.0F);
        names.set(provider, "lightBrightnessTable", brightness);
        names.set(provider, "worldObj", world);
        names.set(world, "worldProvider", provider);
        names.set(world, "rand", new Random(173L));
        Object chunk = names.type("Chunk").getConstructor(worldType, byte[].class, int.class, int.class)
                .newInstance(world, new byte[32768], 0, 0);
        Class<?> chunkProvider = names.type("IChunkProvider");
        Object access = Proxy.newProxyInstance(chunkProvider.getClassLoader(),
                new Class<?>[] {chunkProvider}, new B173ChunkProviderAccess(chunk));
        names.set(world, "chunkProvider", access);
        Object packs = B173ReflectionObjects.allocate(names.type("TexturePackList"));
        names.set(packs, "selectedTexturePack", names.type("TexturePackDefault").getConstructor().newInstance());
        Object settings = names.type("GameSettings").getConstructor().newInstance();
        Object engine = names.type("RenderEngine").getConstructor(names.type("TexturePackList"),
                names.type("GameSettings")).newInstance(packs, settings);
        Object client = B173ReflectionObjects.allocate(names.type("MinecraftImpl"));
        names.set(client, "renderEngine", engine);
        names.set(client, "gameSettings", settings);
        Object itemRenderer = names.type("ItemRenderer").getConstructor(names.type("Minecraft"))
                .newInstance(client);
        manager = names.singleton("RenderManager", "instance");
        names.set(manager, "renderEngine", engine);
        names.set(manager, "itemRenderer", itemRenderer);
        names.set(manager, "worldObj", world);
        names.set(manager, "options", settings);
        names.set(manager, "playerViewY", 0.0F);
        names.set(manager, "playerViewX", 0.0F);
    }

    Object world() { return world; }
    Object manager() { return manager; }
}
