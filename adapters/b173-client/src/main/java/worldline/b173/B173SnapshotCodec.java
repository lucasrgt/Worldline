package worldline.b173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import worldline.api.RuntimeSnapshot;

/** Canonical replay-backed b1.7.3 snapshot codec. */
final class B173SnapshotCodec {
    private static final String HEADER = "WORLDLINE-SNAPSHOT/1";
    private static final String RUNTIME = "minecraft-b1.7.3-client";

    private B173SnapshotCodec() {}

    static RuntimeSnapshot encode(B173Checkpoint checkpoint) {
        if (checkpoint == null) throw new NullPointerException("checkpoint");
        String world = portable(checkpoint.worldPath());
        StringBuilder body = new StringBuilder();
        line(body, HEADER); line(body, "runtime=" + RUNTIME);
        line(body, "seed=" + checkpoint.seed());
        line(body, "initialMillis=" + checkpoint.initialClockMillis());
        line(body, "world=" + encoded(world)); line(body, "tick=" + checkpoint.tick());
        line(body, "events=" + checkpoint.eventCount());
        for (B173Action action : checkpoint.actions()) {
            line(body, "event=" + action.tick + "," + action.kind() + "," + action.a() + ","
                    + action.b() + "," + action.c() + "," + action.d() + "," + action.value());
        }
        line(body, "state=" + encoded(checkpoint.stateFingerprint()));
        String document = body + "sha256=" + sha256(bytes(body.toString())) + "\n";
        return RuntimeSnapshot.of(bytes(document));
    }

    static B173Checkpoint decode(RuntimeSnapshot snapshot) {
        if (snapshot == null) throw new NullPointerException("snapshot");
        byte[] input = snapshot.bytes();
        String document = new String(input, StandardCharsets.UTF_8);
        require(Arrays.equals(input, bytes(document)), "snapshot is not strict UTF-8");
        String[] lines = document.split("\n", -1);
        require(lines.length >= 10 && lines[lines.length - 1].isEmpty(), "invalid snapshot framing");
        require(lines[0].equals(HEADER), "unsupported snapshot version");
        require(value(lines[1], "runtime").equals(RUNTIME), "snapshot runtime mismatch");
        long seed = number(value(lines[2], "seed"), "seed");
        long millis = number(value(lines[3], "initialMillis"), "initialMillis");
        Path world = Paths.get(decoded(value(lines[4], "world")));
        int tick = integer(value(lines[5], "tick"), "tick");
        int count = integer(value(lines[6], "events"), "events");
        require(count >= 0 && lines.length == count + 10, "snapshot event count mismatch");
        List<B173Action> actions = new ArrayList<>();
        for (int index = 0; index < count; index++) actions.add(action(lines[index + 7]));
        String state = decoded(value(lines[count + 7], "state"));
        require(!state.isEmpty() && state.length() <= 4096, "invalid snapshot state fingerprint");
        int checksum = count + 8;
        StringBuilder body = new StringBuilder();
        for (int index = 0; index < checksum; index++) line(body, lines[index]);
        require(value(lines[checksum], "sha256").equals(sha256(bytes(body.toString()))),
                "snapshot checksum mismatch");
        B173Checkpoint result = new B173Checkpoint(seed, millis, world, tick, actions, state);
        require(Arrays.equals(input, encode(result).bytes()), "snapshot is not canonical");
        return result;
    }

    private static B173Action action(String line) {
        String[] values = value(line, "event").split(",", -1);
        require(values.length == 7, "invalid snapshot event");
        return B173Action.decoded(integer(values[0], "event tick"), integer(values[1], "event kind"),
                integer(values[2], "event a"), integer(values[3], "event b"),
                integer(values[4], "event c"), integer(values[5], "event d"),
                number(values[6], "event value"));
    }

    private static String portable(Path path) {
        String value = path.toString().replace('\\', '/');
        Path normalized = Paths.get(value).normalize();
        require(!value.isEmpty() && !normalized.isAbsolute()
                && normalized.toString().replace('\\', '/').equals(value),
                "snapshot world must be a normalized relative path");
        return value;
    }

    private static String encoded(String value) { return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(bytes(value)); }
    private static String decoded(String value) {
        try { return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8); }
        catch (IllegalArgumentException error) { throw new IllegalArgumentException("invalid snapshot base64", error); }
    }
    private static String value(String line, String key) {
        String prefix = key + "="; require(line.startsWith(prefix), "missing snapshot field " + key);
        return line.substring(prefix.length());
    }
    private static int integer(String value, String label) {
        long result = number(value, label); require(result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE,
                "snapshot " + label + " is outside integer range"); return (int) result;
    }
    private static long number(String value, String label) {
        try { return Long.parseLong(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("invalid snapshot " + label, error); }
    }
    private static String sha256(byte[] value) {
        try { byte[] digest = MessageDigest.getInstance("SHA-256").digest(value); StringBuilder hex = new StringBuilder();
            for (byte item : digest) hex.append(String.format("%02x", item & 0xff)); return hex.toString(); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    private static byte[] bytes(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static void line(StringBuilder target, String value) { target.append(value).append('\n'); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
