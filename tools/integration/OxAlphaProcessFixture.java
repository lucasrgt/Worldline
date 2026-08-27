import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Process-boundary fixtures used only by the Ox Alpha executable self-test. */
final class OxAlphaProcessFixture {
    private OxAlphaProcessFixture() {
    }

    static boolean run(String[] arguments) throws Exception {
        if (List.of(arguments).equals(List.of("--self-test-stdin-child"))) {
            System.in.readAllBytes();
            System.out.println("stdin closed");
            return true;
        }
        if (List.of(arguments).equals(List.of("--self-test-terminal-child"))) {
            System.out.println("{\"type\":\"tool_use\",\"title\":\"java tools/harness/Gate.java "
                    + "--milestone m1-contract\",\"metadata\":{\"exit\":1}}");
            System.out.flush();
            sleep();
            return true;
        }
        if (List.of(arguments).equals(List.of("--self-test-provider-child"))) {
            providerChild(false);
            return true;
        }
        if (List.of(arguments).equals(List.of("--self-test-provider-exit-child"))) {
            providerChild(true);
            return true;
        }
        if (arguments.length > 0 && arguments[0].equals("--self-test-sleeper-child")) {
            sleep();
            return true;
        }
        if (arguments.length > 0 && arguments[0].equals("--self-test-middle-child")) {
            middleChild(arguments.length > 1 ? arguments[1] : null);
            return true;
        }
        if (arguments.length == 2 && arguments[0].equals("--self-test-launcher-timeout-child")) {
            launcherTimeoutChild(arguments[1]);
            return true;
        }
        return false;
    }

    private static void providerChild(boolean naturalExit) throws Exception {
        Process child = new ProcessBuilder(OxAlphaWorker.javaTool(), "-cp",
                System.getProperty("java.class.path"), "OxAlphaWorker",
                "--self-test-middle-child").redirectError(ProcessBuilder.Redirect.INHERIT).start();
        child.getOutputStream().close();
        System.err.println("level=INFO message=created id=ses_providerchild");
        System.err.println("selftest.child.pid=" + child.pid());
        System.err.flush();
        Thread.sleep(500);
        String failure = "level=ERROR message=\"stream error\" providerID=opencode-go "
                + "modelID=glm-5.3-flash session.id=ses_providerchild "
                + "error.error=\"AI_APICallError: Monthly usage limit reached.\"";
        if (naturalExit) {
            System.err.print(failure);
        } else {
            System.err.println(failure);
        }
        System.err.flush();
        if (!naturalExit) {
            sleep();
        }
    }

    private static void middleChild(String marker) throws Exception {
        List<String> command = new ArrayList<>(List.of(OxAlphaWorker.javaTool(), "-cp",
                System.getProperty("java.class.path"), "OxAlphaWorker",
                "--self-test-sleeper-child"));
        if (marker != null) {
            command.add(marker);
        }
        Process grandchild = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        grandchild.getOutputStream().close();
        System.err.println("selftest.grandchild.pid=" + grandchild.pid());
        System.err.flush();
        if (marker != null) {
            Files.writeString(Path.of(marker), "middle=" + ProcessHandle.current().pid()
                    + "\ngrandchild=" + grandchild.pid() + "\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        }
        sleep();
    }

    private static void launcherTimeoutChild(String marker) throws Exception {
        Process child = new ProcessBuilder(OxAlphaWorker.javaTool(), "-cp",
                System.getProperty("java.class.path"), "OxAlphaWorker",
                "--self-test-middle-child", marker)
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        child.getOutputStream().close();
        sleep();
    }

    private static void sleep() throws InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
    }
}
