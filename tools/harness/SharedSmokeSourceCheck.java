import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Rejects copied Aero paging mixins and proves the two intentional shared variants. */
final class SharedSmokeSourceCheck {
    private static final List<String> BASE = List.of("m78-paged-stage-timing",
            "m95-page-capacity-thrash", "m96-page-capacity-two-thrash",
            "m97-page-capacity-one-thrash", "m98-zero-capacity-floor",
            "m99-rebuild-budget-fallback", "m100-rebuild-budget-one-fallback",
            "m101-rebuild-budget-zero-direct", "m102-unlimited-rebuild-sentinel");
    private static final List<String> DIRECT = List.of("m103-pages-disabled-immediate-direct",
            "m104-paired-pages-control", "m105-paired-cache-capacity",
            "m106-paired-min-instances", "m107-paired-skip-individual",
            "m108-paired-cell-size", "m109-cell-size-floor", "m110-cell-size-ceiling");

    static void execute(Path root) throws Exception {
        for (String id : BASE) verify(root, id, "aero-paged-base", false);
        for (String id : DIRECT) verify(root, id, "aero-paged-direct", true);
        try (var paths = Files.walk(root.resolve("smokes"))) {
            List<Path> copies = paths.filter(path -> path.getFileName().toString()
                    .equals("WorldlinePagedAeroMixin.java")).toList();
            require(copies.size() == 2, "Aero paging mixin copies drifted: " + copies.size());
        }
        System.out.println("  shared Aero paging mixins: 17 consumers, 2 variants");
    }

    private static void verify(Path root, String id, String variant, boolean direct) throws Exception {
        Path smoke = root.resolve("smokes").resolve(id);
        require(!Files.exists(smoke.resolve(
                "runtime-src/worldline/m74/mixin/WorldlinePagedAeroMixin.java")),
                "copied Aero paging mixin: " + id);
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
            values.load(reader);
        }
        String shared = "smokes/shared/" + variant;
        require(shared.equals(values.getProperty("shared.inputs")), "shared input drift: " + id);
        Path source = root.resolve(shared).resolve(
                "runtime-src/worldline/m74/mixin/WorldlinePagedAeroMixin.java");
        String text = Files.readString(source, StandardCharsets.UTF_8);
        require(text.contains("immediateDirect()") == direct, "shared mixin variant drift: " + id);
        try (var files = Files.list(smoke)) {
            List<Path> scripts = files.filter(path -> path.toString().endsWith(".gradle"))
                    .filter(path -> read(path).contains("smokes/shared/" + variant)).toList();
            require(scripts.size() == 1, "shared source routing drift: " + id);
        }
        require(digest(source).matches("[0-9a-f]{64}"), "shared source digest failed");
    }

    private static String read(Path path) {
        try { return Files.readString(path, StandardCharsets.UTF_8); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }

    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
