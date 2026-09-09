import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/** Java-owned filesystem declaration and no-follow output-budget verifier. */
public final class ExecutionFilesystemManifest {
    private static final String SCHEMA = "worldline.execution-filesystem-manifest.v1";
    private final String profile;
    private final Path sourceRoot;
    private final Path temporaryRoot;
    private final Path outputRoot;
    private final long maximumBytes;
    private final int maximumFiles;

    private ExecutionFilesystemManifest(String profile, Path sourceRoot, Path temporaryRoot,
            Path outputRoot, long maximumBytes, int maximumFiles) {
        this.profile = profile; this.sourceRoot = clean(sourceRoot, "source root");
        this.temporaryRoot = clean(temporaryRoot, "temporary root");
        this.outputRoot = clean(outputRoot, "output root");
        this.maximumBytes = maximumBytes; this.maximumFiles = maximumFiles; validate();
    }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ExecutionFilesystemManifest");
            Path root = Path.of("").toAbsolutePath().normalize();
            Path output = root.resolve(".worldline/executor-tooling-cycle/current");
            System.out.write(tooling(root, output.resolve("java-tmp"), output).descriptor());
        } catch (Exception error) {
            System.err.println("execution filesystem manifest failed: " + error.getMessage()); System.exit(1);
        }
    }

    static ExecutionFilesystemManifest tooling(Path root, Path temporary, Path output) {
        return new ExecutionFilesystemManifest("tooling-cycle", root, temporary, output, 4L << 20, 32);
    }

    WorldlineExecutionPlan.Filesystem executorPolicy() {
        return WorldlineExecutionPlan.Filesystem.inherit();
    }

    void verifyOutput() throws Exception {
        require(Files.isDirectory(outputRoot, LinkOption.NOFOLLOW_LINKS), "tooling output root is absent");
        Count count = new Count(); inspect(outputRoot, count);
        require(count.files <= maximumFiles, "tooling output file count exceeded manifest");
        require(count.bytes <= maximumBytes, "tooling output bytes exceeded manifest");
    }

    byte[] descriptor() {
        String json = "{\"schema\":" + q(SCHEMA) + ",\"profile\":" + q(profile)
                + ",\"enforcement\":\"post-execution-output-invariant\",\"paths\":["
                + path("source", "read-only", sourceRoot) + ","
                + path("temporary", "read-write", temporaryRoot) + ","
                + path("output", "read-write", outputRoot)
                + "],\"official_artifact_mounts\":[],\"forbidden\":\"all-undeclared-output-paths\""
                + ",\"limits\":{\"maximum_bytes\":" + maximumBytes
                + ",\"maximum_files\":" + maximumFiles + "}}";
        return (json + "\n").getBytes(StandardCharsets.UTF_8);
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-filesystem-manifest-").toAbsolutePath().normalize();
        Path output = root.resolve("output"), temporary = output.resolve("tmp");
        try {
            Files.createDirectories(temporary); Files.writeString(output.resolve("one"), "one");
            tooling(root, temporary, output).verifyOutput();
            String descriptor = new String(tooling(root, temporary, output).descriptor(), StandardCharsets.UTF_8);
            require(descriptor.contains("\"enforcement\":\"post-execution-output-invariant\"")
                    && descriptor.contains("\"official_artifact_mounts\":[]"), "filesystem descriptor drift");
            ExecutionFilesystemManifest narrow = new ExecutionFilesystemManifest(
                    "narrow", root, temporary, output, 2, 1);
            expectFailure(narrow::verifyOutput);
        } finally {
            Files.deleteIfExists(output.resolve("one")); Files.deleteIfExists(temporary);
            Files.deleteIfExists(output); Files.deleteIfExists(root);
        }
    }

    private void inspect(Path path, Count count) throws Exception {
        require(path.normalize().startsWith(outputRoot), "tooling output escaped manifest root");
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        require(!attributes.isSymbolicLink() && !attributes.isOther(), "link-like tooling output is forbidden");
        if (attributes.isDirectory()) try (var children = Files.newDirectoryStream(path)) {
            for (Path child : children) inspect(child, count);
        } else {
            require(attributes.isRegularFile(), "non-regular tooling output is forbidden");
            count.files = Math.addExact(count.files, 1); count.bytes = Math.addExact(count.bytes, attributes.size());
        }
    }

    private void validate() {
        require(profile.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid filesystem profile");
        require(temporaryRoot.startsWith(outputRoot) && !temporaryRoot.equals(outputRoot),
                "temporary root must be inside output root");
        require(!sourceRoot.startsWith(outputRoot) && outputRoot.startsWith(sourceRoot),
                "tooling output root must be a distinct source-root descendant");
        require(maximumBytes > 0 && maximumFiles > 0, "invalid filesystem output budget");
    }

    private static Path clean(Path path, String label) { require(path.isAbsolute() && path.normalize().equals(path),
            label + " is not absolute and normalized"); return path; }
    private static String path(String kind, String access, Path path) { return "{\"kind\":" + q(kind)
            + ",\"access\":" + q(access) + ",\"path\":" + q(path.toString()) + "}"; }
    private static String q(String value) { return ExecutionJson.quote(value); }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid filesystem evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    private static final class Count { private int files; private long bytes; }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
