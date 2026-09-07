package worldline.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Exercises the documented source launcher in a fresh JVM with its own classpath. */
public final class RepositoryLauncherTest {
    private RepositoryLauncherTest() { }

    public static void main(String[] arguments) throws Exception {
        Path output = Files.createTempFile("worldline-atlas-launcher", ".log");
        Process child = null;
        try {
            child = new ProcessBuilder("java", "tools/replay/Replay.java", "atlas", "coverage")
                    .redirectErrorStream(true).redirectOutput(output.toFile()).start();
            if (!child.waitFor(45, TimeUnit.SECONDS)) {
                throw new AssertionError("repository Atlas launcher timed out");
            }
            String text = Files.readString(output);
            if (child.exitValue() != 0 || !text.contains("source=declared-coverage-unit")
                    || !text.contains("dimension\tTESTABILITY")) {
                throw new AssertionError("repository Atlas launcher failed: " + text);
            }
        } finally {
            if (child != null && child.isAlive()) {
                child.descendants().forEach(ProcessHandle::destroyForcibly);
                child.destroyForcibly().waitFor();
            }
            Files.deleteIfExists(output);
        }
    }
}
