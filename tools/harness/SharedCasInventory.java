import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Read-only, sanitized inventory of one immutable shared-directory CAS. */
final class SharedCasInventory {
    private static final String MODE = "shared-directory";
    private static final String DIGEST = "[0-9a-f]{64}";
    private static final String PREFIX = "[0-9a-f]{2}";

    private SharedCasInventory() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: SharedCasInventory");
            Snapshot value = configured(Path.of("").toAbsolutePath().normalize(), System.getenv());
            require(value != null, "shared-directory module CAS mode is not enabled");
            System.out.print(json(value));
        } catch (Exception error) {
            System.err.println("shared CAS inventory failed: " + error.getMessage()); System.exit(1);
        }
    }

    static Snapshot configured(Path repository, Map<String, String> environment) throws Exception {
        String mode = environment.get("WORLDLINE_MODULE_CAS_MODE");
        if (!MODE.equals(mode)) return null;
        Path shared = Path.of(required(environment, "WORLDLINE_MODULE_CAS_DIRECTORY"));
        Path indexPath = Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX"));
        Path trustPath = Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX_TRUST"));
        ModuleCacheIndexTrust.Envelope trust = ModuleCacheIndexTrust.verify(indexPath, trustPath,
                required(environment, "WORLDLINE_MODULE_CAS_PUBLIC_KEY"),
                required(environment, "WORLDLINE_MODULE_CAS_CHANNEL"));
        Map<String, Reference> references = new HashMap<>();
        add(references, new Reference(trust.indexSha256(), trust.indexSize(),
                "signed-module-index", true));
        for (ModuleCacheIndex.Entry entry : ModuleCacheIndex.read(indexPath).entries()) add(references,
                new Reference(entry.blobSha256(), entry.blobSize(), "module-bundle", true));
        ArtifactTransportPolicy policy = ArtifactTransportPolicy.load(repository);
        for (ArtifactTransportPolicy.Candidate proof : policy.eligibleCandidates(
                SmokeTrackedFiles.read(repository))) add(references, new Reference(
                proof.sha256(), proof.size(), "tracked-smoke-pin", false));
        return inspect(shared, references);
    }

    static Snapshot inspect(Path shared, Map<String, Reference> references) throws Exception {
        Path root = shared.toAbsolutePath().normalize();
        require(shared.isAbsolute() && shared.normalize().equals(shared),
                "shared CAS directory is not absolute and normalized");
        require(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(root),
                "shared CAS directory is absent or linked");
        Counter counter = new Counter(); List<ObjectEntry> objects = new ArrayList<>();
        scanRoot(root, references, counter, objects);
        objects.sort(Comparator.comparing(ObjectEntry::sha256));
        Set<String> present = new HashSet<>();
        for (ObjectEntry object : objects) if ("verified".equals(object.state)) present.add(object.sha256);
        int missing = (int) references.values().stream().filter(Reference::required)
                .filter(value -> !present.contains(value.sha256)).count();
        return new Snapshot(objects.size(), counter.verified, counter.modules, counter.indexes,
                counter.proofs, counter.orphans, counter.corrupt, counter.malformed, missing,
                List.copyOf(objects));
    }

    static String json(Snapshot value) {
        StringBuilder out = new StringBuilder("{\n  \"schema\":\"worldline.shared-cas-inventory.v1\",")
                .append("\n  \"verdict_authority\":false,\n  \"paths_included\":false,")
                .append("\n  \"total_objects\":").append(value.totalObjects)
                .append(",\n  \"verified_objects\":").append(value.verifiedObjects)
                .append(",\n  \"module_objects\":").append(value.moduleObjects)
                .append(",\n  \"index_objects\":").append(value.indexObjects)
                .append(",\n  \"tracked_proof_objects\":").append(value.trackedProofObjects)
                .append(",\n  \"orphan_objects\":").append(value.orphanObjects)
                .append(",\n  \"corrupt_objects\":").append(value.corruptObjects)
                .append(",\n  \"malformed_entries\":").append(value.malformedEntries)
                .append(",\n  \"missing_signed_references\":").append(value.missingSignedReferences)
                .append(",\n  \"objects\":[");
        for (int index = 0; index < value.objects.size(); index++) {
            ObjectEntry item = value.objects.get(index);
            if (index > 0) out.append(',');
            out.append("\n    {\"sha256\":\"").append(item.sha256).append("\",\"size\":")
                    .append(item.size).append(",\"class\":\"").append(item.classification)
                    .append("\",\"state\":\"").append(item.state).append("\"}");
        }
        if (!value.objects.isEmpty()) out.append('\n').append("  ");
        return out.append("]\n}\n").toString();
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-shared-cas-inventory-")
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(root.resolve("quality")); Files.copy(Path.of(
                    "quality/artifact-transport.properties"), root.resolve(
                    "quality/artifact-transport.properties"));
            Path entry = root.resolve("entry"), classFile = entry.resolve("fixture/Example.class");
            Files.createDirectories(classFile.getParent()); Files.writeString(
                    entry.resolve(".complete"), "a".repeat(64) + "\n", StandardCharsets.UTF_8);
            Files.write(classFile, new byte[] {-54, -2, -70, -66, 0, 1});
            ModuleCachePublication.Prepared prepared = ModuleCachePublication.prepare(root,
                    root.resolve(".worldline/cas-shadow/shared"), List.of(
                            new ModuleCachePublication.Module("fixture", entry)), List.of());
            Path shared = root.resolve("shared"); ModuleCacheSharedDirectory.publish(shared, prepared);
            KeyPair keys = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            Path trustPath = root.resolve("index.trust"); ModuleCacheIndexTrust.Envelope trust =
                    ModuleCacheIndexTrust.sign(prepared.index().path(), "fixture", keys, trustPath);
            Map<String, Reference> references = new HashMap<>();
            add(references, new Reference(trust.indexSha256(), trust.indexSize(),
                    "signed-module-index", true));
            ArtifactTransportPolicy.Candidate module = prepared.modules().get(0).candidate();
            add(references, new Reference(module.sha256(), module.size(), "module-bundle", true));
            add(references, new Reference("d".repeat(64), 4L, "module-bundle", true));
            publishFixture(shared, "orphan\n".getBytes(StandardCharsets.UTF_8));
            Path corrupt = object(shared, "c".repeat(64)); Files.createDirectories(corrupt.getParent());
            Files.writeString(corrupt, "bad", StandardCharsets.UTF_8);
            Files.writeString(shared.resolve("objects/sha256/not-a-prefix"), "bad");
            Snapshot value = inspect(shared, references); String json = json(value);
            require(value.totalObjects == 4 && value.verifiedObjects == 3 && value.moduleObjects == 1
                    && value.indexObjects == 1 && value.orphanObjects == 1 && value.corruptObjects == 1
                    && value.malformedEntries == 1 && value.missingSignedReferences == 1,
                    "shared CAS inventory classification drifted");
            require(!json.contains(root.toString()) && json.contains("\"paths_included\":false"),
                    "shared CAS inventory leaked a path");
            Path badLock = shared.resolve("locks/sha256/zz/bad.lock");
            Files.createDirectories(badLock.getParent()); Files.writeString(badLock, "bad");
            require(inspect(shared, references).malformedEntries == 2,
                    "malformed shared CAS publication lock was hidden");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  shared CAS inventory self-test: passed");
    }

    private static void scanRoot(Path root, Map<String, Reference> references, Counter counter,
            List<ObjectEntry> objects) throws Exception {
        List<Path> children = children(root);
        for (Path child : children) if (!Set.of("objects", "locks").contains(
                child.getFileName().toString())) counter.malformed++;
        scanLocks(root, counter);
        Path objectRoot = root.resolve("objects"); if (!Files.exists(objectRoot)) return;
        if (!directory(objectRoot)) { counter.malformed++; return; }
        for (Path child : children(objectRoot)) if (!child.getFileName().toString().equals("sha256"))
            counter.malformed++;
        Path shaRoot = objectRoot.resolve("sha256"); if (!Files.exists(shaRoot)) return;
        if (!directory(shaRoot)) { counter.malformed++; return; }
        for (Path prefix : children(shaRoot)) {
            String head = prefix.getFileName().toString();
            if (!head.matches(PREFIX) || !directory(prefix)) { counter.malformed++; continue; }
            for (Path path : children(prefix)) inspectObject(path, head, references, counter, objects);
        }
    }

    private static void scanLocks(Path root, Counter counter) throws Exception {
        Path locks = root.resolve("locks"); if (!Files.exists(locks)) return;
        if (!directory(locks)) { counter.malformed++; return; }
        for (Path child : children(locks)) if (!child.getFileName().toString().equals("sha256"))
            counter.malformed++;
        Path shaRoot = locks.resolve("sha256"); if (!Files.exists(shaRoot)) return;
        if (!directory(shaRoot)) { counter.malformed++; return; }
        for (Path prefix : children(shaRoot)) {
            String head = prefix.getFileName().toString();
            if (!head.matches(PREFIX) || !directory(prefix)) { counter.malformed++; continue; }
            for (Path lock : children(prefix)) {
                String tail = lock.getFileName().toString();
                if (!tail.matches("[0-9a-f]{62}[.]lock")
                        || !Files.isRegularFile(lock, LinkOption.NOFOLLOW_LINKS)
                        || Files.isSymbolicLink(lock) || Files.size(lock) != 0L) counter.malformed++;
            }
        }
    }

    private static void inspectObject(Path path, String prefix, Map<String, Reference> references,
            Counter counter, List<ObjectEntry> objects) throws Exception {
        String tail = path.getFileName().toString(), digest = prefix + tail;
        if (!tail.matches("[0-9a-f]{62}") || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(path)) { counter.malformed++; return; }
        long size = Files.size(path); Reference reference = references.get(digest);
        String classification = reference == null ? "orphan" : reference.classification;
        boolean valid = sha256(path).equals(digest) && (reference == null || reference.size == size);
        if (valid) {
            counter.verified++;
            if ("module-bundle".equals(classification)) counter.modules++;
            else if ("signed-module-index".equals(classification)) counter.indexes++;
            else if ("tracked-smoke-pin".equals(classification)) counter.proofs++;
            else counter.orphans++;
        } else counter.corrupt++;
        objects.add(new ObjectEntry(digest, size, classification, valid ? "verified" : "corrupt"));
    }

    private static void publishFixture(Path root, byte[] bytes) throws Exception {
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        Path target = object(root, digest); Files.createDirectories(target.getParent()); Files.write(target, bytes);
    }
    private static Path object(Path root, String digest) { return root.resolve("objects/sha256")
            .resolve(digest.substring(0, 2)).resolve(digest.substring(2)); }
    private static List<Path> children(Path path) throws Exception {
        List<Path> values = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path value : stream) values.add(value);
        }
        values.sort(Comparator.comparing(value -> value.getFileName().toString())); return values;
    }
    private static boolean directory(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path);
    }
    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static void add(Map<String, Reference> references, Reference value) {
        require(value.sha256.matches(DIGEST) && value.size > 0L, "invalid shared CAS reference");
        Reference prior = references.putIfAbsent(value.sha256, value);
        require(prior == null || prior.size == value.size
                && prior.classification.equals(value.classification)
                && prior.required == value.required, "conflicting shared CAS reference");
    }
    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name); require(value != null && !value.isBlank(), name + " is required");
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Snapshot(int totalObjects, int verifiedObjects, int moduleObjects, int indexObjects,
            int trackedProofObjects, int orphanObjects, int corruptObjects, int malformedEntries,
            int missingSignedReferences, List<ObjectEntry> objects) { }
    record ObjectEntry(String sha256, long size, String classification, String state) { }
    record Reference(String sha256, long size, String classification, boolean required) { }
    private static final class Counter { int verified, modules, indexes, proofs, orphans, corrupt, malformed; }
}
