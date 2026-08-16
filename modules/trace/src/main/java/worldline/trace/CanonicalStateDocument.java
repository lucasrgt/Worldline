package worldline.trace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Immutable parsed representation of the canonical schema-bearing v2 trace. */
public final class CanonicalStateDocument {
    public static final int MAX_CHARACTERS = 4_194_304;
    private final long seed;
    private final List<String> fields;
    private final List<Record> records;
    private final String canonical;

    private CanonicalStateDocument(long seed, List<String> fields, List<Record> records,
            String canonical) {
        this.seed = seed; this.fields = Collections.unmodifiableList(fields);
        this.records = Collections.unmodifiableList(records); this.canonical = canonical;
    }

    public static CanonicalStateDocument parse(String input) {
        if (input == null) throw new NullPointerException("input");
        require(!input.isEmpty() && input.length() <= MAX_CHARACTERS, "invalid trace size");
        String[] segments = input.split("\\|", -1);
        require(segments.length >= 3 && segments[0].equals("v2"), "unsupported trace format");
        String seedValue = value(segments[1], "seed");
        long seed = number(seedValue, "seed");
        require(Long.toString(seed).equals(seedValue), "trace seed is not canonical");
        String schema = value(segments[2], "schema");
        String[] names = schema.split(",", -1);
        require(names.length > 0, "trace schema is empty");
        List<String> fields = new ArrayList<>(); Set<String> unique = new HashSet<>();
        for (String name : names) {
            name(name, "field"); require(unique.add(name), "duplicate trace field: " + name);
            fields.add(name);
        }
        List<Record> records = new ArrayList<>();
        for (int index = 3; index < segments.length; index++) {
            String segment = segments[index]; int separator = segment.indexOf('=');
            require(separator > 0 && separator == segment.lastIndexOf('='), "invalid trace record");
            String label = segment.substring(0, separator); name(label, "label");
            String[] raw = segment.substring(separator + 1).split(",", -1);
            require(raw.length == fields.size(), "trace record width mismatch at " + label);
            List<Long> values = new ArrayList<>();
            for (String item : raw) {
                long parsed = number(item, "record value");
                require(Long.toString(parsed).equals(item), "trace value is not canonical");
                values.add(parsed);
            }
            records.add(new Record(label, values));
        }
        return new CanonicalStateDocument(seed, fields, records, input);
    }

    public long seed() { return seed; }
    public List<String> fields() { return fields; }
    public List<Record> records() { return records; }
    public String canonical() { return canonical; }
    public String signature() { return sha256(canonical.getBytes(StandardCharsets.UTF_8)); }

    public static final class Record {
        private final String label;
        private final List<Long> values;
        private Record(String label, List<Long> values) {
            this.label = label; this.values = Collections.unmodifiableList(values);
        }
        public String label() { return label; }
        public List<Long> values() { return values; }
        public long value(int index) { return values.get(index); }
    }

    private static String value(String segment, String key) {
        String prefix = key + "="; require(segment.startsWith(prefix), "missing trace " + key);
        return segment.substring(prefix.length());
    }
    private static long number(String value, String role) {
        try { return Long.parseLong(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("invalid trace " + role, error); }
    }
    private static void name(String value, String role) {
        require(!value.isEmpty(), "trace " + role + " is empty");
        for (int index = 0; index < value.length(); index++) { char item = value.charAt(index);
            require(Character.isLetterOrDigit(item) || item == '_',
                    "trace " + role + " contains a delimiter: " + value); }
    }
    private static String sha256(byte[] value) {
        try { byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) hex.append(String.format("%02x", item & 0xff)); return hex.toString(); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
