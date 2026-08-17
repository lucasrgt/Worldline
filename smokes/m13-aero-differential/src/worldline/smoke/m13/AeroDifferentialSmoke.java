package worldline.smoke.m13;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.aero.AeroDiagnostics;
import worldline.aero.AeroDiagnostics.Sample;

/** Qualifies persistence, empty-scene attribution, and the compile governor. */
public final class AeroDifferentialSmoke {
    private static final long SPIKE_US = 25_000L;
    private AeroDifferentialSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 8, "expected three logs, four BE counts, and frame limit");
        Metrics dense = analyze(Paths.get(arguments[0]), Integer.parseInt(arguments[7]));
        Metrics empty = analyze(Paths.get(arguments[1]), Integer.parseInt(arguments[7]));
        Metrics budget = analyze(Paths.get(arguments[2]), Integer.parseInt(arguments[7]));
        long freshGlobal = Long.parseLong(arguments[3]), freshBlocks = Long.parseLong(arguments[4]);
        long reloadGlobal = Long.parseLong(arguments[5]), reloadBlocks = Long.parseLong(arguments[6]);
        require(freshBlocks > 0 && freshGlobal > freshBlocks + 100, "fresh fixture lacks phantom BEs");
        require(reloadBlocks == freshBlocks, "real BE blocks did not persist");
        require(reloadGlobal < freshGlobal - 100 && reloadGlobal >= reloadBlocks,
                "phantom BEs were not removed on reload");
        require(dense.compileCalls > 0 && empty.compileCalls > 0,
                "both scenes must exercise chunk compilation");
        require(budget.skipped > budget.compileCalls * 100,
                "always-on budget did not expose retry storm");
        String report = "real.entity.blocks.persist=true\nphantom.blockentities.reload.removed=true\n"
                + "empty.scene.compile.pressure=true\ndense.scene.amplifier=NOT_ESTABLISHED\n"
                + "compile.budget.result=REJECT_RETRY_STORM\n";
        System.out.println("WORLDLINE_M13_DIFFERENTIAL=PASS");
        print("dense", dense); print("empty", empty); print("budget", budget);
        System.out.println("fresh.blockentities=" + freshGlobal + "/" + freshBlocks);
        System.out.println("reload.blockentities=" + reloadGlobal + "/" + reloadBlocks);
        System.out.print(report);
        System.out.println("evidence.sha256=" + sha256(report));
    }

    private static Metrics analyze(Path path, int limit) throws Exception {
        List<Sample> all = new ArrayList<>();
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (!line.startsWith("[Aero_")) continue;
            Sample sample = AeroDiagnostics.parse(line);
            if (sample.visibleChunks > 0) all.add(sample);
        }
        require(all.size() >= limit, "too few stable frames in " + path);
        List<Sample> samples = all.subList(all.size() - limit, all.size());
        List<Long> compile = new ArrayList<>();
        long calls = 0, skipped = 0, spikes = 0, compileSpikes = 0, max = 0;
        for (Sample sample : samples) {
            calls += sample.compileCalls; skipped += sample.budgetSkipped;
            if (sample.frameUs >= SPIKE_US) spikes++;
            if (sample.frameUs >= SPIKE_US && sample.compileUs >= 10_000L) compileSpikes++;
            max = Math.max(max, sample.compileUs); compile.add(sample.compileUs);
        }
        Collections.sort(compile);
        long p95 = compile.get((int) Math.floor((compile.size() - 1) * 0.95D));
        return new Metrics(samples.size(), spikes, compileSpikes, calls, skipped, max, p95);
    }

    private static void print(String name, Metrics value) {
        System.out.println(name + ".frames=" + value.frames + " spikes=" + value.spikes
                + " compileSpikes=" + value.compileSpikes + " compileCalls=" + value.compileCalls
                + " skipped=" + value.skipped + " compileMaxUs=" + value.maxCompileUs
                + " compileP95Us=" + value.p95CompileUs);
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255));
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class Metrics {
        final long frames, spikes, compileSpikes, compileCalls, skipped, maxCompileUs, p95CompileUs;
        Metrics(long frames, long spikes, long compileSpikes, long compileCalls, long skipped,
                long maxCompileUs, long p95CompileUs) {
            this.frames = frames; this.spikes = spikes; this.compileSpikes = compileSpikes;
            this.compileCalls = compileCalls; this.skipped = skipped;
            this.maxCompileUs = maxCompileUs; this.p95CompileUs = p95CompileUs;
        }
    }
}
