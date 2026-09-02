package worldline.cli;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Verifies the bounded WPM-to-Worldline production instance binding. */
final class WorldlinePackageAttestation {
    private static final String CLIENT =
            "af1fa04b8006d3ef78c7e24f8de4aa56f439a74d7f314827529062d5bab6db4c";
    private static final String SERVER =
            "033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d";
    private static final List<String> ROOT_KEYS = List.of("schema", "runtime", "loader",
            "side", "official.artifacts", "lock.path", "lock.sha256", "package.count");

    private WorldlinePackageAttestation() { }

    static boolean verify(Path instance, PrintStream output) throws Exception {
        Path attestation = instance.resolve("wpm.worldline.properties");
        require(Files.isRegularFile(attestation, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(attestation) && Files.size(attestation) <= 1_048_576L,
                "WPM attestation is not a bounded regular file");
        String text = Files.readString(attestation, StandardCharsets.UTF_8);
        Map<String, String> values = parse(text); int count = integer(values.get("package.count"));
        require(count >= 0 && count <= 4_096, "WPM package count is invalid");
        List<String> keys = expectedKeys(count);
        require(new ArrayList<>(values.keySet()).equals(keys), "WPM attestation fields drifted");
        require("worldline.wpm-instance-attestation.v1".equals(values.get("schema"))
                && "b1.7.3".equals(values.get("runtime"))
                && values.get("loader").matches("[A-Za-z0-9._+-]{1,96}")
                && List.of("client", "server").contains(values.get("side"))
                && "external-only".equals(values.get("official.artifacts"))
                && "wpm.lock".equals(values.get("lock.path")),
                "WPM runtime identity or official-artifact policy drifted");
        Path lock = regular(instance, values.get("lock.path"), 16_777_216L);
        String lockText = Files.readString(lock, StandardCharsets.UTF_8);
        require(lockText.startsWith("{\n  \"schema\": \"worldline.package-lock.v1\",\n"
                + "  \"official_artifacts\": \"external-only\",\n")
                && lockText.endsWith("}\n") && lockText.indexOf('\0') < 0,
                "WPM production lock header drifted");
        require(hash(values.get("lock.sha256")) && values.get("lock.sha256")
                .equals(PinnedDownload.digest(lock)), "WPM production lock digest mismatch");
        verifyPackages(instance, values, count);
        rejectOfficialFiles(instance, values, count);
        require(text.equals(canonical(keys, values)), "WPM attestation is not canonical");
        output.println("+ WPM production instance verified: " + values.get("lock.sha256"));
        return true;
    }

    private static void verifyPackages(Path instance, Map<String, String> values, int count)
            throws Exception {
        Set<Path> paths = new HashSet<>(); long total = 0L;
        for (int index = 0; index < count; index++) {
            String prefix = "package." + index + ".";
            require(values.get(prefix + "id").matches("[a-z0-9._-]{1,96}")
                    && values.get(prefix + "version").matches("[A-Za-z0-9._+-]{1,96}"),
                    "WPM package identity is invalid");
            long size = number(values.get(prefix + "size"));
            require(size > 0L && size <= 1_073_741_824L, "WPM package size is invalid");
            Path payload = regular(instance, values.get(prefix + "path"), size);
            require(Files.size(payload) == size && paths.add(payload),
                    "WPM package size or path uniqueness drifted");
            String digest = values.get(prefix + "sha256");
            require(hash(digest) && digest.equals(PinnedDownload.digest(payload)),
                    "WPM package digest mismatch");
            total = Math.addExact(total, size);
            require(total <= 17_179_869_184L, "WPM instance exceeds aggregate size bound");
        }
    }

    private static Path regular(Path instance, String declared, long maximum) throws Exception {
        require(declared != null && declared.matches("[A-Za-z0-9/._+-]{1,240}"),
                "WPM instance path is invalid");
        Path relative = Path.of(declared).normalize();
        require(!relative.isAbsolute() && !relative.startsWith("..")
                && declared.equals(relative.toString().replace('\\', '/')),
                "WPM instance path escaped");
        Path target = instance.resolve(relative).normalize();
        require(target.startsWith(instance) && Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(target) && Files.size(target) <= maximum,
                "WPM instance file is not a bounded regular file");
        return target;
    }

    private static void rejectOfficialFiles(Path instance, Map<String, String> values, int count) {
        for (int index = 0; index < count; index++) {
            String digest = values.get("package." + index + ".sha256");
            require(!CLIENT.equals(digest) && !SERVER.equals(digest),
                    "WPM instance embedded an official runtime");
        }
        for (String path : List.of("minecraft.jar", "minecraft_server.jar",
                "tests/worldline/.local/oracles/b1.7.3/minecraft.jar",
                "tests/worldline/.local/oracles/b1.7.3/minecraft_server.jar"))
            require(!Files.exists(instance.resolve(path), LinkOption.NOFOLLOW_LINKS),
                    "WPM instance embedded an official runtime");
    }

    private static List<String> expectedKeys(int count) {
        List<String> keys = new ArrayList<>(ROOT_KEYS);
        for (int index = 0; index < count; index++) for (String suffix :
                List.of("id", "version", "path", "sha256", "size"))
            keys.add("package." + index + "." + suffix);
        return keys;
    }

    private static Map<String, String> parse(String text) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : text.split("\n", -1)) {
            if (line.isEmpty()) continue; int split = line.indexOf('=');
            require(split > 0 && line.indexOf('=', split + 1) < 0, "invalid WPM attestation line");
            String key = line.substring(0, split), value = line.substring(split + 1);
            require(key.matches("[a-z0-9.]+") && !value.isBlank()
                    && values.putIfAbsent(key, value) == null, "invalid WPM attestation field");
        }
        return values;
    }

    private static String canonical(List<String> keys, Map<String, String> values) {
        StringBuilder text = new StringBuilder();
        for (String key : keys) text.append(key).append('=').append(values.get(key)).append('\n');
        return text.toString();
    }
    private static int integer(String value) { long number = number(value);
        require(number <= Integer.MAX_VALUE, "WPM integer exceeds range"); return (int) number; }
    private static long number(String value) { try { long parsed = Long.parseLong(value);
        require(Long.toString(parsed).equals(value), "WPM number is not canonical"); return parsed;
    } catch (NumberFormatException error) { throw new IllegalArgumentException("invalid WPM number"); } }
    private static boolean hash(String value) { return value != null && value.matches("[0-9a-f]{64}"); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
