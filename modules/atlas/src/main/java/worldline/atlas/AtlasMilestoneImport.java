package worldline.atlas;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Indexes explicitly scoped smoke evidence, CYCLE docs, and tracked symbols maps. */
final class AtlasMilestoneImport {
    private static final Pattern MILESTONE = Pattern.compile("^m(\\d+)(?:-.*)?$");

    private AtlasMilestoneImport() {}

    static List<AtlasRecord> load(Path root) throws IOException {
        if (root == null) throw new NullPointerException("root");
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        Path smokes = root.resolve("smokes");
        if (!Files.isDirectory(smokes)) throw new IllegalArgumentException("missing smokes");
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(smokes)) {
            for (Path dir : dirs) {
                if (!Files.isDirectory(dir)) continue;
                Path properties = dir.resolve("smoke.properties");
                if (Files.isRegularFile(properties)) records.add(experiment(root, dir, properties));
                Path symbols = dir.resolve("symbols.map");
                if (Files.isRegularFile(symbols)) records.add(symbolsMap(dir, symbols));
            }
        }
        Collections.sort(records, new Comparator<AtlasRecord>() {
            @Override public int compare(AtlasRecord left, AtlasRecord right) {
                return left.id().compareTo(right.id());
            }
        });
        return Collections.unmodifiableList(records);
    }

    private static AtlasRecord experiment(Path root, Path dir, Path properties) throws IOException {
        Properties fields = loadProperties(properties);
        String folder = dir.getFileName().toString();
        String id = fields.getProperty("id", folder).trim();
        if (id.isEmpty()) id = folder;
        if (!folder.equals(id)) throw new IllegalArgumentException("smoke id mismatch " + folder);
        String signature = fields.getProperty("expected.signature", "").trim();
        Path map = dir.resolve("MAP.md");
        String mapText = Files.isRegularFile(map)
                ? new String(Files.readAllBytes(map), StandardCharsets.UTF_8) : "";
        if (!signature.isEmpty() && !mapText.toLowerCase().contains(signature.toLowerCase())) {
            throw new IllegalArgumentException("MAP does not freeze expected.signature " + id);
        }
        List<String> evidence = new ArrayList<String>();
        if (!signature.isEmpty()) {
            evidence.add("expected.signature=" + signature.toLowerCase());
            evidence.add("map-signature-freeze");
        }
        if (!mapText.isEmpty()) evidence.add(AtlasHashes.sha256(mapText));
        if (hasNonclaims(mapText)) evidence.add("map-nonclaims");
        if (evidence.isEmpty()) evidence.add("smoke.properties");
        List<String> refs = new ArrayList<String>();
        addSubsystems(refs, required(fields, "atlas.subsystems", id));
        addRefs(refs, fields.getProperty("atlas.roles", ""), "atlas.role.");
        addRefs(refs, fields.getProperty("atlas.boundaries", ""), "atlas.boundary.");
        String cycle = cyclePath(id);
        if (!cycle.isEmpty() && Files.isRegularFile(root.resolve(cycle))) {
            evidence.add(cycle.replace('\\', '/'));
        }
        return AtlasRecord.of("atlas.experiment." + id, AtlasKind.EXPERIMENT,
                AtlasStatus.OBSERVATIONAL, artifact(fields, id), AtlasSchema.SCOPE, id, "", 0,
                evidence, refs);
    }

    private static AtlasRecord symbolsMap(Path dir, Path symbols) throws IOException {
        String folder = dir.getFileName().toString();
        String hash = AtlasHashes.sha256(Files.readAllBytes(symbols));
        List<String> evidence = Collections.singletonList(hash);
        List<String> refs = new ArrayList<String>();
        refs.add("atlas.subsystem.mappings");
        refs.add("atlas.subsystem.tick-lifecycle");
        return AtlasRecord.of("atlas.experiment.symbols-map." + folder, AtlasKind.EXPERIMENT,
                AtlasStatus.OBSERVATIONAL, AtlasSchema.CLIENT, AtlasSchema.SCOPE,
                "smokes/" + folder + "/symbols.map", "", 0, evidence, refs);
    }

    private static String required(Properties fields, String key, String id) {
        String value = fields.getProperty(key, "").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("missing " + key + " " + id);
        return value;
    }

    private static String artifact(Properties fields, String id) {
        String value = required(fields, "atlas.artifact", id);
        if ("client".equals(value)) return AtlasSchema.CLIENT;
        if ("server".equals(value)) return AtlasSchema.SERVER;
        if ("worldline".equals(value)) return AtlasSchema.WORLDLINE;
        throw new IllegalArgumentException("unknown atlas.artifact " + value + " " + id);
    }

    private static void addSubsystems(List<String> refs, String value) {
        for (String subsystem : tokens(value)) {
            if (!AtlasSubsystems.known(subsystem)) {
                throw new IllegalArgumentException("unknown atlas subsystem " + subsystem);
            }
            refs.add("atlas.subsystem." + subsystem);
        }
    }

    private static void addRefs(List<String> refs, String value, String prefix) {
        for (String token : tokens(value)) refs.add(prefix + token);
    }

    private static List<String> tokens(String value) {
        List<String> tokens = new ArrayList<String>();
        for (String token : value.trim().split("[,\\s]+")) {
            if (!token.isEmpty()) tokens.add(token);
        }
        return tokens;
    }

    private static boolean hasNonclaims(String text) {
        return text.contains("Nonclaims:") || text.contains("Non-claims:")
                || text.toLowerCase().contains("does not claim");
    }

    private static String cyclePath(String id) {
        Matcher matcher = MILESTONE.matcher(id);
        if (!matcher.matches()) return "";
        return "docs/M" + matcher.group(1) + "_CYCLE.md";
    }

    private static Properties loadProperties(Path path) throws IOException {
        Properties fields = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            fields.load(reader);
        }
        return fields;
    }
}
