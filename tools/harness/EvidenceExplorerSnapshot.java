import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Validated read model built only from authoritative tracked evidence sources. */
record EvidenceExplorerSnapshot(Train train, List<Smoke> smokes,
        List<Disposition> dispositions, List<String> orphanProofs) {
    static EvidenceExplorerSnapshot build(Path root) throws Exception {
        Path smokesRoot = root.resolve("smokes");
        Properties qualification = properties(smokesRoot.resolve("qualification.lock"));
        Properties trainValues = properties(smokesRoot.resolve("train-reconciliation.lock"));
        require("5".equals(qualification.getProperty("schema")), "qualification lock schema drift");
        require("1".equals(trainValues.getProperty("schema")), "train lock schema drift");
        List<Smoke> smokes = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Path directory : children(smokesRoot)) {
            Path descriptorPath = directory.resolve("smoke.properties");
            if (!Files.isRegularFile(descriptorPath)) continue;
            Properties descriptor = properties(descriptorPath);
            String id = required(descriptor, "id");
            require(directory.getFileName().toString().equals(id), "smoke directory/id drift: " + id);
            require(ids.add(id), "duplicate smoke id: " + id);
            smokes.add(smoke(smokesRoot, qualification, trainValues, descriptor, id));
        }
        require(number(trainValues, "pin.count") == smokes.size(), "train pin count drift");
        require(number(trainValues, "catalog.count") == smokes.size(), "train catalog count drift");
        List<String> orphans = orphanProofs(smokesRoot, smokes);
        List<Disposition> dispositions = dispositions(root.resolve("coordination/swarm/dispositions"));
        Train train = new Train(required(trainValues, "base"), number(trainValues, "carried.count"),
                number(trainValues, "imported.count"), number(trainValues, "executed.count"),
                number(trainValues, "pending.count"), smokes.size());
        require(train.carried + train.imported + train.executed + train.pending == train.pins,
                "train provenance counts do not partition pins");
        require(count(smokes, "baseline") == train.carried, "train carried count drift");
        require(count(smokes, "milestone") == train.imported, "train imported count drift");
        require(count(smokes, "executed") == train.executed, "train executed count drift");
        require(count(smokes, "pending") == train.pending, "train pending count drift");
        return new EvidenceExplorerSnapshot(train, List.copyOf(smokes),
                List.copyOf(dispositions), List.copyOf(orphans));
    }

    private static Smoke smoke(Path smokesRoot, Properties qualification, Properties train,
            Properties descriptor, String id) throws Exception {
        String prefix = "smoke." + id + ".";
        String fingerprint = required(qualification, prefix + "fingerprint");
        String observation = required(qualification, prefix + "observation_sha256");
        String evidence = required(qualification, prefix + "evidence_sha256");
        String source = required(qualification, prefix + "source");
        require("passed".equals(required(qualification, prefix + "status")), "non-PASS pin: " + id);
        Path proofPath = smokesRoot.resolve("qualification-evidence").resolve(id + ".proof");
        require(Files.isRegularFile(proofPath) && sha256(proofPath).equals(evidence),
                "proof envelope digest drift: " + id);
        Properties proof = properties(proofPath);
        require(id.equals(required(proof, "id")) && fingerprint.equals(required(proof, "fingerprint"))
                && observation.equals(required(proof, "observation.sha256"))
                && source.equals(required(proof, "source")), "proof envelope content drift: " + id);
        require(fingerprint.equals(required(train, prefix + "current_fingerprint")),
                "train fingerprint drift: " + id);
        require(observation.equals(required(train, prefix + "evidence_sha256")),
                "train observation digest drift: " + id);
        String kind = required(train, prefix + "kind");
        require(Set.of("baseline", "milestone", "executed", "pending").contains(kind),
                "unknown train kind: " + kind);
        String prior = train.getProperty(prefix + "prior_fingerprint");
        String head = train.getProperty(prefix + "receipt.head");
        String proofKind = descriptor.getProperty("qualification.proof", "official-cycle");
        require(Set.of("official-cycle", "tooling-cycle").contains(proofKind),
                "unknown qualification proof: " + proofKind);
        return new Smoke(id, proofKind, fingerprint, observation, evidence, source, kind, prior, head);
    }

    private static long count(List<Smoke> smokes, String kind) {
        return smokes.stream().filter(smoke -> kind.equals(smoke.trainKind())).count();
    }

    private static List<String> orphanProofs(Path smokesRoot, List<Smoke> smokes) throws Exception {
        List<String> known = smokes.stream().map(Smoke::id).toList(); List<String> values = new ArrayList<>();
        for (Path path : children(smokesRoot.resolve("qualification-evidence"))) {
            String name = path.getFileName().toString();
            if (Files.isRegularFile(path) && name.endsWith(".proof")
                    && !known.contains(name.substring(0, name.length() - 6))) values.add(name);
        }
        return values;
    }

    private static List<Disposition> dispositions(Path root) throws Exception {
        List<Disposition> values = new ArrayList<>();
        if (!Files.isDirectory(root)) return values;
        for (Path path : children(root)) {
            if (!Files.isRegularFile(path) || !path.getFileName().toString().endsWith(".properties")) continue;
            Properties item = properties(path);
            values.add(new Disposition(required(item, "id"), required(item, "disposition"),
                    item.getProperty("cause", ""), item.getProperty("scar"), item.getProperty("head"),
                    item.getProperty("archive.sha256"), item.getProperty("next.action")));
        }
        return values;
    }

    private static List<Path> children(Path root) throws Exception {
        List<Path> values = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path path : stream) values.add(path);
        }
        values.sort(Comparator.comparing(path -> path.getFileName().toString())); return values;
    }

    private static Properties properties(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing evidence source: " + path);
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }
    static String sha256(Path path) throws Exception { return HexFormat.of().formatHex(
            HexDigest.sha256().digest(Files.readAllBytes(path))); }
    private static int number(Properties values, String key) {
        try { int value = Integer.parseInt(required(values, key)); require(value >= 0, key + " is negative");
            return value; } catch (NumberFormatException error) {
            throw new IllegalArgumentException(key + " is not an integer", error); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key);
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }

    record Train(String base, int carried, int imported, int executed, int pending, int pins) { }
    record Smoke(String id, String proofKind, String fingerprint, String observation,
            String evidence, String source, String trainKind, String priorFingerprint,
            String lastExecutedHead) { }
    record Disposition(String id, String state, String cause, String scar, String head,
            String archiveSha256, String nextAction) { }
}
