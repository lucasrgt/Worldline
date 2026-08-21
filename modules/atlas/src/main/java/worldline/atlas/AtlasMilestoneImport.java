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

/** Indexes smoke.properties, MAP.md SHA-256 lines, CYCLE docs, and tracked symbols.map files. */
final class AtlasMilestoneImport {
    private static final Pattern MAP_SHA = Pattern.compile("(?m)^SHA-256:\\s*`([a-fA-F0-9]{64})`");
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
        String mapHash = mapSha(dir.resolve("MAP.md"));
        if (!signature.isEmpty() && !mapHash.isEmpty()
                && !signature.equalsIgnoreCase(mapHash)) {
            throw new IllegalArgumentException("MAP SHA-256 mismatch " + id);
        }
        List<String> evidence = new ArrayList<String>();
        if (!signature.isEmpty()) {
            evidence.add("expected.signature=" + signature.toLowerCase());
        }
        if (!mapHash.isEmpty()) evidence.add(mapHash.toLowerCase());
        if (hasNonclaims(dir.resolve("MAP.md"))) evidence.add("map-nonclaims");
        if (evidence.isEmpty()) evidence.add("smoke.properties");
        List<String> refs = new ArrayList<String>();
        for (String subsystem : AtlasSubsystems.forExperiment(id)) {
            refs.add("atlas.subsystem." + subsystem);
        }
        String cycle = cyclePath(id);
        if (!cycle.isEmpty() && Files.isRegularFile(root.resolve(cycle))) {
            evidence.add(cycle.replace('\\', '/'));
        }
        return AtlasRecord.of("atlas.experiment." + id, AtlasKind.EXPERIMENT,
                AtlasStatus.OBSERVATIONAL, AtlasSchema.CLIENT, AtlasSchema.SCOPE, id, "", 0,
                evidence, refs);
    }

    private static AtlasRecord symbolsMap(Path dir, Path symbols) throws IOException {
        String folder = dir.getFileName().toString();
        String hash = AtlasHashes.sha256(Files.readAllBytes(symbols));
        List<String> evidence = Collections.singletonList(hash);
        List<String> refs = new ArrayList<String>();
        for (String subsystem : AtlasSubsystems.forExperiment("symbols-map." + folder)) {
            refs.add("atlas.subsystem." + subsystem);
        }
        return AtlasRecord.of("atlas.experiment.symbols-map." + folder, AtlasKind.EXPERIMENT,
                AtlasStatus.OBSERVATIONAL, AtlasSchema.CLIENT, AtlasSchema.SCOPE,
                "smokes/" + folder + "/symbols.map", "", 0, evidence, refs);
    }

    private static String mapSha(Path map) throws IOException {
        if (!Files.isRegularFile(map)) return "";
        String text = new String(Files.readAllBytes(map), StandardCharsets.UTF_8);
        Matcher matcher = MAP_SHA.matcher(text);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static boolean hasNonclaims(Path map) throws IOException {
        if (!Files.isRegularFile(map)) return false;
        String text = new String(Files.readAllBytes(map), StandardCharsets.UTF_8);
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
