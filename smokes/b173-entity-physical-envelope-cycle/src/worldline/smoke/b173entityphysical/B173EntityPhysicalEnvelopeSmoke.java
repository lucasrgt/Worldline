package worldline.smoke.b173entityphysical;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

/** Renders the frozen subsystem signal from canonical public TestKit evidence. */
public final class B173EntityPhysicalEnvelopeSmoke {
    private B173EntityPhysicalEnvelopeSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) throw new IllegalArgumentException("usage: evidence file");
        Path file = Paths.get(arguments[0]);
        byte[] bytes = Files.readAllBytes(file);
        String canonical = new String(bytes, StandardCharsets.UTF_8);
        require(canonical.startsWith(
                "schema=worldline.entity-physical-envelope-evidence.v1\nclaims=23\n"),
                "entity physical envelope evidence framing drifted");
        require(occurrences(canonical, "#collision-shape|ARCHETYPE") == 13
                && occurrences(canonical, "#collision-shape|SINGULAR") == 10,
                "entity physical envelope layer routing drifted");
        require(occurrences(canonical, "|centered=true|vertical=true") == 23,
                "entity physical envelope geometry is incomplete");
        require(!canonical.contains("entity/048#collision-shape"),
                "abstract EntityLiving was materialized dishonestly");
        String evidence = sha(bytes);
        String signal = "family=entity-physical-envelope,subjects=23,claims=23,"
                + "layers=ARCHETYPEx13+SINGULARx10,abstract=entity/048:NOT_APPLICABLE,"
                + "deterministic=true,evidence=" + evidence;
        String trace = "v1|client=official-mapped-b1.7.3|family=entity-physical-envelope|"
                + "actions=construct-canonical-entities+normalize-slime+position+inspect-aabb+"
                + "inspect-contact-dispositions|oracle=public-entity-physical-envelope-evidence|"
                + "evidence=" + evidence;
        System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_SET=" + signal);
        System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_SIGNATURE="
                + sha(trace.getBytes(StandardCharsets.UTF_8)));
    }

    private static int occurrences(String value, String marker) {
        int count = 0, start = 0;
        while ((start = value.indexOf(marker, start)) >= 0) {
            count++;
            start += marker.length();
        }
        return count;
    }

    private static String sha(byte[] value) throws Exception {
        StringBuilder result = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(value)) {
            result.append(String.format("%02x", item & 255));
        }
        return result.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
