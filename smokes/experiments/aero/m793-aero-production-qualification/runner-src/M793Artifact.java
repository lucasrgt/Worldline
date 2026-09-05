import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** One complete rollback or candidate arm. */
final class M793Artifact {
    final Path game;
    final String arm;
    final int machines, captures, width, height, slices;
    final long atRest, pageCalls, animated, culled, submitted;
    final String[] hashes;

    private M793Artifact(Path game, Properties values) {
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
        // Non-flattened, fixed-light Cell Pages issue one call per submitted model.
        submitted = rest + pages + animation;
        slices = stable;
    }

    static M793Artifact read(Path game, String expected) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(
                game.resolve("metrics-" + expected + ".properties"))) {
            values.load(reader);
        }
        M793Artifact result = new M793Artifact(game, values);
        SmokeSupport.require(expected.equals(result.arm), "M793 artifact identity drift");
        return result;
    }

    void verify() throws Exception {
        SmokeSupport.require(machines == 512 && captures == 36 && width > 0 && height > 0
            && submitted > 0L && pageCalls > 0L && atRest >= 0L && animated > 0L,
            "M793 incomplete artifact: " + summary());
        for (int i = 0; i < captures; i++) {
            byte[] pixels = pixels(i);
            verifyPixels(pixels);
            SmokeSupport.require(pixels.length == width * height * 4
                && sha256(pixels).equals(hashes[i]), "M793 framebuffer shape drift: " + i);
        }
    }

    private static void verifyPixels(byte[] pixels) {
        boolean varied = false, alpha = false;
        for (int i = 0; i < pixels.length; i += 4) {
            alpha |= pixels[i + 3] != 0;
            varied |= pixels[i] != pixels[0] || pixels[i + 1] != pixels[1] || pixels[i + 2] != pixels[2];
        }
        SmokeSupport.require(alpha && varied, "M793 empty or constant framebuffer");
    }

    static void selfTest() {
        verifyPixels(new byte[] {0, 0, 0, -1, 1, 2, 3, -63});
        boolean rejected = false;
        try { verifyPixels(new byte[8]); } catch (IllegalStateException expected) { rejected = true; }
        SmokeSupport.require(rejected, "zero readback accepted");
        rejected = false;
        try { verifyPixels(new byte[] {1, 2, 3, -1, 1, 2, 3, -1}); }
        catch (IllegalStateException expected) { rejected = true; }
        SmokeSupport.require(rejected, "constant readback accepted");
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
        return M793Digest.sha256Hex(value);
    }
    private static int integer(Properties values, String key) {
        return Integer.parseInt(required(values, key));
    }
    private static long number(Properties values, String key) {
        return Long.parseLong(required(values, key));
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M793 " + key);
        return value.trim();
    }
}
