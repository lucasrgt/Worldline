package worldline.atlas;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Fail-closed agreement between milestone descriptors, Census deltas, and compiled Atlas. */
public final class AtlasSynchronization {
    private AtlasSynchronization() {}

    public static void validate(Path root, String milestone) throws IOException {
        if (root == null || milestone == null || milestone.isEmpty()) {
            throw new IllegalArgumentException("milestone");
        }
        Path directory = root.resolve("smokes").resolve(milestone);
        Properties fields = properties(directory.resolve("smoke.properties"));
        AtlasStore store = AtlasStore.standard(root);
        validate(root, store, milestone, directory, fields);
    }

    private static void validate(Path root, AtlasStore store, String milestone, Path directory,
            Properties fields) throws IOException {
        validateBehavior(store, milestone, fields);
        Path delta = directory.resolve("census-delta.tsv");
        if (Files.isRegularFile(delta)) validateDelta(store, milestone, delta);
    }

    public static void validateAll(Path root) throws IOException {
        AtlasStore store = AtlasStore.standard(root);
        AtlasTaxonomy.validate(store);
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(root.resolve("smokes"))) {
            for (Path directory : dirs) {
                if (Files.isRegularFile(directory.resolve("smoke.properties"))) {
                    String milestone = directory.getFileName().toString();
                    validate(root, store, milestone, directory,
                            properties(directory.resolve("smoke.properties")));
                }
            }
        }
    }

    private static void validateBehavior(AtlasStore store, String milestone, Properties fields) {
        String behavior = fields.getProperty("behavior", "").trim();
        if (behavior.isEmpty()) return;
        AtlasRecord record = store.get("atlas.scenario." + behavior);
        require(record.refs().contains("atlas.experiment." + milestone),
                "Atlas behavior lacks milestone ref " + milestone);
        String signature = fields.getProperty("expected.signature", "").trim();
        if (!signature.isEmpty()) require(record.evidence().contains("expected.signature=" + signature),
                "Atlas behavior lacks signature " + milestone);
    }

    private static void validateDelta(AtlasStore store, String milestone, Path delta)
            throws IOException {
        List<String> lines = Files.readAllLines(delta, StandardCharsets.UTF_8);
        require(!lines.isEmpty(), "empty census delta " + milestone);
        String[] header = lines.get(0).split("\t", -1);
        int subject = column(header, "subject_id"), template = column(header, "template_id");
        int status = column(header, "status"), signature = column(header, "signature");
        for (int line = 1; line < lines.size(); line++) {
            if (lines.get(line).trim().isEmpty() || lines.get(line).startsWith("#")) continue;
            String[] values = lines.get(line).split("\t", -1);
            require(values.length == header.length, "census delta width " + milestone);
            String block = values[subject].substring(values[subject].lastIndexOf('/') + 1);
            AtlasRecord record = store.get("atlas.claim.block-" + block + "." + values[template]);
            require(statusAgrees(values[status], record.status()),
                    "Atlas claim status drift " + record.id());
            require(record.evidence().contains("expected.signature=" + values[signature]),
                    "Atlas claim signature drift " + record.id());
            require(record.refs().contains("atlas.experiment." + milestone),
                    "Atlas claim milestone drift " + record.id());
        }
    }

    private static boolean statusAgrees(String evidenceStatus, String currentStatus) {
        return evidenceStatus.equals(currentStatus)
                || (AtlasStatus.PARTIAL.equals(evidenceStatus)
                        && AtlasStatus.VERIFIED.equals(currentStatus));
    }

    private static int column(String[] header, String name) {
        for (int index = 0; index < header.length; index++) {
            if (name.equals(header[index])) return index;
        }
        throw new IllegalArgumentException("missing census column " + name);
    }

    private static Properties properties(Path path) throws IOException {
        Properties result = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
