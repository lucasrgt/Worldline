import java.nio.file.Path;
import java.util.List;

/** Verifies platform-neutral ref classification without executing a push gate. */
public final class PrePushCheckTest {
    public static void main(String[] arguments) {
        Path root = Path.of("").toAbsolutePath().normalize();
        try {
            String head = capture(root, "rev-parse", "HEAD");
            String parent = capture(root, "rev-parse", "HEAD^");
            PrePushCheck.Decision main = PrePushCheck.inspect(root,
                    List.of(line("refs/heads/main", head, "refs/heads/main", parent)));
            require(main.guarded() && head.equals(main.sha()), "main was not guarded");
            PrePushCheck.Decision codex = PrePushCheck.inspect(root,
                    List.of(line("refs/heads/codex/test", head, "refs/heads/codex/test", parent)));
            require(codex.guarded(), "codex branch was not guarded");
            require(PrePushCheck.milestonePath("smokes/m1-test/MAP.md"), "milestone path missed");
            require(!PrePushCheck.milestonePath("docs/M1.md"), "ordinary path was guarded");
            rejects(root, line("refs/heads/main", zeros(), "refs/heads/main", head));
            rejects(root, line("refs/heads/main", head, "refs/heads/main", parent),
                    line("refs/heads/codex/test", parent, "refs/heads/codex/test", parent));
            System.out.println("  Java pre-push policy self-test: passed");
        } catch (Exception error) {
            System.err.println("Java pre-push policy self-test failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void rejects(Path root, String... lines) throws Exception {
        try {
            PrePushCheck.inspect(root, List.of(lines));
        } catch (IllegalStateException expected) { return; }
        throw new IllegalStateException("invalid push was accepted");
    }

    private static String capture(Path root, String... arguments) throws Exception {
        java.util.ArrayList<String> command = new java.util.ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        return ProcessCapture.require(root, command, 60).trim();
    }

    private static String line(String localRef, String localSha, String remoteRef, String remoteSha) {
        return String.join(" ", localRef, localSha, remoteRef, remoteSha);
    }

    private static String zeros() { return "0000000000000000000000000000000000000000"; }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
