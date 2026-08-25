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
        List<String> command = new ArrayList<>();
        if (arguments.get(0).equals("git") || arguments.get(0).equals("csm")) command.addAll(arguments);
        else { command.add("git"); command.addAll(arguments); }
        Path log = Files.createTempFile("worldline-swarm-process-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
            }
            String output = Files.readString(log, StandardCharsets.UTF_8);
            if (process.exitValue() != 0) throw new IllegalStateException(String.join(" ", command)
                    + " failed with " + process.exitValue() + ":\n" + output);
            return output;
        } finally { Files.deleteIfExists(log); }
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
}
