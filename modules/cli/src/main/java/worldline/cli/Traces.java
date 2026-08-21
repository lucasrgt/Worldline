package worldline.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import worldline.trace.CanonicalStateDocument;

/** Strict canonical trace file reader shared by CLI commands. */
final class Traces {
    private Traces() {}

    static CanonicalStateDocument read(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        Checks.require(bytes.length > 0 && bytes.length <= CanonicalStateDocument.MAX_CHARACTERS,
                "invalid trace file size");
        String value = new String(bytes, StandardCharsets.UTF_8);
        Checks.require(Arrays.equals(bytes, value.getBytes(StandardCharsets.UTF_8)),
                "trace is not strict UTF-8");
        return CanonicalStateDocument.parse(value);
    }
}
