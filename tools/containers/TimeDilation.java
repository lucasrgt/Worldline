import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Computes the reviewed opt-in time-dilation environment for pooled Linux containers. */
final class TimeDilation {
    static final String CONFIG = "tools/containers/time-dilation.properties";
    private TimeDilation() { }

    record Dilation(int factor, Path preload) {
        static final Dilation DISABLED = new Dilation(1, null);
        boolean enabled() { return factor > 1; }

        /** Extra docker arguments; empty when disabled so invocations stay byte-identical. */
        List<String> arguments() {
            if (!enabled()) return List.of();
            return List.of("--mount", "type=bind,source=" + preload
                            + ",target=/runtime/faketime/libfaketime.so,readonly",
                    "--env", "LD_PRELOAD=/runtime/faketime/libfaketime.so",
                    "--env", "FAKETIME=+0 x" + factor,
                    "--env", "FAKETIME_NO_CACHE=1");
        }
    }

    /** Absent configuration means disabled; a present configuration must be fully valid. */
    static Dilation load(Path root) throws Exception {
        Path config = root.resolve(CONFIG);
        if (!Files.isRegularFile(config)) return Dilation.DISABLED;
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("1".equals(values.getProperty("schema")), "invalid time dilation schema");
        int factor = factor(values.getProperty("factor", ""));
        if (factor == 1) return Dilation.DISABLED;
        Path preload = Path.of(values.getProperty("preload", "").trim());
        require(preload.isAbsolute() && Files.isRegularFile(preload)
                        && preload.toString().endsWith(".so"),
                "time dilation preload must be an existing absolute libfaketime .so");
        return new Dilation(factor, preload);
    }

    private static int factor(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed >= 1 && parsed <= 40) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalStateException("time dilation factor must be an integer between 1 and 40");
    }

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 1 && arguments[0].equals("--self-test"),
                "usage: TimeDilation --self-test");
        Path root = Files.createTempDirectory("worldline-time-dilation-");
        Path config = root.resolve(CONFIG);
        try {
            require(!TimeDilation.load(root).enabled()
                            && TimeDilation.load(root).arguments().isEmpty(),
                    "absent configuration must disable dilation");
            Files.createDirectories(config.getParent());
            Path preload = Files.write(root.resolve("libfaketime.so"), new byte[] {127});
            write(config, "schema=1\nfactor=20\npreload=" + escaped(preload) + "\n");
            Dilation enabled = TimeDilation.load(root);
            require(enabled.enabled() && enabled.arguments().contains("FAKETIME=+0 x20")
                            && enabled.arguments().contains("FAKETIME_NO_CACHE=1"),
                    "enabled dilation environment drifted");
            write(config, "schema=1\nfactor=1\npreload=" + escaped(preload) + "\n");
            require(!TimeDilation.load(root).enabled(), "factor one must stay disabled");
            for (String invalid : new String[] {"schema=1\nfactor=41\n", "schema=1\nfactor=x\n",
                    "schema=1\nfactor=20\npreload=missing.so\n", "factor=20\n"}) {
                write(config, invalid);
                boolean rejected = false;
                try { TimeDilation.load(root); }
                catch (IllegalStateException expected) { rejected = true; }
                require(rejected, "invalid time dilation configuration was accepted");
            }
            System.out.println("time dilation self-test: passed");
        } finally {
            Files.deleteIfExists(config);
            Files.deleteIfExists(root.resolve("libfaketime.so"));
            Files.deleteIfExists(config.getParent());
            Files.deleteIfExists(config.getParent().getParent());
            Files.deleteIfExists(root);
        }
    }

    private static void write(Path config, String content) throws Exception {
        Files.writeString(config, content, StandardCharsets.UTF_8);
    }

    private static String escaped(Path path) {
        return path.toAbsolutePath().toString().replace("\\", "\\\\");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
