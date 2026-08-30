import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Fails closed when an adopted CSM tool loses its pin, skill, or tracked corpus. */
final class CsmSuiteCheck {
    private static final List<String> TOOLS = List.of("nya", "rtw", "wtw", "nwc");

    private CsmSuiteCheck() { }

    static void execute(Path root) throws Exception {
        verify(root.resolve(".csm"));
        System.out.println("  csm-suite: four tools pinned, scar and way corpora present");
    }

    static void verify(Path home) throws Exception {
        Path lock = home.resolve("lock.toml");
        require(Files.isRegularFile(lock), "missing CSM lock: " + lock);
        String pins = Files.readString(lock, StandardCharsets.UTF_8);
        for (String tool : TOOLS) {
            require(pins.contains("id = \"" + tool + "\""),
                    "CSM lock does not pin required tool: " + tool);
            Path skill = home.resolve(tool).resolve("SKILL.md");
            require(Files.isRegularFile(skill)
                    && !Files.readString(skill, StandardCharsets.UTF_8).isBlank(),
                    "missing or empty CSM skill: " + skill);
        }
        require(corpus(home.resolve("nya/scars")) > 0,
                "NYA scar corpus is empty; record corrected failures with csm nya remember");
        require(corpus(home.resolve("rtw/ways")) > 0,
                "RTW way corpus is empty; record proven repository patterns with csm rtw add");
    }

    private static int corpus(Path directory) throws Exception {
        if (!Files.isDirectory(directory)) {
            return 0;
        }
        int count = 0;
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory, "*.toml")) {
            for (Path entry : entries) {
                if (Files.size(entry) > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    static void selfTest() throws Exception {
        Path home = Files.createTempDirectory("worldline-csm-suite-");
        try {
            StringBuilder lock = new StringBuilder("schema = 1\n");
            for (String tool : TOOLS) {
                lock.append("\n[[tools]]\nid = \"").append(tool).append("\"\nversion = \"1.0.0\"\n");
                Path skill = home.resolve(tool).resolve("SKILL.md");
                Files.createDirectories(skill.getParent());
                Files.writeString(skill, "# " + tool + "\n", StandardCharsets.UTF_8);
            }
            Files.writeString(home.resolve("lock.toml"), lock.toString(), StandardCharsets.UTF_8);
            expectFailure(home, "empty scar corpus passed");
            seed(home.resolve("nya/scars/NYA-TEST.toml"));
            expectFailure(home, "empty way corpus passed");
            seed(home.resolve("rtw/ways/01wtest.toml"));
            verify(home);
            Files.writeString(home.resolve("nwc/SKILL.md"), "", StandardCharsets.UTF_8);
            expectFailure(home, "blank skill passed");
            Files.writeString(home.resolve("nwc/SKILL.md"), "# nwc\n", StandardCharsets.UTF_8);
            Files.writeString(home.resolve("lock.toml"),
                    lock.toString().replace("id = \"wtw\"", "id = \"other\""),
                    StandardCharsets.UTF_8);
            expectFailure(home, "missing tool pin passed");
        } finally {
            SafeTreeDelete.delete(home);
        }
    }

    private static void seed(Path record) throws Exception {
        Files.createDirectories(record.getParent());
        Files.writeString(record, "schema = 1\n", StandardCharsets.UTF_8);
    }

    private static void expectFailure(Path home, String message) throws Exception {
        try {
            verify(home);
        } catch (IllegalStateException expected) {
            return;
        }
        throw new IllegalStateException(message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
