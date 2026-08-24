package worldline.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/** Generates one isolated modern Gradle build under tests/worldline. */
final class WorldlineProjectInit {
    private static final String GRADLE = "8.14.4";
    private static final String RAW = "https://raw.githubusercontent.com/gradle/gradle/v" + GRADLE + "/";
    private WorldlineProjectInit() {}
    static int run(String[] arguments, java.io.PrintStream output) throws Exception {
        Options options = Options.parse(arguments); Path target = options.target.toAbsolutePath().normalize();
        require(!Files.exists(target), "target already exists: " + target);
        Files.createDirectories(target.resolve("src/test/java/example"));
        Files.createDirectories(target.resolve("src/test/resources")); Files.createDirectories(target.resolve("snapshots"));
        Path oracle = target.resolve(".local/oracles/b1.7.3"); Files.createDirectories(oracle);
        write(target.resolve("settings.gradle.kts"), settings(target));
        write(target.resolve("build.gradle.kts"), build(options));
        write(target.resolve("worldline.toml"), config(options));
        write(target.resolve("README.md"), readme(options));
        write(target.resolve(".gitignore"), ".gradle/\nbuild/\n");
        write(target.resolve("src/test/java/example/ExampleWorldlineTest.java"), sample(options.template));
        write(oracle.resolve(".gitignore"), "*\n!.gitignore\n!README.txt\n");
        write(oracle.resolve("README.txt"), oracleReadme());
        if (!options.noWrapper) wrapper(target);
        output.println("WORLDLINE_INIT=PASS"); output.println("project=" + target);
        output.println("next=" + target.resolve(isWindows() ? "gradlew.bat" : "gradlew") + " worldlineDoctor");
        return 0;
    }
    private static void wrapper(Path target) throws Exception {
        Path wrapper = target.resolve("gradle/wrapper"); Files.createDirectories(wrapper);
        PinnedDownload.fetch(RAW + "gradle/wrapper/gradle-wrapper.jar",
                "7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172",
                wrapper.resolve("gradle-wrapper.jar"));
        PinnedDownload.fetch(RAW + "gradlew",
                "fb49f8cb2e5b1d83fba3dcc2c0dd0934c5655cbaf8bd1510ada7cc02692095ca",
                target.resolve("gradlew"));
        PinnedDownload.fetch(RAW + "gradlew.bat",
                "49792578a7942e08b708df3d3902ddbfc49d2203775c0161ef0c24b23fe4aeae",
                target.resolve("gradlew.bat"));
        write(wrapper.resolve("gradle-wrapper.properties"), String.join("\n",
                "distributionBase=GRADLE_USER_HOME", "distributionPath=wrapper/dists",
                "distributionUrl=https\\://services.gradle.org/distributions/gradle-" + GRADLE + "-bin.zip",
                "distributionSha256Sum=f1771298a70f6db5a29daf62378c4e18a17fc33c9ba6b14362e0cdf40610380d",
                "networkTimeout=10000", "validateDistributionUrl=true", "zipStoreBase=GRADLE_USER_HOME",
                "zipStorePath=wrapper/dists") + "\n");
        try { Files.setPosixFilePermissions(target.resolve("gradlew"), EnumSet.of(
                PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE)); } catch (UnsupportedOperationException ignored) { }
    }
    private static String settings(Path target) {
        String name = target.getParent() == null ? "mod" : target.getParent().getParent().getFileName().toString();
        return "pluginManagement { repositories { gradlePluginPortal(); mavenCentral() } }\n"
                + "dependencyResolutionManagement { repositories { mavenCentral() } }\n"
                + "rootProject.name = \"" + safe(name) + "-worldline-tests\"\n";
    }
    private static String build(Options options) {
        return "plugins { id(\"io.github.lucasrgt.worldline.test\") version \"0.3.1\" }\n\n"
                + "worldline {\n    runtime.set(\"b1.7.3\")\n    oracleProfile.set(\"b173-local\")\n"
                + "    noRuntime.set(" + options.hostOnly + ")\n}\n";
    }
    private static String config(Options options) {
        return "runtime = \"b1.7.3\"\nprofile = \"b173-local\"\nnoRuntime = \""
                + options.hostOnly + "\"\nloader = \"" + options.loader + "\"\n";
    }
    private static String readme(Options options) {
        return "# Worldline tests\n\nGenerated for runtime `b1.7.3`, loader `" + options.loader
                + "`, template `" + options.template + "`.\n\nRun `./gradlew worldlineDoctor` then `./gradlew worldlineTest`.\n";
    }
    private static String oracleReadme() {
        return "Place the official Minecraft Beta 1.7.3 client JAR here as minecraft.jar.\n"
                + "Optional server tests also use minecraft_server.jar. Files here are ignored and hash-verified.\n";
    }
    private static String sample(String template) {
        if (template.equals("gui")) return guiSample();
        String planned = template.equals("basic") ? "" : "\n            test(\"" + template
                + " contract\", worldline().runtime(\"b1.7.3\").seed(173L)"
                + ".run(context -> {\n                // Replace with the project-owned " + template
                + " fixture and observable assertions.\n            })).todo();";
        return "package example;\n\nimport worldline.test.WorldlineSpec;\n"
                + "import static worldline.test.Expect.expect;\nimport static worldline.test.Worldline.*;\n\n"
                + "public final class ExampleWorldlineTest extends WorldlineSpec {\n"
                + "    @Override protected void define() {\n        describe(\"Worldline setup\", () -> {\n"
                + "            test(\"loads the test project\", context -> expect(173).toEqual(173));"
                + planned + "\n        });\n    }\n}\n";
    }
    private static String guiSample() {
        return "package example;\n\nimport worldline.api.GameUi;\n"
                + "import worldline.api.GameUiCapability;\nimport worldline.api.GameUiNode;\n"
                + "import worldline.test.WorldlineSpec;\n\nimport static worldline.test.Expect.expect;\n"
                + "import static worldline.test.Worldline.*;\n\n"
                + "public final class ExampleWorldlineTest extends WorldlineSpec {\n"
                + "    @Override protected void define() {\n        describe(\"Vanilla inventory GUI\", () -> {\n"
                + "            test(\"exposes the semantic inventory tree\", worldline().runtime(\"b1.7.3\")"
                + ".seed(173L).run(context -> {\n                GameUi ui = context.ui();\n"
                + "                ui.require(GameUiCapability.INVENTORY_LIFECYCLE);\n"
                + "                ui.openInventory();\n                context.tick();\n"
                + "                expect(ui.screen()).toEqual(GameUiNode.INVENTORY);\n"
                + "                ui.getByRole(GameUiNode.SLOT).shouldHaveCount(45);\n"
                + "                ui.getSlot(0).shouldBeVisible();\n"
                + "                context.onFinished(done -> ui.close());\n            }));\n"
                + "        });\n    }\n}\n";
    }
    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent()); Files.writeString(path, text, StandardCharsets.UTF_8);
    }
    private static String safe(String value) { return value.replaceAll("[^A-Za-z0-9_.-]", "-"); }
    private static boolean isWindows() { return java.io.File.separatorChar == '\\'; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class Options {
        Path target = Paths.get("tests/worldline"); String loader = "modloader", template = "basic";
        boolean hostOnly, noWrapper;
        static Options parse(String[] arguments) {
            Options value = new Options();
            for (String item : arguments) {
                if (item.startsWith("--target=")) value.target = Paths.get(item.substring(9));
                else if (item.startsWith("--loader=")) value.loader = token(item.substring(9), "loader");
                else if (item.startsWith("--template=")) value.template = token(item.substring(11), "template");
                else if (item.equals("--host-only")) value.hostOnly = true;
                else if (item.equals("--no-wrapper")) value.noWrapper = true;
                else if (!item.equals("--runtime=b1.7.3")) throw new IllegalArgumentException("unknown init option: " + item);
            }
            require(Arrays.asList("basic", "storage", "gui", "optimization", "multiplayer").contains(value.template),
                    "unknown template " + value.template); return value;
        }
        private static String token(String value, String label) {
            require(value.matches("[a-z][a-z0-9_.-]*"), "invalid " + label); return value;
        }
    }
}
