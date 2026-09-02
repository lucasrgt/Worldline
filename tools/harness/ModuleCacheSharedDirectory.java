import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

/** Immutable read-through module bundles on a shared filesystem CAS. */
final class ModuleCacheSharedDirectory {
    private static final Object[] LOCAL_LOCKS = localLocks();
    private final Path root, downloads;
    private final ModuleCacheIndex index;

    private ModuleCacheSharedDirectory(Path root, Path downloads, ModuleCacheIndex index) {
        this.root = root; this.downloads = downloads; this.index = index;
    }

    public static void main(String[] arguments) {
        try {
            if (arguments.length == 5 && "--self-test-publish".equals(arguments[0])) {
                selfTestPublish(arguments); return;
            }
            require(java.util.List.of(arguments).equals(java.util.List.of("--self-test")),
                    "usage: ModuleCacheSharedDirectory --self-test");
            selfTest();
        } catch (Exception error) {
            System.err.println("shared-directory module cache failed: " + error.getMessage()); System.exit(1);
        }
    }

    static ModuleCacheSharedDirectory connect(Path shared, Path indexPath, Path trustPath,
            String publicKey, String channel, Path downloads) throws Exception {
        Path root = absolute(shared, "shared module CAS directory");
        require(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(root),
                "shared module CAS directory is absent or linked");
        ModuleCacheIndexTrust.verify(indexPath, trustPath, publicKey, channel);
        return new ModuleCacheSharedDirectory(root, absolute(downloads, "module download directory"),
                ModuleCacheIndex.read(indexPath));
    }

    boolean hydrate(String module, String inputSha256, Path target) throws Exception {
        ModuleCacheIndex.Entry entry = index.lookup(module, inputSha256, System.getProperty("java.version"));
        if (entry == null) return false;
        Path object = object(root, entry.blobSha256()); verify(object, entry.blobSha256(), entry.blobSize());
        Files.createDirectories(downloads); Path bundle = downloads.resolve(module + "-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime() + ".wmb");
        require(!Files.exists(bundle, LinkOption.NOFOLLOW_LINKS), "module download target already exists");
        try {
            copy(object, bundle); verify(bundle, entry.blobSha256(), entry.blobSize());
            ModuleCacheBundle.Identity identity = ModuleCacheBundle.validate(bundle);
            require(identity.module().equals(module) && identity.input().equals(inputSha256)
                    && identity.javaVersion().equals(System.getProperty("java.version"))
                    && identity.sha256().equals(entry.blobSha256()) && identity.size() == entry.blobSize(),
                    "shared module bundle identity drifted");
            ModuleCacheBundle.extract(bundle, module, inputSha256, target); return true;
        } finally { Files.deleteIfExists(bundle); }
    }

    static void publish(Path shared, ModuleCachePublication.Prepared prepared) throws Exception {
        String permits = ArtifactTransportPermitQuery.manifest(prepared.candidates());
        require(Files.readString(prepared.permits(), StandardCharsets.UTF_8).equals(permits),
                "shared module publication permits drifted");
        Path root = createRoot(shared);
        for (ArtifactTransportPolicy.Candidate candidate : prepared.candidates()) {
            verify(candidate.path(), candidate.sha256(), candidate.size());
            publish(root, candidate);
        }
    }

    static void selfTest() throws Exception {
        Path source = Path.of("").toAbsolutePath().normalize();
        Path fixture = Files.createTempDirectory("worldline-shared-module-cas-").toAbsolutePath().normalize();
        try {
            Files.createDirectories(fixture.resolve("quality"));
            Files.copy(source.resolve("quality/artifact-transport.properties"),
                    fixture.resolve("quality/artifact-transport.properties"));
            Path entry = fixture.resolve("entry"), classFile = entry.resolve("fixture/Example.class");
            Files.createDirectories(classFile.getParent()); String input = "a".repeat(64);
            Files.writeString(entry.resolve(".complete"), input + "\n");
            Files.write(classFile, new byte[] {-54, -2, -70, -66, 0, 1});
            ModuleCachePublication.Prepared prepared = ModuleCachePublication.prepare(fixture,
                    fixture.resolve(".worldline/cas-shadow/shared"),
                    java.util.List.of(new ModuleCachePublication.Module("fixture", entry)), java.util.List.of());
            Path shared = fixture.resolve("shared-store"); concurrentPublish(shared, prepared);
            publish(shared, prepared);
            concurrentProcessPublish(fixture.resolve("shared-process-store"),
                    prepared.candidates().get(0));
            KeyPair keys = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            Path trust = fixture.resolve("index.trust"); ModuleCacheIndexTrust.sign(
                    prepared.index().path(), "shared", keys, trust);
            String publicKey = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
            ModuleCacheSharedDirectory backend = connect(shared, prepared.index().path(), trust,
                    publicKey, "shared", fixture.resolve("downloads"));
            Path target = fixture.resolve("hydrated");
            require(backend.hydrate("fixture", input, target)
                    && Files.isRegularFile(target.resolve("fixture/Example.class")),
                    "shared module hydration failed");
            require(!backend.hydrate("fixture", "b".repeat(64), fixture.resolve("miss")),
                    "shared module miss was accepted");
            Path object = object(shared, prepared.modules().get(0).candidate().sha256());
            object.toFile().setWritable(true); Files.writeString(object, "corrupt");
            rejected(() -> backend.hydrate("fixture", input, fixture.resolve("corrupt")),
                    "corrupt shared module object was accepted");
        } finally { SafeTreeDelete.delete(fixture); }
        System.out.println("  shared-directory module cache self-test: passed");
    }

    private static void concurrentPublish(Path shared, ModuleCachePublication.Prepared prepared)
            throws Exception {
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Future<?> first = pool.submit(() -> { publish(shared, prepared); return null; });
            java.util.concurrent.Future<?> second = pool.submit(() -> { publish(shared, prepared); return null; });
            first.get(); second.get();
        } finally {
            pool.shutdownNow(); require(pool.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                    "shared CAS publisher pool did not terminate");
        }
    }

