import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Bounded process capture shared by swarm coordination tools. */
final class SwarmProcess {
    private SwarmProcess() { }

    static String output(Path directory, List<String> arguments, int seconds) throws Exception {
        Result result = capture(directory, arguments, seconds);
        if (result.exitCode() != 0) {
            throw new IllegalStateException(String.join(" ", arguments) + " failed with "
                    + result.exitCode() + ":\n" + result.output());
        }
        return result.output();
    }

    static Result capture(Path directory, List<String> arguments, int seconds) throws Exception {
        List<String> command = new ArrayList<>();
        if (arguments.get(0).equals("git") || arguments.get(0).equals("csm")) command.addAll(arguments);
        else { command.add("git"); command.addAll(arguments); }
        Path output = Files.createTempFile("worldline-swarm-process-out-", ".log");
        Path error = Files.createTempFile("worldline-swarm-process-err-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectOutput(output.toFile()).redirectError(error.toFile()).start();
        try {
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
            }
            return new Result(process.exitValue(), Files.readString(output, StandardCharsets.UTF_8),
                    Files.readString(error, StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(output);
            Files.deleteIfExists(error);
        }
    }

    static void run(Path directory, List<String> arguments, int seconds) throws Exception {
        output(directory, arguments, seconds);
    }

    static int status(Path directory, List<String> arguments, int seconds) throws Exception {
        List<String> command = new ArrayList<>();
        if (arguments.get(0).equals("git") || arguments.get(0).equals("csm")) command.addAll(arguments);
        else { command.add("git"); command.addAll(arguments); }
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private static void destroy(Process process) {
        process.descendants().sorted(java.util.Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    record Result(int exitCode, String stdout, String stderr) {
        String output() { return stdout + stderr; }
    }
}
