import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Compiles the reflection-only Butter bridge without mapped Minecraft inputs. */
public final class ForeignUiContractCheck {
    private final Path root = Paths.get("").toAbsolutePath().normalize();

    public static void main(String[] arguments) throws Exception {
        new ForeignUiContractCheck().execute();
    }

    private void execute() throws Exception {
        Path output = root.resolve(".worldline/build/foreign-ui-contract");
        Files.createDirectories(output);
        Path api = root.resolve(".worldline/build/classes/api");
        List<String> compile = new ArrayList<String>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath", api.toString(),
                "-d", output.toString(), root.resolve(
                        "adapters/b173-client/src/main/java/worldline/b173/B173ForeignUi.java").toString()));
        compile.addAll(javaFiles(root.resolve("adapters/b173-client/contract-src")));
        run(compile);
        run(Arrays.asList("java", "-classpath", output + System.getProperty("path.separator") + api,
                "worldline.b173.B173ForeignUiContract"));
    }

    private List<String> javaFiles(Path source) throws IOException {
        try (Stream<Path> files = Files.walk(source)) {
            return files.filter(path -> path.toString().endsWith(".java")).sorted()
                    .map(Path::toString).collect(Collectors.toList());
        }
    }

    private void run(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output);
        System.out.print(output);
    }
}
