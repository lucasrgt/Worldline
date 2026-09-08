package worldline.b173;

import java.util.ArrayList;
import java.util.Random;

/** Original controlled fixture state; all particle construction, ticking and rendering remain native. */
final class B173ParticleScene {
    private final B173NativeNames names;
    private final Object world, engine, camera;
    B173ParticleScene(B173NativeNames names) throws Exception {
        this.names = names;
        names.type("Block"); names.type("Item");
        B173EntityRenderWorld scene = new B173EntityRenderWorld(names);
        world = scene.world(); engine = names.get(scene.manager(), "renderEngine");
        for (String field : new String[] {"loadedEntityList", "worldAccesses", "collidingBoundingBoxes",
                "playerEntities", "weatherEffects", "field_1012_M"}) names.set(world, field, new ArrayList<Object>());
        camera = names.type("EntityPig").getConstructor(names.type("World")).newInstance(world);
        names.method("Entity", "setPosition", double.class, double.class, double.class)
                .invoke(camera, 0.0D, 64.0D, 0.0D);
        for (String field : new String[] {"lastTickPosX", "lastTickPosZ"}) names.set(camera, field, 0.0D);
        names.set(camera, "lastTickPosY", 64.0D);
        names.set(camera, "rotationYaw", 0.0F); names.set(camera, "rotationPitch", 0.0F);
    }
    Object manager() throws Exception {
        return names.type("EffectRenderer").getConstructor(names.type("World"), names.type("RenderEngine"))
                .newInstance(world, engine);
    }
    Object particle(String type) throws Exception {
        boolean dust = type.equals("EntityReddustFX");
        Class<?> velocity = dust ? float.class : double.class;
        Object x = dust ? (Object) Float.valueOf(0.75F) : Double.valueOf(0.0D);
        Object y = dust ? (Object) Float.valueOf(0.75F) : Double.valueOf(0.02D);
        Object particle = names.type(type).getConstructor(names.type("World"), double.class, double.class,
                double.class, velocity, velocity, velocity)
                .newInstance(world, 0.0D, 64.0D, 0.0D, x, y, x);
        int max = (Integer) names.get(particle, "particleMaxAge");
        if (max < 1 || max > 200 || (Integer) names.get(particle, "particleAge") != 0
                || (Boolean) names.get(particle, "isDead")
                || (Integer) names.method("EntityFX", "getFXLayer").invoke(particle) != 0)
            throw new IllegalStateException("native particle construction invariant");
        names.method("Entity", "setPosition", double.class, double.class, double.class)
                .invoke(particle, 0.0D, 64.0D, 0.0D);
        for (String field : new String[] {"prevPosX", "prevPosZ", "motionX", "motionZ"})
            names.set(particle, field, 0.0D);
        names.set(particle, "prevPosY", 64.0D); names.set(particle, "motionY", 0.02D);
        names.set(particle, "rand", new Random(173L)); names.set(particle, "particleMaxAge", 8);
        names.set(particle, "particleScale", 1.0F);
        String scale = type.equals("EntitySmokeFX") ? "field_671_a"
                : type.equals("EntityFlameFX") ? "field_672_a" : "field_673_a";
        names.set(particle, scale, 1.0F);
        for (String field : new String[] {"particleRed", "particleGreen", "particleBlue"})
            names.set(particle, field, 0.75F);
        return particle;
    }
    void draw(Object manager) throws Exception {
        names.method("EffectRenderer", "renderParticles", names.type("Entity"), float.class)
                .invoke(manager, camera, 0.5F);
    }
}
