import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;

/** Owns the single-writer launch claim and terminal infrastructure dispositions. */
final class OxAlphaRolloverLaunch {
    private OxAlphaRolloverLaunch() {
    }

    static void validateAvailable(Path root, OxAlphaRequest request) throws Exception {
        Path reports = reports(root);
        require(!Files.exists(reports.resolve(request.evidenceStem() + ".json")),
                "authorized rollover launch already exists");
        require(!Files.exists(claim(root, request)), "authorized rollover launch is already reserved");
        Path failure = startFailure(root, request);
        Path archived = startFailureClaim(root, request);
        require(Files.exists(failure) == Files.exists(archived),
                "start-failure rollover evidence is incomplete");
        if (Files.exists(failure)) {
            validateFailure(request, failure, archived);
        }
        Path exhausted = exhaustedFailure(root, request);
        Path exhaustedClaim = exhaustedFailureClaim(root, request);
        require(Files.exists(exhausted) == Files.exists(exhaustedClaim),
                "exhausted start-failure evidence is incomplete");
        if (Files.exists(exhausted)) {
            validateFailure(request, exhausted, exhaustedClaim);
            throw new IllegalStateException("rollover process-start retry is exhausted");
        }
    }

    static Path reserve(Path root, OxAlphaRequest request) throws Exception {
        Path claim = claim(root, request);
        String value = "{\"schema\":1,\"id\":\"" + request.id()
                + "\",\"attempt\":2,\"launch\":3,\"session\":\"" + request.session()
                + "\",\"rollover_sha256\":\"" + request.rolloverSha()
                + "\",\"status\":\"RESERVED\"}\n";
        Files.writeString(claim, value, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        return claim;
    }

    static Process start(Path root, OxAlphaRequest request, ProcessBuilder builder) throws Exception {
        try {
            return builder.start();
        } catch (Exception failure) {
            if (request.rolloverReceipt() != null) {
                try {
                    startFailed(root, request, failure);
                } catch (Exception evidenceFailure) {
                    failure.addSuppressed(evidenceFailure);
                }
            }
            throw failure;
        }
    }

    static String claimSha(Path root, OxAlphaRequest request) throws Exception {
        return request.rolloverReceipt() == null ? null
                : OxAlphaInfrastructureRollover.sha(claim(root, request));
    }

    static void startFailed(Path root, OxAlphaRequest request, Exception failure) throws Exception {
        Path claim = claim(root, request);
        String claimSha = OxAlphaInfrastructureRollover.sha(claim);
        boolean retried = Files.exists(startFailure(root, request));
        Path archived = retried ? exhaustedFailureClaim(root, request)
                : startFailureClaim(root, request);
        Path receipt = retried ? exhaustedFailure(root, request) : startFailure(root, request);
        require(!Files.exists(archived) && !Files.exists(receipt),
                "rollover process-start evidence already exists");
        String value = failure(request, claimSha,
                retried ? "process-start-retry" : "process-start", failure);
        Path temporary = temporary(reports(root));
        Files.writeString(temporary, value, StandardCharsets.UTF_8);
        try {
            Files.move(claim, archived, StandardCopyOption.ATOMIC_MOVE);
            Files.move(temporary, receipt, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static void terminalFailure(Path root, OxAlphaRequest request, String phase,
            Exception failure) throws Exception {
        Path receipt = reports(root).resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3-terminal-failure.json");
        String value = failure(request, claimSha(root, request), phase, failure);
        publish(receipt, value);
    }

    static void recordTerminal(Path root, OxAlphaRequest request, String phase,
            Exception failure) {
        if (request.rolloverReceipt() == null) {
            return;
        }
        try {
            terminalFailure(root, request, phase, failure);
        } catch (Exception evidenceFailure) {
            failure.addSuppressed(evidenceFailure);
        }
    }

    private static String failure(OxAlphaRequest request, String claimSha, String phase,
            Exception failure) {
        return "{\"schema\":1,\"id\":\"" + request.id()
                + "\",\"attempt\":2,\"launch\":3,\"session\":\"" + request.session()
                + "\",\"rollover_sha256\":\"" + request.rolloverSha()
                + "\",\"claim_sha256\":\"" + claimSha + "\",\"phase\":\"" + phase
                + "\",\"error_type\":\"" + failure.getClass().getName()
                + "\",\"created\":\"" + Instant.now()
                + "\",\"disposition\":\"RETRYABLE\",\"status\":\"RETRYABLE\"}\n";
    }

    private static void validateFailure(OxAlphaRequest request, Path receipt, Path claim)
            throws Exception {
        Map<String, Object> json = MiniJson.object(Files.readString(receipt, StandardCharsets.UTF_8));
        require(MiniJson.string(json, "id").equals(request.id())
                && MiniJson.string(json, "session").equals(request.session())
                && MiniJson.string(json, "rollover_sha256").equalsIgnoreCase(request.rolloverSha())
                && MiniJson.string(json, "claim_sha256").equalsIgnoreCase(
                        OxAlphaInfrastructureRollover.sha(claim))
                && MiniJson.string(json, "status").equals("RETRYABLE"),
                "start-failure rollover evidence drifted");
    }

    private static void publish(Path output, String value) throws Exception {
        Path temporary = temporary(output.getParent());
        Files.writeString(temporary, value, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, output, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static Path temporary(Path reports) throws Exception {
        return Files.createTempFile(reports, ".rollover-launch-", ".json.tmp");
    }

    private static Path reports(Path root) {
        return root.resolve(".worldline/reports/swarm").normalize();
    }

    private static Path claim(Path root, OxAlphaRequest request) {
        return reports(root).resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3.claim");
    }

    private static Path startFailure(Path root, OxAlphaRequest request) {
        return reports(root).resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3-start-failure.json");
    }

    private static Path startFailureClaim(Path root, OxAlphaRequest request) {
        return reports(root).resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3-start-failure.claim");
    }

    private static Path exhaustedFailure(Path root, OxAlphaRequest request) {
        return reports(root).resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3-start-failure-exhausted.json");
    }

    private static Path exhaustedFailureClaim(Path root, OxAlphaRequest request) {
        return reports(root).resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3-start-failure-exhausted.claim");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
