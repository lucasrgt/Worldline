import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;

/** Imports one exact executed plan while the shared data-cycle plan source is migrating. */
final class DataDrivenPlanReceipt {
    private DataDrivenPlanReceipt() { }

    static boolean available(Path root, String id) {
        return Files.isRegularFile(root.resolve(".worldline/reports/milestones")
                        .resolve(id + ".json"))
                && Files.isRegularFile(root.resolve(".worldline/reports/smokes")
                        .resolve(id + ".properties"))
                && Files.isRegularFile(root.resolve(".worldline/smoke-logs")
                        .resolve(id + ".log"));
    }

    static SmokePins.Entry pin(Path root, SmokeDiscovery.Entry smoke, String current)
            throws Exception {
        Path report = root.resolve(".worldline/reports/milestones").resolve(smoke.id + ".json");
        Path attestation = root.resolve(".worldline/reports/smokes")
                .resolve(smoke.id + ".properties");
        Path log = root.resolve(".worldline/smoke-logs").resolve(smoke.id + ".log");
        require(available(root, smoke.id), "new generic plan lacks complete evidence: " + smoke.id);
        Map<String, Object> receipt = MiniJson.object(Files.readString(report));
        Properties proof = StrictProperties.load(attestation);
        Properties descriptor = StrictProperties.load(root.resolve("smokes").resolve(smoke.id)
                .resolve("smoke.properties"));
        String head = MiniJson.string(receipt, "head");
        String evidence = MiniJson.string(receipt, "evidence_sha256");
        require("passed".equals(MiniJson.string(receipt, "status"))
                        && smoke.id.equals(MiniJson.string(receipt, "id"))
                        && head.equals(proof.getProperty("head"))
                        && "passed".equals(proof.getProperty("status"))
                        && evidence.equals(digest(Files.readAllBytes(log)))
                        && MiniJson.string(receipt, "signature").equals(
                                descriptor.getProperty("expected.signature")),
                "invalid new generic milestone evidence: " + smoke.id);
        require(git(root, "merge-base", "--is-ancestor", head, "HEAD") == 0,
                "new generic milestone is not an ancestor");
        String observed = proof.getProperty("fingerprint", "");
        require(observed.matches("[0-9a-f]{64}"), "invalid new generic fingerprint");
        return new SmokePins.Entry(smoke.id, observed, evidence,
                current.equals(observed) ? "executed" : "refactor-equivalent");
    }

    private static int git(Path root, String... arguments) throws Exception {
        java.util.List<String> command = new java.util.ArrayList<String>();
        command.add("git"); command.addAll(java.util.Arrays.asList(arguments));
        return new ProcessBuilder(command).directory(root.toFile()).start().waitFor();
    }

    private static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
