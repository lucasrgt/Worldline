import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Deterministic three-way merge driver for the sorted smoke qualification lock. */
public final class QualificationLockMerge {
    public static void main(String[] arguments) {
        try {
            if (arguments.length == 1 && arguments[0].equals("--self-test")) { selfTest(); return; }
            if (arguments.length != 3) throw new IllegalArgumentException(
                    "usage: QualificationLockMerge.java BASE CURRENT OTHER");
            Path base = Path.of(arguments[0]), current = Path.of(arguments[1]), other = Path.of(arguments[2]);
            Lock merged = merge(read(base), read(current), read(other));
            Files.writeString(current, merged.render(), StandardCharsets.UTF_8);
            System.out.println("qualification lock merge: " + merged.entries.size() + " pins");
        } catch (Exception error) {
            System.err.println("qualification lock merge failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static Lock merge(Lock base, Lock current, Lock other) {
        require(current.header.equals(other.header) && base.header.equals(current.header),
                "lock header/schema differs; migrate algorithms before merging");
        Set<String> ids = new TreeSet<>(); ids.addAll(base.entries.keySet());
        ids.addAll(current.entries.keySet()); ids.addAll(other.entries.keySet());
        Map<String, String> result = new TreeMap<>();
        for (String id : ids) {
            String ancestor = base.entries.get(id), left = current.entries.get(id), right = other.entries.get(id);
            String value;
            if (equal(left, right)) value = left;
            else if (equal(ancestor, left)) value = right;
            else if (equal(ancestor, right)) value = left;
            else throw new IllegalStateException("conflicting qualification pin: " + id);
            if (value != null) result.put(id, value);
        }
        return new Lock(current.header, result);
    }

    private static Lock read(Path path) throws Exception {
        return parse(Files.readString(path, StandardCharsets.UTF_8));
    }

    private static Lock parse(String text) {
        String normalized = text.replace("\r\n", "\n");
        String[] lines = normalized.split("\n");
        require(lines.length >= 3 && lines[0].startsWith("# Worldline smoke qualification lock v")
                && lines[1].matches("schema=[0-9]+")
                && lines[2].matches("algorithm=worldline-smoke-input-v[0-9]+(?:-[a-z]+)?"),
                "invalid lock header");
        String header = lines[0] + "\n" + lines[1] + "\n" + lines[2] + "\n";
        Map<String, ArrayList<String>> rows = new TreeMap<>();
        for (int index = 3; index < lines.length; index++) {
            if (lines[index].isBlank()) continue;
            String line = lines[index]; require(line.startsWith("smoke."), "invalid lock row: " + line);
            int field = line.indexOf('.', 6); require(field > 6, "invalid lock key: " + line);
            String id = line.substring(6, field);
            require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid smoke id: " + id);
            rows.computeIfAbsent(id, ignored -> new ArrayList<>()).add(line);
        }
        Map<String, String> entries = new TreeMap<>();
        for (Map.Entry<String, ArrayList<String>> row : rows.entrySet()) {
            ArrayList<String> values = row.getValue();
            require(values.size() == 4 || values.size() == 5, "incomplete pin: " + row.getKey());
            boolean sealed = values.size() == 5;
            require(values.get(0).contains(".fingerprint=")
                    && (!sealed || values.get(1).contains(".observation_sha256="))
                    && values.get(sealed ? 2 : 1).contains(".evidence_sha256=")
                    && values.get(sealed ? 3 : 2).contains(".source=")
                    && values.get(sealed ? 4 : 3).endsWith(".status=passed"),
                    "pin field order drift: " + row.getKey());
            entries.put(row.getKey(), String.join("\n", values) + "\n");
        }
        return new Lock(header, entries);
    }

    private static String value(String line) { return line.substring(line.indexOf('=') + 1); }
    private static boolean equal(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private static void selfTest() {
        String header = "# Worldline smoke qualification lock v4\nschema=4\n"
                + "algorithm=worldline-smoke-input-v4\n";
        Lock base = parse(header + pin("m1-one", "a") + pin("m2-two", "b"));
        Lock left = parse(header + pin("m1-one", "c") + pin("m2-two", "b"));
        Lock right = parse(header + pin("m1-one", "a") + pin("m2-two", "d") + pin("m3-three", "e"));
        Lock merged = merge(base, left, right);
        require(merged.render().equals(header + pin("m1-one", "c") + pin("m2-two", "d")
                + pin("m3-three", "e")), "ordered union drifted");
        boolean rejected = false;
        try { merge(base, left, parse(header + pin("m1-one", "f") + pin("m2-two", "b"))); }
        catch (IllegalStateException expected) { rejected = true; }
        require(rejected, "same-pin conflict was accepted");
        String sealedHeader = "# Worldline smoke qualification lock v5\nschema=5\n"
                + "algorithm=worldline-smoke-input-v6-tokens\n";
        Lock sealedBase = parse(sealedHeader + sealedPin("m1-one", "a") + sealedPin("m2-two", "b"));
        Lock sealedLeft = parse(sealedHeader + sealedPin("m1-one", "c") + sealedPin("m2-two", "b"));
        Lock sealedRight = parse(sealedHeader + sealedPin("m1-one", "a") + sealedPin("m2-two", "b")
                + sealedPin("m3-three", "e"));
        require(merge(sealedBase, sealedLeft, sealedRight).render().equals(sealedHeader
                + sealedPin("m1-one", "c") + sealedPin("m2-two", "b") + sealedPin("m3-three", "e")),
                "sealed ordered union drifted");
        System.out.println("qualification lock merge self-test passed");
    }

    private static String pin(String id, String seed) {
        String hash = seed.repeat(64);
        return "smoke." + id + ".fingerprint=" + hash + "\nsmoke." + id
                + ".evidence_sha256=" + hash + "\nsmoke." + id
                + ".source=executed\nsmoke." + id + ".status=passed\n";
    }

    private static String sealedPin(String id, String seed) {
        String hash = seed.repeat(64);
        return "smoke." + id + ".fingerprint=" + hash + "\nsmoke." + id
                + ".observation_sha256=" + hash + "\nsmoke." + id
                + ".evidence_sha256=" + hash + "\nsmoke." + id
                + ".source=executed\nsmoke." + id + ".status=passed\n";
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Lock(String header, Map<String, String> entries) {
        String render() { return header + String.join("", entries.values()); }
    }
}
