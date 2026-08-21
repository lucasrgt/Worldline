package worldline.smoke.m19;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import worldline.aero.AeroSaveProbe;
import worldline.aero.AeroSaveProbe.Sample;

/** Qualifies the opt-in one-chunk save budget against the forced hitch. */
public final class ForcedAutosaveSmoke {
    private ForcedAutosaveSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 4, "expected live, budgeted, skipped logs, and frame limit");
        int limit = Integer.parseInt(arguments[3]);
        Window live = Window.load(Paths.get(arguments[0]), limit);
        Window budgeted = Window.load(Paths.get(arguments[1]), limit);
        Window skipped = Window.load(Paths.get(arguments[2]), limit);
        require(live.saves >= 2 && live.saveMaxUs >= 20_000L,
                "live run did not reproduce the cadenced batch hitch");
        require(skipped.saves == 0 && skipped.saveMaxUs == 0L,
                "skipped twin still recorded worldSaveMs");
        require(budgeted.saves >= 1, "budgeted run cancelled saves instead of spreading them");
        require(budgeted.saveMaxUs < live.saveMaxUs,
                "one-chunk budget did not reduce the worst save");
        live.print("live");
        budgeted.print("budgeted");
        System.out.println("skipped.saveFrames=" + skipped.saves);
        String report = "scene=LOOK_JUMP_SPIN_TOWER\n"
                + "budget=ONE_CHUNK_NON_FORCED_OPT_IN\n"
                + "live=CADENCED_BATCH\n"
                + "budgeted=SMALLER_MAX_SAVE\n"
                + "skipped=SAVE_CANCELLED\n";
        System.out.println("WORLDLINE_M19_FORCED_AUTOSAVE=PASS");
        System.out.print(report);
        System.out.println("evidence.sha256=" + sha256(report));
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

    private static final class Window {
        final int saves;
        final long saveMaxUs;

        private Window(int saves, long saveMaxUs) {
            this.saves = saves; this.saveMaxUs = saveMaxUs;
        }

        static Window load(Path path, int limit) throws Exception {
            int saves = 0; long max = 0; int frames = 0;
            for (String row : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!row.startsWith("[Aero_")) continue;
                Sample sample = AeroSaveProbe.parse(row);
                frames++;
                if (sample.saveUs >= 1000L) { saves++; max = Math.max(max, sample.saveUs); }
            }
            require(frames >= limit, "too few measured frames in " + path);
            return new Window(saves, max);
        }

        void print(String name) {
            System.out.println(name + ".saveFrames=" + saves + " saveMaxUs=" + saveMaxUs);
        }
    }
}
