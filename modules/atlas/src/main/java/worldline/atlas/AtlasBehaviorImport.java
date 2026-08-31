package worldline.atlas;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import worldline.api.WorldlineBehavior;

/** Materializes every public Worldline behavior as a real Atlas scenario record. */
final class AtlasBehaviorImport {
    private AtlasBehaviorImport() {}

    static List<AtlasRecord> load(Path root) throws IOException {
        Map<String, List<Proof>> byBehavior = proofs(root);
        Properties lock = properties(root.resolve("smokes/qualification.lock"));
        List<AtlasRecord> result = new ArrayList<AtlasRecord>();
        for (WorldlineBehavior behavior : WorldlineBehavior.all().values()) {
            List<Proof> proofs = byBehavior.get(behavior.token());
            if (proofs == null) proofs = Collections.emptyList();
            result.add(record(behavior, proofs, lock));
        }
        return Collections.unmodifiableList(result);
    }

    private static AtlasRecord record(WorldlineBehavior behavior, List<Proof> proofs,
            Properties lock) {
        List<String> evidence = new ArrayList<String>();
        List<String> refs = new ArrayList<String>();
        boolean verified = false;
        for (Proof proof : proofs) {
            refs.add("atlas.experiment." + proof.id);
            if (!proof.signature.isEmpty()) evidence.add("expected.signature=" + proof.signature);
            String prefix = "smoke." + proof.id + ".";
            if ("passed".equals(lock.getProperty(prefix + "status"))) {
                String observation = lock.getProperty(prefix + "observation_sha256", "").trim();
                if (!observation.isEmpty()) evidence.add("qualification=" + observation);
                verified = true;
            }
        }
        refs.add("atlas.subsystem." + subsystem(behavior.family()));
        String status = verified ? AtlasStatus.VERIFIED
                : proofs.isEmpty() ? AtlasStatus.UNKNOWN : AtlasStatus.OBSERVATIONAL;
        return AtlasRecord.of(behavior.atlasId(), AtlasKind.SCENARIO, status,
                AtlasSchema.WORLDLINE, AtlasSchema.SCOPE, subject(behavior.subject()),
                "family=" + behavior.family(), 0, evidence, refs);
    }

    private static Map<String, List<Proof>> proofs(Path root) throws IOException {
        Map<String, List<Proof>> result = new LinkedHashMap<String, List<Proof>>();
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(root.resolve("smokes"))) {
            for (Path dir : dirs) {
                Path file = dir.resolve("smoke.properties");
                if (!Files.isRegularFile(file)) continue;
                Properties fields = properties(file);
                String behavior = fields.getProperty("behavior", "").trim();
                if (behavior.isEmpty()) continue;
                List<Proof> list = result.get(behavior);
                if (list == null) {
                    list = new ArrayList<Proof>(); result.put(behavior, list);
                }
                list.add(new Proof(dir.getFileName().toString(),
                        fields.getProperty("expected.signature", "").trim()));
            }
        }
        return result;
    }

    private static String subsystem(String family) {
        if ("hostile".equals(family)) return "mob-ai";
        if ("player".equals(family)) return "player";
        if ("item".equals(family)) return "inventory";
        if ("redstone".equals(family)) return "redstone";
        if ("vehicle".equals(family)) return "entities";
        if ("entity".equals(family)) return "entities";
        return "block-ticks";
    }

    private static String subject(String value) {
        return value.replace("=", " equals ").replace('\n', ' ').replace('\r', ' ');
    }

    private static Properties properties(Path path) throws IOException {
        Properties result = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private static final class Proof {
        final String id, signature;
        Proof(String id, String signature) { this.id = id; this.signature = signature; }
    }
}
