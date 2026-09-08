import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** One milestone joins independently qualified native gameplay and native visual state families. */
public final class EntityStateConformanceCycle {
    private static final String ID = "b173-entity-state-conformance-cycle";
    private EntityStateConformanceCycle() {
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 0 && (args.length != 1 || !args[0].equals(ID))) {
            throw new IllegalArgumentException("unexpected entity context milestone");
        }
        Path root = Path.of("").toAbsolutePath().normalize();
        Properties config = new Properties();
        SmokeSupport.load(root.resolve("smokes/" + ID + "/smoke.properties"), config);
        Path build = root.resolve(".worldline/smokes/" + ID);
        StringBuilder joined = new StringBuilder();
        for (String phase : new String[] {"gameplay", "render"}) {
            Properties selected = new Properties();
            selected.putAll(config);
            for (String key : config.stringPropertyNames()) {
                if (key.startsWith(phase + '.')) selected.setProperty(key.substring(phase.length() + 1),
                        config.getProperty(key));
            }
            NativeDifferentialCapture.execute(root, ID, selected);
            String evidence = Files.readString(build.resolve("evidence.txt"), StandardCharsets.UTF_8);
            Files.writeString(build.resolve(phase + "-evidence.txt"), evidence, StandardCharsets.UTF_8);
            joined.append(evidence);
            for (String role : new String[] {"mapped", "official"}) for (int attempt = 1; attempt <= 2; attempt++) {
                for (String suffix : new String[] {".properties", ".log", ".properties.observed"}) {
                    Path source = build.resolve(role + '-' + attempt + suffix);
                    Files.copy(source, build.resolve(phase + '-' + source.getFileName()),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        Files.writeString(build.resolve("evidence.txt"), joined, StandardCharsets.UTF_8);
        String signature = SmokeSupport.digest(build.resolve("evidence.txt"), "SHA-256");
        if (!signature.equals(SmokeSupport.value(config, "expected.signature"))) {
            throw new IllegalStateException("combined entity context signature differs: " + signature);
        }
        System.out.println("WORLDLINE_ENTITY_STATE_SET=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_ENTITY_STATE_TRACE=" + SmokeSupport.value(config, "expected.trace"));
        System.out.println("WORLDLINE_ENTITY_STATE_SIGNATURE=" + signature);
    }
}
