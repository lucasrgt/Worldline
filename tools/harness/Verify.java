import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Compatibility launcher that always enters the versioned repository gate. */
public final class Verify {
    private Verify() {}

    public static void main(String[] arguments) {
        List<String> command = new ArrayList<>();
        command.add(java()); command.add("tools/harness/Gate.java");
        command.addAll(Arrays.asList(arguments));
        Process process = null;
        try {
            process = new ProcessBuilder(command).inheritIO().start();
            System.exit(process.waitFor());
        } catch (InterruptedException error) {
            if (process != null) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            System.err.println("verify interrupted while entering the repository gate");
            System.exit(130);
        } catch (IOException error) {
            System.err.println("verify could not enter the repository gate: " + error.getMessage());
            System.exit(1);
        }
    }

    private static String java() {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        return Path.of(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java").toString();
    }
}