    private static void concurrentProcessPublish(Path shared,
            ArtifactTransportPolicy.Candidate candidate) throws Exception {
        java.util.List<Process> processes = new java.util.ArrayList<>();
        for (int index = 0; index < 4; index++) processes.add(new ProcessBuilder(javaTool(), "-cp",
                System.getProperty("java.class.path"), "ModuleCacheSharedDirectory", "--self-test-publish",
                shared.toString(), candidate.path().toString(), candidate.sha256(),
                Long.toString(candidate.size())).redirectErrorStream(true).start());
        for (Process process : processes) {
            require(process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS),
                    "shared CAS publisher process timed out");
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            require(process.exitValue() == 0, "shared CAS publisher process failed: " + output);
        }
        verify(object(shared, candidate.sha256()), candidate.sha256(), candidate.size());
    }

    private static void selfTestPublish(String[] arguments) throws Exception {
        Path shared = Path.of(arguments[1]).toAbsolutePath().normalize();
        Path source = Path.of(arguments[2]).toAbsolutePath().normalize();
        String digest = arguments[3]; long size = Long.parseLong(arguments[4]);
        publish(createRoot(shared), new ArtifactTransportPolicy.Candidate("shared-module-cache",
                ArtifactTransportClass.PORTABLE_WORLDLINE_SOURCE_DERIVED, digest, size, source));
    }

    private static void publish(Path root, ArtifactTransportPolicy.Candidate candidate) throws Exception {
        Path target = object(root, candidate.sha256());
        synchronized (localLock(candidate.sha256())) {
            Path lock = publicationLock(root, candidate.sha256()); safeDirectory(root, lock.getParent());
            require(!Files.exists(lock, LinkOption.NOFOLLOW_LINKS)
                    || Files.isRegularFile(lock, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isSymbolicLink(lock), "shared CAS publication lock is invalid");
            try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS); FileLock held = channel.lock()) {
                require(held.isValid() && Files.isRegularFile(lock, LinkOption.NOFOLLOW_LINKS)
                        && !Files.isSymbolicLink(lock), "shared CAS publication lock drifted");
                publishLocked(root, target, candidate);
            }
        }
    }

    private static void publishLocked(Path root, Path target,
            ArtifactTransportPolicy.Candidate candidate) throws Exception {
        safeDirectory(root, target.getParent());
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            verify(target, candidate.sha256(), candidate.size()); return;
        }
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        try {
            copy(candidate.path(), temporary); verify(temporary, candidate.sha256(), candidate.size());
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException error) { Files.move(temporary, target); }
            require(target.toFile().setReadOnly(), "shared CAS object could not be made read-only");
            verify(target, candidate.sha256(), candidate.size());
        } catch (java.io.IOException error) {
            verify(target, candidate.sha256(), candidate.size());
        } finally { Files.deleteIfExists(temporary); }
    }

    private static Path createRoot(Path value) throws Exception {
        Path root = absolute(value, "shared module CAS directory"); Files.createDirectories(root);
        require(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(root),
                "shared module CAS root is linked"); return root;
    }
    private static void safeDirectory(Path root, Path directory) throws Exception {
        Files.createDirectories(directory);
        require(Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(directory)
                && directory.toRealPath().startsWith(root.toRealPath()), "shared CAS object directory escaped root");
    }
    private static Path object(Path root, String digest) {
        require(digest.matches("[0-9a-f]{64}"), "invalid shared CAS object digest");
        Path path = root.resolve("objects/sha256").resolve(digest.substring(0, 2))
                .resolve(digest.substring(2)).toAbsolutePath().normalize();
        require(path.startsWith(root), "shared CAS object path escaped root"); return path;
    }
    private static Path publicationLock(Path root, String digest) {
        require(digest.matches("[0-9a-f]{64}"), "invalid shared CAS lock digest");
        Path path = root.resolve("locks/sha256").resolve(digest.substring(0, 2))
                .resolve(digest.substring(2) + ".lock").toAbsolutePath().normalize();
        require(path.startsWith(root), "shared CAS publication lock escaped root"); return path;
    }
    private static Object localLock(String digest) {
        return LOCAL_LOCKS[(digest.hashCode() & Integer.MAX_VALUE) % LOCAL_LOCKS.length];
    }
    private static Object[] localLocks() {
        Object[] values = new Object[64];
        java.util.Arrays.setAll(values, ignored -> new Object()); return values;
    }
    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                "java" + (windows ? ".exe" : "")).toString();
    }
    private static void copy(Path source, Path target) throws Exception {
        require(Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(source),
                "shared CAS source is not a regular file");
        try (InputStream input = Files.newInputStream(source); OutputStream output = Files.newOutputStream(
                target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) { input.transferTo(output); }
    }
    private static void verify(Path path, String digest, long size) throws Exception {
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)
                && Files.size(path) == size && sha256(path).equals(digest), "shared CAS object identity drifted");
    }
    private static String sha256(Path path) throws Exception {
        MessageDigest digest = HexDigest.sha256();
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static Path absolute(Path value, String label) {
        Path path = value.toAbsolutePath().normalize();
        require(value.isAbsolute() && value.normalize().equals(value), label + " is not absolute and normalized");
        return path;
    }
    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false;
        try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
