import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Argument files keep complete multi-adapter candidate closures below Windows process limits. */
final class CandidateCompiler {
    private CandidateCompiler() { }
    static String compile(Path root, List<String> command, int timeout) throws Exception {
        Path directory = root.resolve(".worldline"); Files.createDirectories(directory);
        Path arguments = Files.createTempFile(directory, "candidate-javac-", ".args");
        try {
            List<String> lines = new ArrayList<>();
            for (String value : command.subList(1, command.size())) {
                lines.add("\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
            }
            Files.write(arguments, lines, StandardCharsets.UTF_8);
            return ProcessCapture.require(root, List.of(command.get(0), "@" + arguments), timeout);
        } finally { Files.deleteIfExists(arguments); }
    }
    static void selfTest() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path directory = Files.createTempDirectory(root.resolve(".worldline"), "candidate compiler spaces ");
        try {
            Path source = directory.resolve("ArgumentProbe.java");
            Files.writeString(source, "final class ArgumentProbe {}\n", StandardCharsets.UTF_8);
            String classpath = String.join(System.getProperty("path.separator"),
                    java.util.Collections.nCopies(500, directory.resolve("absent dependency").toString()));
            if (classpath.length() < 32768) throw new AssertionError("compiler limit regression setup");
            String javac = Path.of(System.getProperty("java.home"), "bin",
                    System.getProperty("os.name").startsWith("Windows") ? "javac.exe" : "javac").toString();
            compile(root, List.of(javac, "-classpath", classpath, "-d", directory.toString(), source.toString()), 60);
            if (!Files.isRegularFile(directory.resolve("ArgumentProbe.class"))) {
                throw new AssertionError("long candidate invocation did not compile");
            }
        } finally { SafeTreeDelete.delete(directory); }
    }
}
