import java.nio.file.Path;
import java.util.List;

/** One supervised CSM context policy: bootstrapped stores and a complete four-tool fan-out. */
final class CsmContextPolicy {
    static final List<String> SECTIONS = List.of("== wtw ==", "== rtw ==", "== nya ==", "== nwc ==");
    private static final List<String> BOOTSTRAP_TOOLS = List.of("wtw", "rtw", "nwc");

    private CsmContextPolicy() { }

    /** Initializes every optional durable store with its idempotent init command. */
    static void bootstrap(Path root) throws Exception {
        for (String tool : BOOTSTRAP_TOOLS) {
            SwarmProcess.Result result = SwarmProcess.capture(root,
                    List.of("csm", tool, "init"), 120);
            if (result.exitCode() != 0) {
                throw new IllegalStateException("csm " + tool + " init failed: " + result.stderr());
            }
        }
    }

    /** Accepts only a zero exit whose output proves all four tools were consulted. */
    static boolean accepted(int exitCode, String stdout) {
        return exitCode == 0 && SECTIONS.stream().allMatch(stdout::contains);
    }

    /** Requires at least one ranked way line from an rtw guide output. */
    static boolean guided(String guideOutput) {
        return guideOutput.lines().anyMatch(line -> line.startsWith("> "));
    }

    static void selfTest() {
        String full = "== wtw ==\n== rtw ==\n== nya ==\n== nwc ==\n";
        require(accepted(0, full), "complete CSM context was rejected");
        require(!accepted(1, full), "nonzero CSM context was accepted");
        require(!accepted(0, "== nya ==\n"), "partial CSM context fan-out was accepted");
        require(guided("> Behavioral smoke fixture declaration [smoke]\n  guidance\n"),
                "ranked way output was not recognized");
        require(!guided("No relevant ways found.\n"), "empty guide output counted as guidance");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
