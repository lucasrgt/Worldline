import java.nio.file.Files;
import java.nio.file.Path;

/** Focused lexer and fail-closed self-test for the smoke statement policy. */
final class SmokeStatementBudgetTest {
    private SmokeStatementBudgetTest() { }

    static void execute() throws Exception {
        String source = "int first = 0; int second = 1; // ignored ;\n"
                + "String signature = \"one;two\"; /* ignored ; */\n"
                + "for (int index = 0; index < 1; index++) first++;\n"
                + "char semicolon = ';';\n";
        SmokeStatementBudget.Metrics metrics = SmokeStatementBudget.measure(source);
        require(metrics.statements == 7, "statement lexer counted comments or literals");
        require(metrics.packedLines == 1, "packed-line lexer drifted");
        require(metrics.maximumPerLine == 2, "statement density lexer drifted");
        rejectsUnreviewedDebt();
        System.out.println("  smoke statement budget self-test: passed");
    }

    private static void rejectsUnreviewedDebt() throws Exception {
        Path root = Files.createTempDirectory("worldline-statement-budget-");
        try {
            Files.createDirectories(root.resolve("quality"));
            Files.createDirectories(root.resolve("tools/smoke"));
            Files.createDirectories(root.resolve("smokes/example/src"));
            Files.writeString(root.resolve("harness.properties"),
                    "smoke.runner.max.statements=3\nsmoke.max.statements=2\n");
            Files.writeString(root.resolve("quality/source-policy.properties"), "unused=true\n");
            Files.writeString(root.resolve("quality/smoke-statement-debt.properties"), "");
            Path runner = root.resolve("tools/smoke/Example.java");
            Path scenario = root.resolve("smokes/example/src/Example.java");
            Files.writeString(runner, "int a; int b; int c; int d;\n");
            Files.writeString(scenario, "int a;\n");
            rejects(() -> new SmokeStatementBudget(root).candidate(runner, scenario.getParent().getParent()));
            Files.writeString(runner, "int a;\n");
            Files.writeString(scenario, "int a; int b; int c;\n");
            rejects(() -> new SmokeStatementBudget(root).candidate(runner, scenario.getParent().getParent()));
        } finally { SafeTreeDelete.delete(root); }
    }

    private static void rejects(Checked action) throws Exception {
        try { action.run(); throw new IllegalStateException("expected statement budget rejection"); }
        catch (IllegalStateException expected) {
            require(!expected.getMessage().equals("expected statement budget rejection"),
                    "statement budget accepted unreviewed debt");
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private interface Checked { void run() throws Exception; }
}
