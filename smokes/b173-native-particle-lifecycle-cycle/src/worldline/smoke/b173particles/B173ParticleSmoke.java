package worldline.smoke.b173particles;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import worldline.b173.B173ParticleLifecycle;
import worldline.b173.B173RgbaDigest;
import worldline.testkit.ParticleLifecycleFixture;

/** Native creation invariants, manager updates, visible frames and terminal empty frame. */
public final class B173ParticleSmoke {
    private B173ParticleSmoke() { }
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 4 || !(arguments[0].equals("mapped") || arguments[0].equals("official")))
            throw new IllegalArgumentException("expected role mappings subjects output");
        List<String> lines = Files.readAllLines(Paths.get(arguments[2]), StandardCharsets.UTF_8);
        List<String> particles = Arrays.asList("EntitySmokeFX", "EntityFlameFX", "EntityReddustFX");
        if (!lines.get(0).equals("particle_class") || !lines.subList(1, lines.size()).equals(particles))
            throw new IllegalStateException("particle subject family drift");
        String canonical = ParticleLifecycleFixture.verify(particles, B173ParticleLifecycle.capture(
                Paths.get(arguments[1]), arguments[0].equals("official"), particles)).canonical();
        byte[] bytes = canonical.getBytes(StandardCharsets.UTF_8);
        Files.write(Paths.get(arguments[3]), bytes);
        System.out.println("WORLDLINE_PARTICLE_ROLE=" + arguments[0]);
        System.out.println("WORLDLINE_PARTICLE_SUBJECTS=" + particles.size());
        System.out.println("WORLDLINE_PARTICLE_SIGNATURE=" + B173RgbaDigest.hash(ByteBuffer.wrap(bytes)));
        System.out.println("WORLDLINE_PARTICLE_PROVENANCE=particles-and-manager:"
                + (arguments[0].equals("official") ? "jars/minecraft.jar" : "minecraft/bin/"));
    }
}
