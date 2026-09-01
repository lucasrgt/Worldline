import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** One fresh-client loading and cold-render artifact. */
final class M787Artifact {
    final Path game;
    final String arm, loadSequence;
    final boolean pages, flattened;
    final int machines, frames, captures, width, height;
    final int loadStarts, buildingStages, simulatingStages, rendersDuringLoad;
    final int[] submitted, rebuilds, direct, cached, pageCalls;
    final String[] pageHashes, directHashes;

    private M787Artifact(Path game, Properties values) {
        this.game = game;
        arm = required(values, "arm");
        pages = Boolean.parseBoolean(required(values, "pages.enabled"));
        flattened = Boolean.parseBoolean(required(values, "pages.flattened"));
        machines = integer(values, "machines");
        frames = integer(values, "frames");
        captures = integer(values, "captures");
        width = integer(values, "width");
        height = integer(values, "height");
        loadStarts = integer(values, "loading.starts");
        buildingStages = integer(values, "loading.building.stages");
        simulatingStages = integer(values, "loading.simulating.stages");
        rendersDuringLoad = integer(values, "loading.renderworld.calls");
        loadSequence = required(values, "loading.sequence");
        submitted = ints(values, "frame", "submitted", frames);
        rebuilds = ints(values, "frame", "rebuilds", frames);
        direct = ints(values, "frame", "direct", frames);
        cached = ints(values, "frame", "cached", frames);
        pageCalls = ints(values, "frame", "pageCalls", frames);
        pageHashes = new String[captures];
        directHashes = new String[captures];
        for (int index = 0; index < captures; index++) {
            pageHashes[index] = required(values, "capture." + index + ".pages.sha256");
            directHashes[index] = required(values, "capture." + index + ".direct.sha256");
        }
    }

    static M787Artifact read(Path game, String expectedArm) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) {
            values.load(reader);
        }
        M787Artifact result = new M787Artifact(game, values);
        SmokeSupport.require(result.arm.equals(expectedArm), "M787 artifact identity drift");
        return result;
    }

    void verify() throws Exception {
        SmokeSupport.require(machines == 576 && frames == 240 && captures == 40
            && width > 0 && height > 0, "M787 artifact shape drift: " + summary());
        SmokeSupport.require(loadStarts == 3 && buildingStages == 1 && simulatingStages == 1
            && rendersDuringLoad == 0
            && loadSequence.equals("Loading level>>Loading level>Building terrain>Simulating world for a bit"),
            "M787 loading lifecycle drift: " + summary());
        SmokeSupport.require(!flattened, "M787 flattening unexpectedly active");
        for (int frame = 0; frame < frames; frame++) {
            SmokeSupport.require(submitted[frame] == 576,
                "M787 partial submission at frame " + frame);
        }
        SmokeSupport.require(pages && rebuilds[0] > 0 && direct[0] > 0
                && cached[frames - 1] > 0 && direct[frames - 1] == 0
                && pageCalls[frames - 1] == 576,
            "M787 cold page fallback/convergence absent: " + summary());
        for (int capture = 0; capture < captures; capture++) {
            verifyPixels("pages", capture, pageHashes[capture]);
            verifyPixels("direct", capture, directHashes[capture]);
        }
    }

    byte[] pixels(String mode, int capture) throws IOException {
        return Files.readAllBytes(game.resolve("cold-entry-frames")
            .resolve(String.format("%s-%02d.rgba", mode, capture)));
    }

    String summary() {
        return arm + ":pages=" + pages + ",loading=" + loadSequence
            + ",rendersDuringLoad=" + rendersDuringLoad + ",frames=" + frames
            + ",captures=" + captures + ",first=" + rebuilds[0] + "/" + direct[0]
            + ",cached=" + cached[frames - 1] + ",hot=" + pageCalls[frames - 1]
            + "/" + direct[frames - 1];
    }

    private void verifyPixels(String mode, int capture, String expected) throws Exception {
        byte[] pixels = pixels(mode, capture);
        SmokeSupport.require(pixels.length == width * height * 4
            && M787Runtime.sha256(pixels).equals(expected),
            "M787 " + mode + " framebuffer digest drift: " + capture);
    }

    private static int[] ints(Properties values, String prefix, String suffix, int count) {
        int[] result = new int[count];
        for (int index = 0; index < count; index++) {
            result[index] = integer(values, prefix + "." + index + "." + suffix);
        }
        return result;
    }

    private static int integer(Properties values, String key) {
        return Integer.parseInt(required(values, key));
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("missing M787 " + key);
        }
        return value.trim();
    }
}
