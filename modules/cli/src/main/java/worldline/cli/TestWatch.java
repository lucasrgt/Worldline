package worldline.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import worldline.testkit.TestResult;
import worldline.testkit.TestRunResult;
import worldline.testkit.TestStatus;

/** Deliberately line-oriented watch shell so it also works on plain terminals. */
final class TestWatch {
    private TestWatch() {}
    static int run(TestCommand.Parsed parsed, PrintStream output, PrintStream error)
            throws IOException, ReflectiveOperationException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        TestRunResult last = TestCommand.execute(parsed, output); help(output);
        long fingerprint = TestSourceFingerprint.read(parsed.source);
        while (true) {
            output.print("worldline test watch> "); output.flush();
            while (!input.ready()) {
                try { Thread.sleep(250L); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); return 130; }
                long next = TestSourceFingerprint.read(parsed.source);
                if (next != fingerprint) {
                    fingerprint = next; output.println("source changed; rerunning");
                    last = TestCommand.execute(parsed, output);
                    output.print("worldline test watch> "); output.flush();
                }
            }
            String command = input.readLine();
            if (command == null || command.equals("q")) return last.passed() ? 0 : 1;
            if (command.equals("h")) { help(output); continue; }
            if (command.equals("o")) { artifacts(last, output, error); continue; }
            if (command.equals("v")) { divergence(last, output); continue; }
            if (command.equals("p")) {
                output.print("file regex> "); output.flush(); parsed.file = Pattern.compile(input.readLine());
            } else if (command.equals("t")) {
                output.print("test regex> "); output.flush(); parsed.name = Pattern.compile(input.readLine());
            } else if (command.equals("f")) parsed.name = failed(last);
            else if (command.equals("a")) { parsed.name = null; parsed.file = null; }
            else if (command.equals("u")) parsed.update = true;
            else if (command.equals("m")) { parsed.name = lastFailure(last); parsed.minimize = true; }
            else if (!command.equals("r")) { error.println("unknown watch command: " + command); continue; }
            last = TestCommand.execute(parsed, output); parsed.update = false; parsed.minimize = false;
        }
    }
    private static Pattern failed(TestRunResult result) {
        List<String> paths = new ArrayList<>();
        for (TestResult test : result.tests()) if (test.status() == TestStatus.FAILED
                || test.status() == TestStatus.FLAKY || test.status() == TestStatus.INTERRUPTED)
            paths.add(Pattern.quote(test.path()));
        return Pattern.compile(paths.isEmpty() ? "(?!)" : String.join("|", paths));
    }
    private static Pattern lastFailure(TestRunResult result) {
        List<TestResult> tests = result.tests();
        for (int index = tests.size() - 1; index >= 0; index--) {
            TestResult test = tests.get(index);
            if (test.status() == TestStatus.FAILED || test.status() == TestStatus.FLAKY
                    || test.status() == TestStatus.INTERRUPTED) return Pattern.compile(Pattern.quote(test.path()));
        }
        return Pattern.compile("(?!)");
    }
    private static void artifacts(TestRunResult result, PrintStream output, PrintStream error) {
        for (TestResult test : result.tests()) for (java.nio.file.Path path : test.artifacts()) {
            java.nio.file.Path directory = path.toAbsolutePath().normalize().getParent();
            output.println(directory);
            if (directory != null && Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) try {
                Desktop.getDesktop().open(directory.toFile()); return;
            } catch (IOException failure) { error.println("cannot open artifact directory: " + failure.getMessage()); }
            return;
        }
        output.println("No artifacts are available.");
    }
    private static void divergence(TestRunResult result, PrintStream output) {
        for (TestResult test : result.tests()) if (test.status() == TestStatus.FAILED) {
            output.println(test.path()); output.println("Expected: " + test.expected());
            output.println("Received: " + test.received());
            if (test.divergenceTick() >= 0) {
                output.println("Tick: " + test.divergenceTick()); output.println("Role: " + test.divergenceRole());
                output.println("Field: " + test.divergenceField());
            }
            return;
        }
        output.println("No failed test has a divergence.");
    }
    private static void help(PrintStream output) {
        output.println("a all | r rerun | f failures | u update snapshots | p spec filter | t test filter");
        output.println("m minimize | o artifacts | v divergence | q quit | h help");
    }
}
