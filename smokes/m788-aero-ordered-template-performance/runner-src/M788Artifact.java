import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Qualifies Aero's safe template-memory Cell Page preset in four fresh GPU clients. */
final class M788Artifact {
    final Path game;
    final String arm;
    final int machines, frames, captures, blankCaptures, width, height;
    final int pageCalls, pageRebuilds, directCalls, cachedMax, compiled, expired, evicted;
    final int lastPageCalls, lastPageRebuilds, lastDirectCalls;
    final int ttl, rebuildBudget, cacheMax, prewarmPending, displayDenied, displayFailed;
    final int displayLive, displayPeak, displayAllocated, displayMax;
    final long allocatedBytes, heapPeak, heapFinal, renderCalls, renderNanos, submittedMachines;
    final boolean flattened, prewarmEnabled;
    final long[] walls, allocations;
    final String[] hashes;

    private M788Artifact(Path game, Properties p, long[][] rows) {
        this.game = game;
        arm = required(p, "arm");
        machines = integer(p, "machines");
        frames = integer(p, "frames");
        captures = integer(p, "captures");
        blankCaptures = integer(p, "captures.blank.rejected");
        width = integer(p, "width");
        height = integer(p, "height");
        allocatedBytes = number(p, "frame.allocated.bytes");
        heapPeak = number(p, "heap.peak.bytes");
        heapFinal = number(p, "heap.final.bytes");
        pageCalls = integer(p, "page.calls");
        pageRebuilds = integer(p, "page.rebuilds");
        directCalls = integer(p, "page.direct");
        cachedMax = integer(p, "page.cached.max");
        lastPageCalls = integer(p, "page.last.calls");
        lastPageRebuilds = integer(p, "page.last.rebuilds");
        lastDirectCalls = integer(p, "page.last.direct");
        compiled = integer(p, "page.compiled");
        expired = integer(p, "page.expired");
        evicted = integer(p, "page.evicted");
        flattened = bool(p, "page.flattened");
        ttl = integer(p, "page.ttl.frames");
        rebuildBudget = integer(p, "page.rebuild.budget");
        cacheMax = integer(p, "page.cache.max");
        renderCalls = number(p, "render.calls");
        renderNanos = number(p, "render.nanos");
        submittedMachines = number(p, "submitted.machines");
        prewarmEnabled = bool(p, "prewarm.enabled");
        prewarmPending = integer(p, "prewarm.pending");
        displayLive = integer(p, "display.live");
        displayPeak = integer(p, "display.peak");
        displayAllocated = integer(p, "display.allocated");
        displayDenied = integer(p, "display.denied");
        displayFailed = integer(p, "display.failed");
        displayMax = integer(p, "display.max");
        walls = rows[0];
        allocations = rows[1];
        hashes = new String[captures];
        for (int i = 0; i < captures; i++) hashes[i] = required(p, "checkpoint." + i + ".sha256");
    }

    static M788Artifact read(Path game, String expected) throws Exception {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) {
            p.load(reader);
        }
        List<String> lines = Files.readAllLines(game.resolve("frames.csv"));
        long[][] rows = new long[2][lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            String[] columns = lines.get(i).split(",", -1);
            SmokeSupport.require(columns.length == 2, "M788 frame row drift");
            rows[0][i] = Long.parseLong(columns[0]);
            rows[1][i] = Long.parseLong(columns[1]);
        }
        M788Artifact value = new M788Artifact(game, p, rows);
        SmokeSupport.require(value.arm.equals(expected) && value.frames == lines.size(),
            "M788 artifact identity drift: " + expected);
        return value;
    }

    void verify() throws Exception {
        boolean template = arm.equals("template");
        SmokeSupport.require(machines == 576 && frames >= 1200 && captures == 24
            && width > 0 && height > 0 && allocatedBytes > 0L && heapPeak > 0L
            && renderCalls == frames && renderNanos > 0L
            && submittedMachines == (long) frames * machines,
            "M788 incomplete artifact: " + summary());
        SmokeSupport.require(!flattened && ttl == 100000 && rebuildBudget == 8
            && cacheMax == -1 && !prewarmEnabled, "M788 activation drift: " + summary());
        SmokeSupport.require((template ? pageCalls > 0 && cachedMax > 0
                && pageRebuilds <= frames * 2 && directCalls <= frames
                && lastPageCalls == machines && lastPageRebuilds == 0 && lastDirectCalls == 0
                : pageCalls == 0 && cachedMax == 0 && directCalls == 0)
            && compiled == pageRebuilds
            && expired <= compiled && evicted == 0 && prewarmPending == 0
            && displayDenied == 0 && displayFailed == 0
            && (displayMax < 0 || displayPeak <= displayMax),
            "M788 cache/display-list guardrail drift: " + summary());
        for (int i = 0; i < captures; i++) {
            byte[] pixels = pixels(i);
            SmokeSupport.require(pixels.length == width * height * 4
                && M788Runtime.sha256Bytes(pixels).equals(hashes[i]),
                "M788 framebuffer artifact drift: " + i);
        }
    }

    byte[] pixels(int checkpoint) throws IOException {
        return Files.readAllBytes(game.resolve("visual-frames").resolve(arm)
            .resolve(String.format("checkpoint-%02d.rgba", checkpoint)));
    }

    double fps() { return walls.length * 1_000_000_000.0D / sum(walls); }
    long p50() { return percentile(0.50D); }
    long p95() { return percentile(0.95D); }
    long p99() { return percentile(0.99D); }
    private long percentile(double quantile) {
        long[] values = walls.clone();
        Arrays.sort(values);
        return values[Math.min(values.length - 1,
            Math.max(0, (int) Math.ceil(values.length * quantile) - 1))];
    }
    double allocationPerFrame() { return (double) allocatedBytes / frames; }
    double renderPerCall() { return (double) renderNanos / renderCalls; }
    String summary() {
        return arm + ":frames=" + frames + ",fps=" + fmt(fps()) + ",p50/p95/p99.ms="
            + fmt(p50() / 1_000_000.0D) + "/" + fmt(p95() / 1_000_000.0D) + "/"
            + fmt(p99() / 1_000_000.0D) + ",alloc/frame=" + fmt(allocationPerFrame())
            + ",heap.peak.mb=" + fmt(heapPeak / 1048576.0D) + ",render.us="
            + fmt(renderPerCall() / 1000.0D) + ",blank=" + blankCaptures + ",pages=" + pageCalls + "/"
            + pageRebuilds + "/" + directCalls + "/" + cachedMax + ",lists="
            + displayLive + "/" + displayPeak + "/" + displayAllocated;
    }

    private static long sum(long[] values) {
        long total = 0L;
        for (long value : values) total += value;
        return total;
    }
    private static String fmt(double v) { return String.format(Locale.ROOT, "%.2f", v); }
    private static boolean bool(Properties p, String k) { return Boolean.parseBoolean(required(p, k)); }
    private static int integer(Properties p, String k) { return Integer.parseInt(required(p, k)); }
    private static long number(Properties p, String k) { return Long.parseLong(required(p, k)); }
    private static String required(Properties p, String k) {
        String value = p.getProperty(k);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M788 " + k);
        return value.trim();
    }
}
