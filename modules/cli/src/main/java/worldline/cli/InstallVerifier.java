package worldline.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Compares installed TestKit artifacts with the public release checksums. */
final class InstallVerifier {
    private InstallVerifier() { }

    static int run(String[] arguments, PrintStream output) throws Exception {
        require(arguments.length <= 1, "usage: worldline verify-install [distribution-directory]");
        String version = System.getProperty("worldline.install.version",
                InstallVerifier.class.getPackage().getImplementationVersion());
        require(version != null && version.matches("[0-9]+(?:[.][0-9]+){2}"),
                "runner has no valid implementation version");
        Path directory = arguments.length == 1 ? Path.of(arguments[0]).toAbsolutePath().normalize()
                : codeSource().getParent();
        String defaultPins = "https://github.com/lucasrgt/Worldline/releases/download/testkit-v"
                + version + "/checksums.properties";
        URI pins = URI.create(System.getProperty("worldline.install.pins", defaultPins));
        Properties expected = new Properties();
        try (InputStream input = pins.toURL().openStream()) { expected.load(input); }
        require("1".equals(expected.getProperty("format")), "published checksum format drifted");
        output.println("WORLDLINE_VERIFY_INSTALL");
        output.println("version=" + version); output.println("pins=" + pins);
        for (String artifact : new String[] {"worldline-test-api", "worldline-test-runner"}) {
            String name = artifact + "-" + version + ".jar";
            String pinned = expected.getProperty(name, ""); Path file = directory.resolve(name);
            require(pinned.matches("[0-9a-f]{64}"), "missing published pin for " + name);
            require(Files.isRegularFile(file), "missing installed artifact " + file);
            String installed = digest(file);
            output.println(artifact + ".installed.sha256=" + installed);
            output.println(artifact + ".published.sha256=" + pinned);
            require(installed.equals(pinned), "installed artifact differs from published pin: " + name);
        }
        output.println("WORLDLINE_VERIFY_INSTALL=PASS"); return 0;
    }

    private static Path codeSource() throws Exception {
        return Path.of(InstallVerifier.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toAbsolutePath().normalize();
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
