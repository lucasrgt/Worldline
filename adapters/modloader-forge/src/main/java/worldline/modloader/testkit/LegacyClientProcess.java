package worldline.modloader.testkit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** One isolated legacy client process with bounded natural shutdown. */
final class LegacyClientProcess implements AutoCloseable {
    private final String loader;
    private final Process process;
    private final Path log, artifact;

    private LegacyClientProcess(String loader, Process process, Path log, Path artifact) {
        this.loader = loader; this.process = process; this.log = log; this.artifact = artifact;
    }

    static LegacyClientProcess start(LegacyClientSettings settings, Path root, int port,
            long seed, String session, String username) throws Exception {
        require(Files.notExists(root) || empty(root), "legacy session directory is not fresh");
        Files.createDirectories(root);
        Path probe = root.resolve("game").resolve(settings.probeTarget).normalize();
        require(probe.startsWith(root.resolve("game")), "legacy probe target escaped game directory");
        Files.createDirectories(probe.getParent());
        Files.copy(settings.probe, probe, StandardCopyOption.REPLACE_EXISTING);
        Path log = root.resolve("client.log"), artifact = root.resolve("profiler.wlpr");
        List<String> command = LegacyClientCommand.create(
                settings, root, artifact, port, seed, session, username);
        ProcessBuilder builder = new ProcessBuilder(command).directory(settings.workspace.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().put("APPDATA", root.toString());
        return new LegacyClientProcess(settings.loader, builder.start(), log, artifact);
    }

    void awaitExit(int seconds) throws Exception {
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            destroy(); throw new IllegalStateException("legacy client did not stop; log=" + log);
        }
        String text = Files.readString(log, StandardCharsets.ISO_8859_1);
        require(process.exitValue() == 0, "legacy client exited " + process.exitValue());
        require(once(text, "WORLDLINE_LEGACY_TESTKIT_BOOT=" + loader)
                && once(text, "WORLDLINE_LEGACY_TESTKIT_SHUTDOWN=" + loader),
                "legacy lifecycle markers are absent or ambiguous");
        require(Files.isRegularFile(artifact) && Files.size(artifact) > 64L,
                "legacy profiler artifact is absent");
    }

    void requireRunning() throws Exception {
        if (process.isAlive()) return;
        String text = Files.isRegularFile(log)
                ? Files.readString(log, StandardCharsets.ISO_8859_1) : "";
        throw new IllegalStateException("legacy client exited " + process.exitValue()
                + " before control connection\n" + tail(text));
    }

    private static boolean empty(Path root) throws Exception {
        try (java.util.stream.Stream<Path> paths = Files.list(root)) { return !paths.findAny().isPresent(); }
    }
    private static boolean once(String text, String marker) {
        int first = text.indexOf(marker); return first >= 0 && text.indexOf(marker, first + marker.length()) < 0;
    }
    private static String tail(String text) {
        String[] lines = text.split("\\R", -1); int start = Math.max(0, lines.length - 40);
        return String.join("\n", java.util.Arrays.copyOfRange(lines, start, lines.length));
    }
    private void destroy() {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(handle -> { if (handle.isAlive()) handle.destroyForcibly(); });
        if (process.isAlive()) process.destroyForcibly();
    }
    @Override public void close() { destroy(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
