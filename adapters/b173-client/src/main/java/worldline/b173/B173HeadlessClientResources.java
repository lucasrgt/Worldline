package worldline.b173;

import java.nio.ByteBuffer;
import java.util.Map;

/** Minimal official client resources required by native tile renderers. */
final class B173HeadlessClientResources {
    private final Object dispatcher;
    private final Class<?> dispatcherType;

    B173HeadlessClientResources(String[] names, int terrainTexture, int signTexture)
            throws Exception {
        Class<?> engineType = Class.forName(names[7]);
        Class<?> fontType = Class.forName(names[8]);
        Class<?> settingsType = Class.forName(names[9]);
        Class<?> packsType = Class.forName(names[10]);
        Class<?> defaultPackType = Class.forName(names[11]);
        Object packs = B173ReflectionObjects.allocate(packsType);
        Object defaultPack = defaultPackType.getConstructor().newInstance();
        B173ReflectionObjects.set(packs, packsType, names[30], defaultPack);
        Object settings = settingsType.getConstructor().newInstance();
        Object engine = engineType.getConstructor(packsType, settingsType)
                .newInstance(packs, settings);
        java.lang.reflect.Field textures = engineType.getDeclaredField(names[31]);
        textures.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, Integer> textureMap =
                (Map<String, Integer>) textures.get(engine);
        textureMap.put("/terrain.png", Integer.valueOf(terrainTexture));
        textureMap.put("/item/sign.png", Integer.valueOf(signTexture));
        Object font = B173ReflectionObjects.allocate(fontType);
        B173ReflectionObjects.set(font, fontType, names[32], new int[256]);
        B173ReflectionObjects.set(font, fontType, names[33],
                ByteBuffer.allocateDirect(4096).asIntBuffer());
        B173ReflectionObjects.set(font, fontType, names[34], Integer.valueOf(signTexture));
        B173ReflectionObjects.set(font, fontType, names[35], Integer.valueOf(0));
        dispatcherType = Class.forName(names[6]);
        dispatcher = dispatcherType.getField(names[27]).get(null);
        B173ReflectionObjects.set(dispatcher, dispatcherType, names[28], engine);
        B173ReflectionObjects.set(dispatcher, dispatcherType, names[29], font);
    }

    Object dispatcher() { return dispatcher; }
    Class<?> dispatcherType() { return dispatcherType; }
}
