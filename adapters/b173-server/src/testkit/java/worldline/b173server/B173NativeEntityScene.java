package worldline.b173server;

import java.util.Random;
import worldline.testapi.NativeEntityDynamicsCase;

/** A fresh native entity and memory world; assignments define preconditions, never proof outcomes. */
final class B173NativeEntityScene {
    final B173NativeServerSymbols names;
    final NativeEntityDynamicsCase scene;
    final Object world, entity;

    B173NativeEntityScene(B173NativeServerSymbols names, NativeEntityDynamicsCase scene) throws Exception {
        this.names = names; this.scene = scene;
        world = new B173NativeMemoryWorld(names).create();
        Object registered = ((java.util.Map<?, ?>) names.constant("EntityList", "IDtoClassMapping"))
                .get(scene.legacyId());
        require(registered instanceof Class<?>, "native registry subject is absent");
        entity = ((Class<?>) registered).getConstructor(names.type("World")).newInstance(world);
        require(entity != null && names.named(entity.getClass()).equals(
                "net/minecraft/src/" + scene.runtimeClass()), "native registry class differs");
        String origin = entity.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        require(origin.contains("minecraft_server/bin") || origin.contains("minecraft_server.jar"),
                "native server class provenance differs: " + origin);
        ((Random) names.get(entity, "rand")).setSeed(17350907L);
        ((Random) names.get(world, "rand")).setSeed(17350907L);
        if (scene.legacyId() == 1) names.set(entity, "item", names.type("ItemStack")
                .getConstructor(int.class, int.class, int.class).newInstance(1, 2, 0));
        if (scene.legacyId() == 9) {
            names.set(entity, "art", names.type("EnumArt").getEnumConstants()[0]);
            names.set(entity, "xPosition", 8); names.set(entity, "yPosition", 72); names.set(entity, "zPosition", 8);
            names.call(entity, "EntityPainting", "func_179_a", "(I)V", 0);
        }
        if (scene.legacyId() == 20) names.set(entity, "fuse", 80);
        if (scene.legacyId() == 21) names.set(entity, "blockID", 12);
        if (scene.legacyId() == 55) names.call(entity, "EntitySlime", "setSlimeSize", "(I)V", 1);
        position(8.5, 72 + number("yOffset"), 8.5);
        names.set(entity, "rotationYaw", 45F); names.set(entity, "prevRotationYaw", 45F);
        names.set(entity, "rotationPitch", 0F); names.set(entity, "prevRotationPitch", 0F);
        names.set(entity, "motionX", 0D); names.set(entity, "motionY", 0D); names.set(entity, "motionZ", 0D);
    }
    void position(double x, double y, double z) throws Exception {
        names.call(entity, "Entity", "setPosition", "(DDD)V", x, y, z);
    }
    void tick() throws Exception { names.call(entity, "Entity", "onUpdate", "()V"); }
    void move(double x, double y, double z) throws Exception {
        names.call(entity, "Entity", "moveEntity", "(DDD)V", x, y, z);
    }
    double number(String field) throws Exception { return ((Number) names.get(entity, field)).doubleValue(); }
    long milli(String field) throws Exception { return Math.round(number(field) * 1000D); }
    long flag(String field) throws Exception { return (Boolean) names.get(entity, field) ? 1 : 0; }
    void block(int x, int y, int z, int id) throws Exception { block(x, y, z, id, 0); }
    void block(int x, int y, int z, int id, int metadata) throws Exception {
        names.call(world, "World", "setBlockAndMetadata", "(IIIII)Z", x, y, z, id, metadata);
    }
    int block(int x, int y, int z) throws Exception {
        return (Integer) names.call(world, "IBlockAccess", "getBlockId", "(III)I", x, y, z);
    }
    void fill(int xa, int xb, int ya, int yb, int za, int zb, int id) throws Exception {
        for (int x = xa; x <= xb; x++) for (int y = ya; y <= yb; y++) for (int z = za; z <= zb; z++) {
            block(x, y, z, id);
        }
    }
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
