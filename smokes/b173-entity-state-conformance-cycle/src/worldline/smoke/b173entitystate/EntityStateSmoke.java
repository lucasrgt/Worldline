package worldline.smoke.b173entitystate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import worldline.api.RuntimeSnapshot;
import worldline.b173server.B173EntityStateConformance;
import worldline.testkit.EntityStateFixture;

/** One fresh native process; diagnostics precede strict public acceptance. */
public final class EntityStateSmoke {
    private EntityStateSmoke() { }
    public static void main(String[] a) throws Exception {
        if (a.length != 4 || !(a[0].equals("mapped") || a[0].equals("official"))) {
            throw new IllegalArgumentException("role mappings subjects output required");
        }
        boolean official = a[0].equals("official"); Path output = Paths.get(a[3]);
        Map<String, Long> values = B173EntityStateConformance.observe(Paths.get(a[1]), official);
        Files.write(output.resolveSibling(output.getFileName()+".observed"),
                values.toString().getBytes(StandardCharsets.UTF_8));
        byte[] canonical = EntityStateFixture.verify(values).canonical().getBytes(StandardCharsets.UTF_8);
        Files.write(output, canonical);
        System.out.println("WORLDLINE_ENTITY_GAMEPLAY_ROLE=" + a[0]);
        System.out.println("WORLDLINE_ENTITY_GAMEPLAY_SUBJECTS=5");
        System.out.println("WORLDLINE_ENTITY_GAMEPLAY_SIGNATURE=" + RuntimeSnapshot.of(canonical).sha256());
        System.out.println("WORLDLINE_ENTITY_GAMEPLAY_PROVENANCE="
                + (official ? "jars/minecraft_server.jar" : "minecraft_server/bin/"));
    }
}
