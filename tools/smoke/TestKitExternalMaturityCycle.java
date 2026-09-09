import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/** Real external mods consume only public Gradle/plugin/JAR inputs in an isolated build cache. */
public final class TestKitExternalMaturityCycle {
    private static final String ID = "testkit-external-maturity-cycle";
    private TestKitExternalMaturityCycle() {
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 0 && (args.length != 1 || !args[0].equals(ID))) {
            throw new IllegalArgumentException("unexpected maturity milestone");
        }
        Path root = Path.of("").toAbsolutePath().normalize();
        Path smoke = root.resolve("smokes/" + ID), build = root.resolve(".worldline/smokes/" + ID);
        Files.createDirectories(build);
        Properties config = new Properties();
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        require(java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString().equals(value(config, "assessment.date")),
                "new consumer executions require today's UTC assessment date; historical captures cannot be backdated");
        Path distribution = build.resolve("public-distribution");
        Files.createDirectories(distribution);
        for (String type : List.of("api", "runner")) {
            String name = "worldline-test-" + type + "-0.3.0.jar";
            Path jar = distribution.resolve(name);
            if (!Files.isRegularFile(jar)) {
                try (var input = java.net.URI.create(value(config, "release.url") + '/' + name).toURL().openStream()) {
                    Files.copy(input, jar);
                }
            }
            require(SmokeSupport.digest(jar, "SHA-256").equals(value(config, "release." + type + ".sha256")),
                    "public release artifact differs: " + type);
        }
        for (String name : List.of("beta", "aero")) checkout(root, build, config, name);
        Path wrapper = build.resolve("beta/tests/worldline/gradle/wrapper/gradle-wrapper.jar");
        require(SmokeSupport.digest(wrapper, "SHA-256").equals(value(config, "gradle.wrapper.sha256")),
                "public consumer Gradle wrapper differs");
        StringBuilder observations = new StringBuilder(Files.readString(smoke.resolve("history.tsv")));
        for (String name : List.of("beta", "aero")) {
            Path checkout = build.resolve(name), project = checkout.resolve("tests/worldline");
            require(Files.readString(project.resolve("build.gradle.kts")).contains("noRuntime.set(true)"),
                    "external project requests a Minecraft runtime");
            List<String> command = new ArrayList<>(List.of("java", "-Xmx64m", "-classpath", wrapper.toString(),
                    "org.gradle.wrapper.GradleWrapperMain", "--project-dir", project.toString(),
                    "--gradle-user-home", build.resolve("gradle-home").toString(), "--no-daemon", "--max-workers=1",
                    "--console=plain", "--no-configuration-cache", "--rerun-tasks", "--init-script",
                    smoke.resolve("public-consumer.init.gradle").toString(),
                    "-Dorg.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=384m",
                    "-Pworldline.distributionDir=" + distribution, "worldlineDoctor", "worldlineTest"));
            String log = SmokeSupport.capture(project, command, 600);
            Files.writeString(build.resolve(name + "-public-consumer.log"), log, StandardCharsets.UTF_8);
            require(log.contains("WORLDLINE_PUBLIC_PLUGIN=") && log.contains("WORLDLINE_PUBLIC_RUNNER=0.3.0")
                    && log.contains("WORLDLINE_PUBLIC_COMPILE=compileWorldlineTestJava"),
                    "external classpath attestation is missing");
            var count = Pattern.compile("WORLDLINE_TEST=PASS\\Rtests=([0-9]+)").matcher(log);
            require(count.find(), "public TestKit did not pass");
            int tests = Integer.parseInt(count.group(1));
            require(tests == Integer.parseInt(value(config, name + ".tests")) && !count.find(),
                    "external TestKit test count differs");
            require(SmokeSupport.capture(checkout, List.of("git", "status", "--porcelain", "--untracked-files=no"))
                    .isBlank(), "external tracked source changed during qualification");
            observations.append(value(config, name + ".repository")).append('\t')
                    .append(value(config, name + ".revision")).append("\t0.3.0\t")
                    .append(value(config, "assessment.date")).append('\t').append(tests)
                    .append("\ttrue\tlocal-public-consumer-").append(name).append('\n');
        }
        Path records = build.resolve("observations.tsv");
        Files.writeString(records, observations, StandardCharsets.UTF_8);
        String separator = System.getProperty("path.separator");
        String classpath = String.join(separator, SmokeSupport.product(root, "api").toString(),
                SmokeSupport.product(root, "testapi").toString(), SmokeSupport.product(root, "testkit").toString());
        Path classes = build.resolve("classes");
        Files.createDirectories(classes);
        SmokeSupport.capture(root, List.of("javac", "--release", "8", "-Xlint:all,-options", "-Werror",
                "-classpath", classpath, "-d", classes.toString(),
                smoke.resolve("src/worldline/smoke/maturity/ExternalMaturitySmoke.java").toString()));
        String output = SmokeSupport.capture(root, List.of("java", "-classpath", classes + separator + classpath,
                "worldline.smoke.maturity.ExternalMaturitySmoke", records.toString(),
                value(config, "assessment.date"), value(config, "assessment.minimum-span-days"),
                build.resolve("evidence.txt").toString()));
        System.out.print(output);
        String signature = SmokeSupport.digest(build.resolve("evidence.txt"), "SHA-256");
        require(signature.equals(value(config, "expected.signature")), "maturity signature differs: " + signature);
        System.out.println("WORLDLINE_MATURITY_SET=" + value(config, "expected.signal"));
        System.out.println("WORLDLINE_MATURITY_SIGNATURE=" + signature);
    }
    private static void checkout(Path root, Path build, Properties config, String name) throws Exception {
        Path checkout = build.resolve(name);
        String repository = value(config, name + ".repository"), revision = value(config, name + ".revision");
        require(repository.matches("https://github[.]com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")
                && revision.matches("[a-f0-9]{40}"), "invalid public repository pin");
        if (!Files.isDirectory(checkout.resolve(".git"))) {
            SmokeSupport.capture(root, List.of("git", "clone", "--no-checkout", repository + ".git",
                    checkout.toString()), 300);
            SmokeSupport.capture(checkout, List.of("git", "checkout", "--detach", revision));
        }
        require(SmokeSupport.capture(checkout, List.of("git", "rev-parse", "HEAD")).trim().equals(revision),
                "external consumer revision differs");
        require(SmokeSupport.capture(checkout, List.of("git", "remote", "get-url", "origin")).trim()
                .equals(repository + ".git"), "external consumer remote differs");
        require(SmokeSupport.capture(checkout, List.of("git", "status", "--porcelain", "--untracked-files=no"))
                .isBlank(), "external consumer source is dirty");
    }
    private static String value(Properties values, String key) {
        return SmokeSupport.value(values, key);
    }
    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
