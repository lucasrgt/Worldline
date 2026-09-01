import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** Builds the non-authoritative static evidence explorer from tracked sources. */
public final class EvidenceExplorer {
    private EvidenceExplorer() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: EvidenceExplorer");
            Path root = Path.of("").toAbsolutePath().normalize();
            EvidenceExplorerSnapshot snapshot = EvidenceExplorerSnapshot.build(root);
            Path output = root.resolve(".worldline/reports/evidence-explorer");
            write(output.resolve("evidence.json"), EvidenceExplorerFormat.json(snapshot));
            write(output.resolve("index.html"), EvidenceExplorerFormat.html(snapshot));
            System.out.println("evidence explorer built: " + output + "; smokes=" + snapshot.smokes().size()
                    + "; dispositions=" + snapshot.dispositions().size() + "; orphan-proofs="
                    + snapshot.orphanProofs().size());
        } catch (Exception error) {
            System.err.println("evidence explorer failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void write(Path target, String value) throws Exception {
        Files.createDirectories(target.getParent()); Path temporary = target.resolveSibling(
                target.getFileName() + ".tmp-" + ProcessHandle.current().pid());
        Files.deleteIfExists(temporary);
        try {
            Files.writeString(temporary, value, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
