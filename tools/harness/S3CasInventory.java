import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Read-only authenticated audit of the objects named by one signed S3 module index. */
final class S3CasInventory {
    private static final String MODE = "s3";

    private S3CasInventory() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: S3CasInventory");
            Snapshot value = configured(Path.of("").toAbsolutePath().normalize(), System.getenv());
            require(value != null, "S3 module CAS mode is not enabled");
            System.out.print(json(value));
        } catch (Exception error) {
            System.err.println("S3 CAS inventory failed: " + error.getMessage()); System.exit(1);
        }
    }

    static Snapshot configured(Path repository, Map<String, String> environment) throws Exception {
        if (!MODE.equals(environment.get("WORLDLINE_MODULE_CAS_MODE"))) return null;
        Path indexPath = Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX"));
        Path trustPath = Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX_TRUST"));
        ModuleCacheIndexTrust.Envelope trust = ModuleCacheIndexTrust.verify(indexPath, trustPath,
                required(environment, "WORLDLINE_MODULE_CAS_PUBLIC_KEY"),
                required(environment, "WORLDLINE_MODULE_CAS_CHANNEL"));
        Map<String, Reference> references = new TreeMap<>();
        add(references, new Reference(trust.indexSha256(), trust.indexSize(),
                "signed-module-index"));
        for (ModuleCacheIndex.Entry entry : ModuleCacheIndex.read(indexPath).entries()) add(references,
                new Reference(entry.blobSha256(), entry.blobSize(), "module-bundle"));
        Path downloads = Files.createTempDirectory("worldline-s3-inventory-")
                .toAbsolutePath().normalize();
        try {
            S3ObjectTransport transport = new S3ObjectTransport(
                    ModuleCacheS3Config.from(environment), Clock.systemUTC(), downloads);
            return inspect(transport, references);
        } finally { SafeTreeDelete.delete(downloads); }
    }

    static Snapshot inspect(S3ObjectTransport transport, Map<String, Reference> references)
            throws Exception {
        List<ObjectEntry> objects = new ArrayList<>(); int verified = 0, modules = 0;
        int indexes = 0, missing = 0, corrupt = 0;
        for (Reference reference : references.values()) {
            Path target = transport.temporary("inventory");
            S3ObjectTransport.State observed;
            try { observed = transport.inspect(reference.sha256(), reference.size(), target); }
            finally { Files.deleteIfExists(target); }
            String state = observed.name().toLowerCase(java.util.Locale.ROOT);
            if (observed == S3ObjectTransport.State.VERIFIED) {
                verified++;
                if (reference.classification().equals("module-bundle")) modules++;
                else indexes++;
            } else if (observed == S3ObjectTransport.State.MISSING) missing++;
            else corrupt++;
            objects.add(new ObjectEntry(reference.sha256(), reference.size(),
                    reference.classification(), state));
        }
        return new Snapshot(objects.size(), verified, modules, indexes, missing, corrupt,
                List.copyOf(objects));
    }

    static String json(Snapshot value) {
        StringBuilder out = new StringBuilder("{\n  \"schema\":\"worldline.s3-cas-inventory.v1\",")
                .append("\n  \"verdict_authority\":false,\n  \"paths_included\":false,")
                .append("\n  \"credentials_included\":false,")
                .append("\n  \"coverage\":\"signed-references\",")
                .append("\n  \"remote_enumeration\":false,")
                .append("\n  \"total_references\":").append(value.totalReferences())
                .append(",\n  \"verified_objects\":").append(value.verifiedObjects())
                .append(",\n  \"module_objects\":").append(value.moduleObjects())
                .append(",\n  \"index_objects\":").append(value.indexObjects())
                .append(",\n  \"missing_objects\":").append(value.missingObjects())
                .append(",\n  \"corrupt_objects\":").append(value.corruptObjects())
                .append(",\n  \"objects\":[");
        for (int index = 0; index < value.objects().size(); index++) {
            ObjectEntry item = value.objects().get(index);
            if (index > 0) out.append(',');
            out.append("\n    {\"sha256\":\"").append(item.sha256()).append("\",\"size\":")
                    .append(item.size()).append(",\"class\":\"").append(item.classification())
                    .append("\",\"state\":\"").append(item.state()).append("\"}");
        }
        if (!value.objects().isEmpty()) out.append('\n').append("  ");
        return out.append("]\n}\n").toString();
    }

    static void selfTest() throws Exception {
        Path source = Path.of("").toAbsolutePath().normalize();
        Path root = Files.createTempDirectory("worldline-s3-inventory-test-")
                .toAbsolutePath().normalize();
        try (ModuleCacheS3Fixture fixture = new ModuleCacheS3Fixture(
                "us-east-1", "TESTACCESS", "test-secret")) {
            Files.createDirectories(root.resolve("quality"));
            Files.copy(source.resolve("quality/artifact-transport.properties"),
                    root.resolve("quality/artifact-transport.properties"));
            Path entry = root.resolve("entry"); Files.createDirectories(entry.resolve("fixture"));
            Files.writeString(entry.resolve(".complete"), "a".repeat(64) + "\n");
            Files.write(entry.resolve("fixture/Example.class"),
                    new byte[] {-54, -2, -70, -66, 0, 1});
            ModuleCachePublication.Prepared prepared = ModuleCachePublication.prepare(root,
                    root.resolve(".worldline/cas-shadow/inventory"), List.of(
                            new ModuleCachePublication.Module("fixture", entry)), List.of());
            KeyPair keys = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            Path trust = root.resolve("index.trust"); ModuleCacheIndexTrust.sign(
                    prepared.index().path(), "inventory", keys, trust);
            Map<String, String> environment = new java.util.HashMap<>(fixture.environment());
            environment.put("WORLDLINE_MODULE_CAS_MODE", MODE);
            environment.put("WORLDLINE_MODULE_CAS_INDEX", prepared.index().path().toString());
            environment.put("WORLDLINE_MODULE_CAS_INDEX_TRUST", trust.toString());
            environment.put("WORLDLINE_MODULE_CAS_PUBLIC_KEY", Base64.getEncoder()
                    .encodeToString(keys.getPublic().getEncoded()));
            environment.put("WORLDLINE_MODULE_CAS_CHANNEL", "inventory");
            Snapshot absent = configured(root, environment);
            require(absent.totalReferences() == 2 && absent.missingObjects() == 2,
                    "missing S3 references were hidden");
            ModuleCacheS3 backend = ModuleCacheS3.connect(ModuleCacheS3Config.from(environment),
                    prepared.index().path(), trust, environment.get(
                            "WORLDLINE_MODULE_CAS_PUBLIC_KEY"), "inventory", root.resolve("publish"));
            backend.publish(prepared);
            Snapshot complete = configured(root, environment); String report = json(complete);
            require(complete.verifiedObjects() == 2 && complete.moduleObjects() == 1
                    && complete.indexObjects() == 1 && complete.missingObjects() == 0
                    && complete.corruptObjects() == 0, "verified S3 inventory drifted");
            require(!report.contains(root.toString()) && !report.contains("TESTACCESS")
                    && report.contains("\"remote_enumeration\":false"),
                    "S3 inventory leaked configuration or overstated coverage");
            fixture.corrupt(prepared.modules().get(0).candidate().sha256());
            require(configured(root, environment).corruptObjects() == 1,
                    "corrupt S3 reference was hidden");
            Map<String, String> wrong = new java.util.HashMap<>(environment);
            wrong.put("WORLDLINE_MODULE_S3_SECRET_KEY", "wrong-secret");
            rejected(() -> configured(root, wrong), "invalid S3 inventory credentials were accepted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  S3 CAS inventory self-test: passed");
    }

    private static void add(Map<String, Reference> references, Reference value) {
        require(value.sha256().matches("[0-9a-f]{64}") && value.size() > 0L,
                "invalid S3 CAS reference");
        Reference prior = references.putIfAbsent(value.sha256(), value);
        require(prior == null || prior.equals(value), "conflicting S3 CAS reference");
    }
    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name); require(value != null && !value.isBlank(),
                name + " is required"); return value;
    }
    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false; try { action.run(); }
        catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Snapshot(int totalReferences, int verifiedObjects, int moduleObjects, int indexObjects,
            int missingObjects, int corruptObjects, List<ObjectEntry> objects) { }
    record ObjectEntry(String sha256, long size, String classification, String state) { }
    record Reference(String sha256, long size, String classification) { }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
