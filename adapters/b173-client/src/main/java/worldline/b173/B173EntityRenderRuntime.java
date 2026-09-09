package worldline.b173;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

/** Native EntityList construction and RenderManager dispatch under a fixed render-only scene. */
final class B173EntityRenderRuntime {
    private final B173NativeNames names;
    private final B173EntityRenderWorld scene;
    private final TreeMap<Integer, Class<?>> types = new TreeMap<>();

    B173EntityRenderRuntime(B173NativeNames names) throws Exception {
        this.names = names;
        names.type("Block"); names.type("Item");
        Map<?, ?> registry = (Map<?, ?>) names.singleton("EntityList", "IDtoClassMapping");
        for (Map.Entry<?, ?> entry : registry.entrySet()) {
            types.put((Integer) entry.getKey(), (Class<?>) entry.getValue());
        }
        require(types.size() == 24 && Modifier.isAbstract(types.get(48).getModifiers()), "native registry drift");
        scene = new B173EntityRenderWorld(names);
    }

    Object entity(int id) throws Exception {
        Class<?> type = types.get(id);
        require(type != null && !Modifier.isAbstract(type.getModifiers()), "non-concrete entity " + id);
        Object entity = type.getConstructor(names.type("World")).newInstance(scene.world());
        names.method("Entity", "setPosition", double.class, double.class, double.class)
                .invoke(entity, 8.0D, 64.0D, 8.0D);
        for (String field : new String[] {"rotationYaw", "rotationPitch", "prevRotationYaw", "prevRotationPitch"}) {
            names.set(entity, field, 0.0F);
        }
        if (id >= 49) {
            names.set(entity, "renderYawOffset", 0.0F); names.set(entity, "prevRenderYawOffset", 0.0F);
        }
        if (id == 1) {
            names.set(entity, "item", names.type("ItemStack").getConstructor(int.class, int.class, int.class)
                    .newInstance(4, 3, 0));
            names.set(entity, "field_804_d", 0.0F);
        }
        if (id == 9) {
            names.set(entity, "art", names.type("EnumArt").getEnumConstants()[0]);
            names.set(entity, "yPosition", 64);
            names.method("EntityPainting", "func_412_b", int.class).invoke(entity, 0);
        }
        if (id == 20) names.set(entity, "fuse", 40);
        if (id == 10) {
            names.set(entity, "rotationYaw", 45.0F); names.set(entity, "prevRotationYaw", 45.0F);
        }
        if (id == 21) names.set(entity, "blockID", 12);
        if (id == 55) names.method("EntitySlime", "setSlimeSize", int.class).invoke(entity, 1);
        if (id == 91) names.method("EntitySheep", "setFleeceColor", int.class).invoke(entity, 0);
        return entity;
    }

    Object renderer(Object entity) throws Exception {
        Object renderer = names.method("RenderManager", "getEntityRenderObject", names.type("Entity"))
                .invoke(scene.manager(), entity);
        require(renderer != null, "missing native entity renderer");
        return renderer;
    }

    void draw(Object renderer, Object entity) throws Exception {
        names.method("Render", "doRender", names.type("Entity"), double.class, double.class,
                double.class, float.class, float.class).invoke(renderer, entity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
    }

    float height(Object entity) throws Exception { return ((Number) names.get(entity, "height")).floatValue(); }
    float width(Object entity) throws Exception { return ((Number) names.get(entity, "width")).floatValue(); }
    String identity(Object value) {
        String name = names.named(value.getClass()); return name.substring(name.lastIndexOf('/') + 1);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
