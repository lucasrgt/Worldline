import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Determinism, validation, and escaping fixtures for the evidence explorer. */
final class EvidenceExplorerTest {
    private EvidenceExplorerTest() { }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-evidence-explorer-");
        try {
            fixture(root, "sample", "executed", "abc123", "cause <unsafe>");
            EvidenceExplorerSnapshot first = EvidenceExplorerSnapshot.build(root);
            String json = EvidenceExplorerFormat.json(first), html = EvidenceExplorerFormat.html(first);
            EvidenceExplorerSnapshot second = EvidenceExplorerSnapshot.build(root);
            require(json.equals(EvidenceExplorerFormat.json(second))
                    && html.equals(EvidenceExplorerFormat.html(second)), "explorer rebuild drift");
            require(json.contains("\"verdict_authority\":false")
                    && html.contains("cause &lt;unsafe&gt;") && !html.contains("cause <unsafe>"),
                    "explorer authority or escaping drift");
            Path orphan = root.resolve("smokes/qualification-evidence/orphan.proof");
            Files.writeString(orphan, "orphan=true\n", StandardCharsets.UTF_8);
            require(EvidenceExplorerFormat.json(EvidenceExplorerSnapshot.build(root))
                    .contains("\"orphan_proofs\":[\n    \"orphan.proof\""), "orphan proof was hidden");
            Files.delete(orphan);
            Path proof = root.resolve("smokes/qualification-evidence/sample.proof");
            Files.writeString(proof, Files.readString(proof) + "tampered=true\n", StandardCharsets.UTF_8);
            expectFailure(() -> EvidenceExplorerSnapshot.build(root));
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  evidence explorer self-test: passed");
    }

    private static void fixture(Path root, String id, String kind, String head, String cause) throws Exception {
        Path smokes = root.resolve("smokes"), proofRoot = smokes.resolve("qualification-evidence");
        Files.createDirectories(proofRoot); Files.createDirectories(smokes.resolve(id));
        Files.writeString(smokes.resolve(id).resolve("smoke.properties"),
                "id=" + id + "\nqualification.proof=tooling-cycle\n", StandardCharsets.UTF_8);
        String fingerprint = "a".repeat(64), observation = "b".repeat(64);
        Path proof = proofRoot.resolve(id + ".proof");
        Files.writeString(proof, "schema=1\nid=" + id + "\nfingerprint=" + fingerprint
                + "\nobservation.sha256=" + observation + "\nsource=executed\n", StandardCharsets.UTF_8);
        String digest = EvidenceExplorerSnapshot.sha256(proof);
        Files.writeString(smokes.resolve("qualification.lock"), "schema=5\nsmoke." + id
                + ".fingerprint=" + fingerprint + "\nsmoke." + id + ".observation_sha256=" + observation
                + "\nsmoke." + id + ".evidence_sha256=" + digest + "\nsmoke." + id
                + ".source=executed\nsmoke." + id + ".status=passed\n", StandardCharsets.UTF_8);
        Files.writeString(smokes.resolve("train-reconciliation.lock"), "schema=1\nbase=" + "c".repeat(40)
                + "\ncarried.count=0\nimported.count=0\nexecuted.count=1\npending.count=0\npin.count=1"
                + "\ncatalog.count=1\nsmoke." + id + ".current_fingerprint=" + fingerprint + "\nsmoke."
                + id + ".evidence_sha256=" + observation + "\nsmoke." + id + ".kind=" + kind
                + "\nsmoke." + id + ".prior_fingerprint=" + "d".repeat(64) + "\nsmoke." + id
                + ".receipt.head=" + head + "\n", StandardCharsets.UTF_8);
        Path dispositions = root.resolve("coordination/swarm/dispositions"); Files.createDirectories(dispositions);
        Files.writeString(dispositions.resolve(id + ".properties"), "id=" + id
                + "\ndisposition=REJECTED\ncause=" + cause + "\n", StandardCharsets.UTF_8);
    }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid explorer evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
