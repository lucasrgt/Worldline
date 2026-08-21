package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Preserving migration from worldline-tests/ into tests/worldline/. */
final class WorldlineProjectMigrate {
    private WorldlineProjectMigrate() {}
    static int run(String[] arguments, PrintStream output) throws Exception {
        boolean noWrapper = false; Path repository = Paths.get("").toAbsolutePath().normalize();
        for (String item : arguments) {
            if (item.equals("--no-wrapper")) noWrapper = true;
            else if (item.startsWith("--root=")) repository = Paths.get(item.substring(7)).toAbsolutePath().normalize();
            else throw new IllegalArgumentException("unknown migrate option: " + item);
        }
        Path old = repository.resolve("worldline-tests"), target = repository.resolve("tests/worldline");
        require(Files.isDirectory(old), "worldline-tests directory is absent");
        require(!Files.exists(target), "migration target already exists");
        List<String> init = new ArrayList<>(); init.add("--target=" + target); init.add("--host-only");
        if (noWrapper) init.add("--no-wrapper");
        WorldlineProjectInit.run(init.toArray(new String[0]), output);
        Files.deleteIfExists(target.resolve("src/test/java/example/ExampleWorldlineTest.java"));
        Path source = old.resolve("src/test/java");
        if (Files.isDirectory(source)) copyTree(source, target.resolve("src/test/java"));
        Path snapshots = old.resolve("src/test/snapshots");
        if (Files.isDirectory(snapshots)) copyTree(snapshots, target.resolve("snapshots"));
        Path legacy = repository.resolve("worldline-test.properties");
        if (Files.isRegularFile(legacy)) Files.copy(legacy,
                target.resolve("legacy-worldline-test.properties"), StandardCopyOption.COPY_ATTRIBUTES);
        output.println("WORLDLINE_MIGRATE=PASS");
        output.println("legacy launcher/config retained for manual removal after verification"); return 0;
    }
    private static void copyTree(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.naturalOrder())::iterator) {
                Path destination = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) Files.createDirectories(destination);
                else { Files.createDirectories(destination.getParent());
                    Files.copy(path, destination, StandardCopyOption.COPY_ATTRIBUTES); }
            }
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
