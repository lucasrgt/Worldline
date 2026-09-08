package worldline.smoke.b173entitystate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import worldline.api.RuntimeSnapshot;
import worldline.b173.B173EntityStateRender;
import worldline.testkit.EntityVisualStateFixture;

/** One fresh native process; diagnostics precede strict public acceptance. */
public final class EntityVisualStateSmoke {
    private EntityVisualStateSmoke() { }
    public static void main(String[] a) throws Exception {
        if (a.length != 4 || !(a[0].equals("mapped") || a[0].equals("official"))) {
            throw new IllegalArgumentException("role mappings subjects output required");
        }
        boolean official = a[0].equals("official");
        Path output = Paths.get(a[3]);
        Map<String, String> values = B173EntityStateRender.observe(Paths.get(a[1]), official,
                output.resolveSibling(output.getFileName()+"-frames"));
        Files.write(output.resolveSibling(output.getFileName()+".observed"),
                values.toString().getBytes(StandardCharsets.UTF_8));
        byte[] canonical = EntityVisualStateFixture.verify(values).canonical().getBytes(StandardCharsets.UTF_8);
        Files.write(output, canonical);
        System.out.println("WORLDLINE_ENTITY_RENDER_ROLE=" + a[0]);
        System.out.println("WORLDLINE_ENTITY_RENDER_SUBJECTS=5");
        System.out.println("WORLDLINE_ENTITY_RENDER_SIGNATURE=" + RuntimeSnapshot.of(canonical).sha256());
        System.out.println("WORLDLINE_ENTITY_RENDER_PROVENANCE="
                + (official ? "jars/minecraft.jar" : "minecraft/bin/"));
    }
}
