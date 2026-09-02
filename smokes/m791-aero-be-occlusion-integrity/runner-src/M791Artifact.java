import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** One complete rollback or candidate arm. */
final class M791Artifact {
    final Path game;
    final String arm;
    final int machines, captures, width, height, slices;
    final long atRest, pageCalls, animated, culled, submitted;
    final String[] hashes;

    private M791Artifact(Path game, Properties values) {
        this.game = game;
        arm = required(values, "arm");
        machines = integer(values, "machines");
        captures = integer(values, "captures");
        width = integer(values, "width");
        height = integer(values, "height");
        hashes = new String[captures];
        long rest = 0L, pages = 0L, animation = 0L, rejected = 0L;
        int stable = 0;
        for (int i = 0; i < captures; i++) {
            String prefix = "checkpoint." + i + ".";
            hashes[i] = required(values, prefix + "sha256");
            rest += number(values, prefix + "work");
            pages += number(values, prefix + "pagecalls");
            animation += number(values, prefix + "animated");
            rejected += number(values, prefix + "culled");
            stable = Math.max(stable, integer(values, prefix + "slices"));
        }
        atRest = rest;
        pageCalls = pages;
        animated = animation;
        culled = rejected;
        submitted = rest + animation;
        slices = stable;
    }

    static M791Artifact read(Path game, String expected) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(
                game.resolve("metrics-" + expected + ".properties"))) {
            values.load(reader);
        }
        M791Artifact result = new M791Artifact(game, values);
        SmokeSupport.require(expected.equals(result.arm), "M791 artifact identity drift");
        return result;
    }

    void verify() throws Exception {
        SmokeSupport.require(machines == 512 && captures == 12 && width > 0 && height > 0
            && submitted > 0L && pageCalls > 0L && atRest > 0L && animated > 0L,
            "M791 incomplete artifact: " + summary());
        for (int i = 0; i < captures; i++) {
            byte[] pixels = pixels(i);
            SmokeSupport.require(pixels.length == width * height * 4
                && sha256(pixels).equals(hashes[i]), "M791 framebuffer shape drift: " + i);
        }
    }

    byte[] pixels(int checkpoint) throws IOException {
        return Files.readAllBytes(game.resolve("visual-frames").resolve(arm)
            .resolve(String.format("checkpoint-%02d.rgba", checkpoint)));
    }

    String summary() {
        return arm + ":submitted=" + submitted + ",pages=" + pageCalls
            + ",culled=" + culled + ",slices=" + slices;
    }

    private static String sha256(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }
    private static int integer(Properties values, String key) {
        return Integer.parseInt(required(values, key));
    }
    private static long number(Properties values, String key) {
        return Long.parseLong(required(values, key));
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M791 " + key);
        return value.trim();
    }
}
