import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Qualifies identical workspace transaction evidence on Windows and WSL ext4. */
public final class WorkspaceTransactionQualification {
    private static final Pattern DIGEST = Pattern.compile("digest=([0-9a-f]{64})");

    private WorkspaceTransactionQualification() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: WorkspaceTransactionQualification");
            execute();
        } catch (Exception error) {
            System.err.println("workspace transaction qualification failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void execute() throws Exception {
        require(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win"),
                "cross-platform workspace qualification requires a Windows host with WSL");
        Path root = Path.of("").toAbsolutePath().normalize();
        String windowsFilesystem = Files.getFileStore(root).type();
        require(windowsFilesystem.toLowerCase(Locale.ROOT).contains("ntfs"),
                "Windows workspace is not on NTFS: " + windowsFilesystem);
        String windowsDigest = WorkspaceTransaction.selfTest();
        String source = linuxPath(root.resolve("tools/containers/WorkspaceTransaction.java"));
        Result filesystem = capture(List.of("wsl.exe", "--exec", "findmnt", "-n", "-o",
                "FSTYPE", "-T", "/var/tmp"), 30);
        require(filesystem.passed && filesystem.output.strip().equals("ext4"),
                "WSL transaction root is not ext4: " + filesystem.output);
        Result linux = capture(List.of("wsl.exe", "--exec", "java",
                "-Djava.io.tmpdir=/var/tmp", source, "--self-test"), 120);
        require(linux.passed, "WSL ext4 transaction failed:\n" + linux.output);
        Matcher digest = DIGEST.matcher(linux.output);
        require(digest.find() && windowsDigest.equals(digest.group(1)),
                "Windows/Linux transaction evidence diverged");
        System.out.println("workspace transaction qualification passed; windows="
                + windowsFilesystem + "; linux=ext4; digest=" + windowsDigest);
    }

    private static String linuxPath(Path source) throws Exception {
        require(source.isAbsolute() && source.normalize().equals(source) && Files.isRegularFile(source),
                "workspace transaction source is invalid");
        Result converted = capture(List.of("wsl.exe", "--exec", "wslpath", "-a", "-u",
                source.toString()), 30);
        String value = converted.output.strip();
        require(converted.passed && value.startsWith("/mnt/") && !value.contains("\n")
                        && value.endsWith("/tools/containers/WorkspaceTransaction.java"),
                "WSL source path conversion failed: " + converted.output);
        return value;
    }

    private static Result capture(List<String> command, long seconds) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        Thread reader = Thread.startVirtualThread(() -> {
            try { process.getInputStream().transferTo(output); }
            catch (java.io.IOException error) { throw new IllegalStateException(error); }
        });
        boolean finished = process.waitFor(seconds, TimeUnit.SECONDS);
        if (!finished) ProcessTreeTermination.kill(process, "workspace transaction qualification");
        reader.join(TimeUnit.SECONDS.toMillis(10));
        require(finished && !reader.isAlive(), "workspace transaction probe timed out");
        return new Result(process.exitValue() == 0, output.toString(StandardCharsets.UTF_8));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    private record Result(boolean passed, String output) { }
}
