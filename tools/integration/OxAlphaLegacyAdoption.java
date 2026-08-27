import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Verifies a legacy adoption receipt at the Ox Alpha process boundary. */
final class OxAlphaLegacyAdoption {
    private OxAlphaLegacyAdoption() {
    }

    static void validate(Path root, OxAlphaRequest request) throws Exception {
        require(request.phase().equals("checkpoint") && request.attempt() == 2,
                "legacy adoption only authorizes checkpoint attempt 2");
        Path expected = root.resolve(".worldline/reports/swarm/legacy-retry-adoption-"
                + request.id() + ".json").normalize();
        Path supplied = Path.of(request.adoptionReceipt()).toAbsolutePath().normalize();
        require(supplied.equals(expected) && Files.isRegularFile(expected),
                "legacy adoption receipt is not at its canonical path");
        require(sha(expected).equalsIgnoreCase(request.adoptionSha()),
                "legacy adoption receipt SHA-256 drifted");
        OxAlphaAdoptionReceipt receipt = OxAlphaAdoptionReceipt.read(expected);
        require(receipt.schema() == 1 && receipt.id().equals(request.id())
                && GitAncestry.contains(root, receipt.controlBase(), request.controlBase())
                && receipt.branch().equals("codex/milestone-" + request.id())
                && receipt.worktree().equals(root)
                && receipt.priorAttempt() == 1 && receipt.authorizedAttempt() == 2
                && receipt.maxAttempts() == 2 && receipt.status().equals("PASS"),
                "legacy adoption receipt drifted");
        if (request.session() == null) {
            require(receipt.mode().equals("process-recovery") && receipt.session() == null
                    && receipt.recoverySessions() == 1,
                    "process-recovery adoption drifted");
            int limit = receipt.recoveryTimeout();
            require(request.timeoutSeconds() > 0 && request.timeoutSeconds() <= limit && limit <= 3600,
                    "process-recovery session exceeds its adoption budget");
        } else {
            boolean resumed = receipt.mode().equals("resume-session")
                    && request.session().equals(receipt.session())
                    && receipt.recoverySessions() == 0;
            boolean recovered = request.rolloverReceipt() != null
                    && request.rolloverSha() != null
                    && receipt.mode().equals("process-recovery") && receipt.session() == null
                    && receipt.recoverySessions() == 1;
            require(resumed || recovered,
                    "resume-session adoption drifted");
            if (recovered) {
                require(receipt.recoveryTimeout() > 0 && receipt.recoveryTimeout() <= 3600,
                        "process-recovery adoption budget drifted");
                require(request.timeoutSeconds() == 7200,
                        "rollover fallback recovery requires exactly 7200 seconds");
            }
        }
    }

    private static String sha(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
