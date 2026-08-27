import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Provisions reviewed, content-addressed world templates into smoke workspaces. */
final class WorldTemplate {
    private WorldTemplate() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 3 && arguments[0].equals("--capture"),
                    "usage: WorldTemplate --capture WORKSPACE smokes/shared/worlds/NAME");
            Path root = Path.of("").toAbsolutePath().normalize();
            capture(root, Path.of(arguments[1]), arguments[2]);
        } catch (Exception error) {
            System.err.println("world template failed: " + error.getMessage());
            System.exit(1);
        }
    }

    /** Copies a verified tracked template into workspace/world before the first boot. */
    static Path provision(Path root, Path workspace, String template) throws Exception {
        Path source = directory(root, template);
        Properties manifest = StrictProperties.load(source.resolve("template.properties"));
        require("1".equals(manifest.getProperty("schema")), "invalid world template schema");
        String recorded = manifest.getProperty("content.sha256", "");
        require(recorded.equals(digest(source.resolve("world"))),
                "world template content drift: " + template);
        Path world = workspace.resolve("world");
        require(!Files.exists(world), "workspace already contains a world: " + workspace);
        copy(source.resolve("world"), world);
        return world;
    }

    /** Seals one captured world directory as an immutable reviewable template. */
    static void capture(Path root, Path workspace, String template) throws Exception {
        Path world = workspace.toAbsolutePath().normalize().resolve("world");
        require(Files.isDirectory(world), "missing captured world: " + world);
        require(template.matches("smokes/shared/worlds/[a-z0-9-]+"),
                "unsafe world template destination: " + template);
        Path destination = root.resolve(template);
        require(!Files.exists(destination), "world template already exists: " + template);
        copy(world, destination.resolve("world"));
        Files.deleteIfExists(destination.resolve("world").resolve("session.lock"));
        String content = digest(destination.resolve("world"));
        Files.writeString(destination.resolve("template.properties"),
                "schema=1\ncontent.sha256=" + content + "\n", StandardCharsets.UTF_8);
        System.out.println("world template sealed: " + template + " content=" + content);
    }

    private static Path directory(Path root, String template) {
        require(template.matches("smokes/shared/worlds/[a-z0-9-]+"),
                "unsafe world template: " + template);
        Path source = root.resolve(template).toAbsolutePath().normalize();
        require(source.startsWith(root) && Files.isDirectory(source),
                "missing world template: " + template);
        return source;
    }

    /** Content identity over sorted relative paths and exact bytes; binaries stay byte-exact. */
    static String digest(Path world) throws Exception {
        require(Files.isDirectory(world), "missing world template content: " + world);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        List<Path> files = SafeTreeDelete.paths(world).stream().filter(Files::isRegularFile)
                .sorted(Comparator.comparing(path -> world.relativize(path).toString()
                        .replace('\\', '/'))).toList();
        require(!files.isEmpty(), "empty world template content: " + world);
        for (Path file : files) {
            String relative = world.relativize(file).toString().replace('\\', '/');
            require(!relative.equals("session.lock"), "world template contains session.lock");
            digest.update(relative.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(Files.readAllBytes(file));
            digest.update((byte) 0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void copy(Path source, Path target) throws Exception {
        for (Path path : SafeTreeDelete.paths(source).stream().sorted().toList()) {
            require(!SafeTreeDelete.linkLike(path), "world template link rejected: " + path);
            Path destination = target.resolve(source.relativize(path).toString());
            if (Files.isDirectory(path)) Files.createDirectories(destination);
            else {
                Files.createDirectories(destination.getParent());
                Files.copy(path, destination);
            }
        }
    }

    static void selfTest() throws Exception {
        Path base = Files.createTempDirectory("worldline-world-template-");
        try {
            Path workspace = base.resolve("workspace");
            Files.createDirectories(workspace.resolve("world/region"));
            Files.write(workspace.resolve("world/level.dat"), new byte[] {10, 0, 4, 1});
            Files.write(workspace.resolve("world/region/r.0.0.mcr"), new byte[] {2, 0, 77});
            Files.writeString(workspace.resolve("world/session.lock"), "x",
                    StandardCharsets.UTF_8);
            capture(base, workspace, "smokes/shared/worlds/seed-selftest");
            Path fresh = base.resolve("fresh");
            Files.createDirectories(fresh);
            Path world = provision(base, fresh, "smokes/shared/worlds/seed-selftest");
            require(java.util.Arrays.equals(Files.readAllBytes(world.resolve("level.dat")),
                            new byte[] {10, 0, 4, 1})
                            && Files.isRegularFile(world.resolve("region/r.0.0.mcr"))
                            && !Files.exists(world.resolve("session.lock")),
                    "world template provision drifted");
            boolean occupied = false;
            try {
                provision(base, fresh, "smokes/shared/worlds/seed-selftest");
            } catch (IllegalStateException expected) {
                occupied = true;
            }
            Files.write(base.resolve("smokes/shared/worlds/seed-selftest/world/level.dat"),
                    new byte[] {9, 9});
            boolean drifted = false;
            try {
                provision(base, base.resolve("other"), "smokes/shared/worlds/seed-selftest");
            } catch (IllegalStateException expected) {
                drifted = true;
            }
            require(occupied && drifted, "world template drift was accepted");
            System.out.println("  world template self-test: passed");
        } finally {
            SafeTreeDelete.delete(base);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
