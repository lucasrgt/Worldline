package worldline.smoke.b173entitypersistence;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

/** Renders the frozen subsystem signal from canonical public TestKit evidence. */
public final class B173EntityPersistenceSmoke {
    private B173EntityPersistenceSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) throw new IllegalArgumentException("usage: evidence file");
        Path file = Paths.get(arguments[0]);
        byte[] bytes = Files.readAllBytes(file);
        String canonical = new String(bytes, StandardCharsets.UTF_8);
        require(canonical.startsWith(
                "schema=worldline.entity-persistence-evidence.v1\nclaims=23\n"),
                "entity persistence evidence framing drifted");
        require(occurrences(canonical, "#save-reload|UNIVERSAL") == 23,
                "entity persistence universal routing drifted");
        require(occurrences(canonical, "|reconstructed=true|type-exact=true|"
                + "common-state-exact=true|nbt-exact=true|") == 23,
                "entity persistence round-trip is incomplete");
        require(!canonical.contains("entity/048#save-reload"),
                "abstract EntityLiving was persisted dishonestly");
        String evidence = sha(bytes);
        String signal = "family=entity-persistence-envelope,subjects=23,claims=23,"
                + "layers=UNIVERSALx23,abstract=entity/048:NOT_APPLICABLE,"
                + "reconstructed=23,type-exact=23,nbt-exact=23,deterministic=true,evidence="
                + evidence;
        String trace = "v1|client=official-mapped-b1.7.3|family=entity-persistence-envelope|"
                + "actions=seed-common-state+seed-native-payload+write-nbt+"
                + "entity-list-reconstruct+reserialize|oracle=public-entity-persistence-evidence|"
                + "evidence=" + evidence;
        System.out.println("WORLDLINE_B173_ENTITY_PERSISTENCE_SET=" + signal);
        System.out.println("WORLDLINE_B173_ENTITY_PERSISTENCE_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_ENTITY_PERSISTENCE_SIGNATURE="
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
