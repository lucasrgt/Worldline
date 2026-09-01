import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Recognizes the bounded source evidence accepted by data-cycle pin refreshes. */
final class DataDrivenRefreshEvidence {
    private DataDrivenRefreshEvidence() { }

    static boolean reviewed(Path root, Properties manifest) throws Exception {
        if (staged(root)) return true;
        boolean worktreeClean = git(root, "diff", "--quiet") == 0;
        boolean indexClean = git(root, "diff", "--cached", "--quiet") == 0;
        String recordedRuntime = manifest.getProperty("runtime_support_source_sha256", "");
        String recordedPlan = manifest.getProperty("plan_source_sha256", "");
        String recordedProcess = manifest.getProperty("process_source_sha256", "");
        return worktreeClean && indexClean
                && (!digest(root.resolve("tools/harness/SmokeSupport.java")).equals(recordedRuntime)
                || !digest(root.resolve("tools/harness/DataDrivenCyclePlan.java"))
                        .equals(recordedPlan)
                || !digest(root.resolve("tools/harness/SmokeProcess.java"))
                        .equals(recordedProcess)
                || trainEvidenceChanged(root, manifest));
    }

    private static boolean trainEvidenceChanged(Path root, Properties manifest) throws Exception {
        SmokePins pins = new SmokePins(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        Properties train = TrainPinCheck.manifest(root);
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (!smoke.runner.equals("tools/smoke/DataDrivenCycle.java")) continue;
            String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            if (pin != null && TrainPinCheck.carriesCurrent(train, smoke.id, pin, current)
                    && !pin.evidence().equals(manifest.getProperty(
                            "cycle." + smoke.id + ".evidence_sha256"))) return true;
        }
        return false;
    }

    static boolean unchangedMilestone(Path root, String id) throws Exception {
        return git(root, "diff", "--quiet", "HEAD", "--", "smokes/" + id) == 0;
    }

    static FixtureRefactor fixture(Path root, String id) throws Exception {
        List<String> paths = capture(root, "diff", "--name-only", "HEAD", "--", "smokes/" + id)
                .lines().filter(value -> !value.isBlank()).toList();
        if (paths.size() != 1 || !paths.get(0).endsWith(".java")) return null;
        String path = paths.get(0), prior = capture(root, "show", "HEAD:" + path);
        String current = Files.readString(root.resolve(path), StandardCharsets.UTF_8);
        String expected = SharedFixturePatch.rewrite(prior).replace("\r\n", "\n");
        return expected.equals(current.replace("\r\n", "\n"))
                ? new FixtureRefactor(path, prior, current) : null;
    }

    private static boolean staged(Path root) throws Exception {
        return git(root, "diff", "--cached", "--quiet") == 1;
    }

    private static String capture(Path root, String... arguments) throws Exception {
        List<String> command = command(arguments);
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output;
    }

    private static int git(Path root, String... arguments) throws Exception {
        return new ProcessBuilder(command(arguments)).directory(root.toFile()).start().waitFor();
    }

    private static List<String> command(String... arguments) {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        return command;
    }

    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    record FixtureRefactor(String path, String prior, String current) { }
}
