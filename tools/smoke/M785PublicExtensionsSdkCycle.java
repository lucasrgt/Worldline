import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Compiles and runs a mod fixture outside every Worldline implementation module. */
public final class M785PublicExtensionsSdkCycle {
    private static final String ID = "m785-public-extensions-sdk";
    private static final String SIGNAL = "extensions=1,subjects=4,contracts=3,"
            + "modes=conformance+differential+custom-contract,tests=5,atlas-pages=8,"
            + "imports=public-only";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private M785PublicExtensionsSdkCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/M785PublicExtensionsSdkCycle.java " + ID);
            System.exit(2);
        }
        try { new M785PublicExtensionsSdkCycle().execute(); }
        catch (Exception failure) {
            System.err.println("M785 extension SDK cycle failed: " + failure.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path project = root.resolve("smokes/" + ID + "/external-project");
        Path build = root.resolve(".worldline/smokes/" + ID);
        SmokeSupport.recreate(root, build);
        List<Path> sources = javaFiles(project);
        publicImports(sources);
        Path external = build.resolve("external-classes");
        Files.createDirectories(external);
        List<Path> authoring = paths(product("api"), product("extensions"), product("invariants"),
                product("semantics"), product("trace"), product("minimization"), product("atlas"),
                product("testmodel"), product("testapi"), product("testkit"));
        List<String> compile = new ArrayList<String>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath", join(authoring),
                "-d", external.toString()));
        for (Path source : sources) compile.add(source.toString());
        run(compile, false);
        List<Path> runtime = paths(product("api"), product("extensions"), product("invariants"),
                product("symbolgraph"), product("semantics"), product("trace"),
                product("reproduction"), product("mods"), product("analysis"), product("modtest"),
                product("minimization"), product("atlas"), product("testmodel"),
                product("testapi"), product("testkit"), product("fuzz"), product("profiling"),
                product("coverage"), product("cli"));
        runtime.add(0, external);
        String output = run(Arrays.asList("java", "-classpath", join(runtime),
                "example.worldline.ExternalSdkProbe", project.toString(), build.resolve("results").toString()),
                true);
        require(output.contains("WORLDLINE_EXTENSION_SDK=PASS") && output.contains(SIGNAL),
                "external SDK probe did not emit the frozen signal");
        String cli = run(Arrays.asList("java", "-classpath", join(runtime),
                "worldline.cli.WorldlineCli", "atlas", "extensions", project.toString()), true);
        require(cli.contains("WORLDLINE_ATLAS_EXTENSIONS=PASS")
                && cli.contains("extensions=1") && cli.contains("extension_records=8")
                && cli.contains("extension=example.sdk-fixture"),
                "Atlas CLI did not import the external extension");
        Properties descriptor = properties(root.resolve("smokes/" + ID + "/smoke.properties"));
        String signature = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(SIGNAL.getBytes(StandardCharsets.UTF_8)));
        require(SIGNAL.equals(descriptor.getProperty("expected.signal")), "SDK signal drifted");
        require(signature.equals(descriptor.getProperty("expected.signature")), "SDK signature drifted");
        System.out.println("M785 public Extensions SDK cycle passed");
        System.out.println("  signal: " + SIGNAL);
        System.out.println("  signature: " + signature);
    }

    private static void publicImports(List<Path> sources) throws Exception {
        for (Path source : sources) for (String line : Files.readAllLines(source)) {
            String clean = line.trim();
            if (!clean.startsWith("import ")) continue;
            require(clean.startsWith("import java.") || clean.startsWith("import worldline.api.")
                    || clean.startsWith("import worldline.atlas.")
                    || clean.startsWith("import worldline.extension.")
                    || clean.startsWith("import worldline.test.")
                    || clean.startsWith("import worldline.testkit."),
                    "external fixture imports a non-public package: " + clean);
        }
    }

    private String run(List<String> command, boolean capture) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile());
        if (capture) builder.redirectErrorStream(true);
        else builder.inheritIO();
        Process process = builder.start();
        String output = capture ? new String(process.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8) : "";
        int status = process.waitFor();
        if (capture) System.out.print(output);
        require(status == 0, "command failed: " + command.get(0));
        return output;
    }

    private static List<Path> javaFiles(Path root) throws Exception {
        List<Path> values = new ArrayList<Path>();
        for (String path : SmokeSupport.javaFiles(root)) values.add(Paths.get(path));
        require(!values.isEmpty() && values.size() <= 32, "invalid external source count");
        return values;
    }
    private Path product(String module) {
        return SmokeSupport.product(root, module);
    }
    private static List<Path> paths(Path... modules) {
        List<Path> values = new ArrayList<Path>();
        for (Path module : modules) {
            require(Files.isDirectory(module), "missing " + module.getFileName());
            values.add(module);
        }
        return values;
    }
    private static Properties properties(Path path) throws Exception {
        Properties values = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(path)) { values.load(reader); }
        return values;
    }
    private static String join(List<Path> paths) {
        List<String> values = new ArrayList<String>();
        for (Path path : paths) values.add(path.toString());
        return String.join(File.pathSeparator, values);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
