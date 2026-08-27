import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/** Atomically publishes one single-writer infrastructure-rollover receipt. */
final class OxAlphaRolloverReceipt {
    private OxAlphaRolloverReceipt() {
    }

    static Path publish(Path reports, OxAlphaInfrastructureRollover.CreateRequest request,
            String head, String tree, Path prior, Path adoption, Path sessionEvidence,
            String priorModel, String adoptionMode, OxAlphaProviderOccurrence.Result occurrence)
            throws Exception {
        Path output = reports.resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3.json");
        Path claim = reports.resolve("infrastructure-rollover-" + request.id()
                + "-attempt2-launch3.seal.claim");
        require(!Files.exists(output), "infrastructure rollover receipt already exists");
        String claimValue = "{\"schema\":1,\"id\":\"" + request.id()
                + "\",\"session\":\"" + request.session() + "\",\"prior_sha256\":\""
                + request.priorSha().toLowerCase() + "\",\"provider_log_sha256\":\""
                + occurrence.sourceSha() + "\",\"status\":\"RESERVED\"}\n";
        Files.writeString(claim, claimValue, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        String value = json(request, head, tree, prior, adoption, sessionEvidence, priorModel,
                adoptionMode, occurrence, claim, OxAlphaInfrastructureRollover.sha(claim));
        MiniJson.object(value);
        Path temporary = Files.createTempFile(reports, ".infrastructure-rollover-", ".json.tmp");
        try {
            Files.writeString(temporary, value, StandardCharsets.UTF_8);
            Files.move(temporary, output, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporary);
        }
        return output;
    }

    private static String json(OxAlphaInfrastructureRollover.CreateRequest request,
            String head, String tree, Path prior, Path adoption, Path sessionEvidence,
            String priorModel, String adoptionMode, OxAlphaProviderOccurrence.Result occurrence,
            Path claim, String claimSha) {
        StringBuilder lines = new StringBuilder("[");
        for (int index = 0; index < occurrence.excerpt().size(); index++) {
            if (index > 0) {
                lines.append(',');
            }
            lines.append("\n    \"").append(escape(occurrence.excerpt().get(index))).append("\"");
        }
        lines.append("\n  ]");
        return "{\n  \"schema\":1,\n  \"id\":\"" + request.id()
                + "\",\n  \"attempt\":2,\n  \"prior_launch\":2,\n  \"authorized_launch\":3"
                + ",\n  \"control_base\":\"" + request.controlBase() + "\",\n  \"head\":\"" + head
                + "\",\n  \"tree\":\"" + tree + "\",\n  \"session\":\"" + request.session()
                + "\",\n  \"prior_model\":\"" + priorModel
                + "\",\n  \"authorized_model\":\"" + request.authorizedModel()
                + "\",\n  \"adoption_mode\":\"" + adoptionMode
                + "\",\n  \"classification\":\"provider-usage-limit\""
                + ",\n  \"contract_events\":0,\n  \"prior_receipt\":\"" + escape(prior.toString())
                + "\",\n  \"prior_receipt_sha256\":\"" + request.priorSha().toLowerCase()
                + "\",\n  \"adoption_receipt\":\"" + escape(adoption.toString())
                + "\",\n  \"adoption_sha256\":\"" + request.adoptionSha().toLowerCase()
                + "\",\n  \"session_evidence\":\"" + escape(sessionEvidence.toString())
                + "\",\n  \"session_evidence_sha256\":\"" + request.sessionEvidenceSha().toLowerCase()
                + "\",\n  \"provider_log_source\":\"" + escape(request.providerLog().toString())
                + "\",\n  \"provider_log_source_sha256\":\"" + occurrence.sourceSha()
                + "\",\n  \"provider_excerpt_sha256\":\"" + occurrence.excerptSha()
                + "\",\n  \"provider_excerpt\":" + lines
                + ",\n  \"seal_claim\":\"" + escape(claim.toString())
                + "\",\n  \"seal_claim_sha256\":\"" + claimSha
                + "\",\n  \"created\":\"" + Instant.now() + "\",\n  \"status\":\"PASS\"\n}\n";
    }

    private static String escape(String value) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\"' ->
                    result.append("\\\"");
                case '\\' ->
                    result.append("\\\\");
                case '\b' ->
                    result.append("\\b");
                case '\f' ->
                    result.append("\\f");
                case '\n' ->
                    result.append("\\n");
                case '\r' ->
                    result.append("\\r");
                case '\t' ->
                    result.append("\\t");
                default -> {
                    if (character < 0x20) {
                        result.append(String.format("\\u%04x", (int) character));
                    } else {
                        result.append(character);
                    }
                }
            }
        }
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
