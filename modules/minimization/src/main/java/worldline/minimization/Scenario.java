package worldline.minimization;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/** Canonical ordered scenario whose opaque steps can be replayed by an adapter. */
public final class Scenario {
    public static final int MAX_BYTES = 1_048_576, MAX_STEPS = 4096, MAX_STEP_BYTES = 1024;
    private static final String HEADER = "WORLDLINE-SCENARIO/1";
    private final List<String> steps;
    private final byte[] bytes;

    private Scenario(List<String> steps, byte[] bytes) {
        this.steps = Collections.unmodifiableList(steps); this.bytes = bytes;
    }

    public static Scenario of(List<String> input) {
        if (input == null) throw new NullPointerException("steps");
        require(input.size() <= MAX_STEPS, "scenario has too many steps");
        List<String> steps = new ArrayList<>(); StringBuilder body = new StringBuilder();
        line(body, HEADER);
        for (String step : input) {
            if (step == null) throw new NullPointerException("step");
            byte[] encoded = utf8(step);
            require(!step.isEmpty() && encoded.length <= MAX_STEP_BYTES
                    && step.equals(new String(encoded, StandardCharsets.UTF_8)), "invalid scenario step");
            for (int index = 0; index < step.length(); index++) require(step.charAt(index) >= 0x20
                    && step.charAt(index) <= 0x7e, "scenario step must be visible ASCII");
            steps.add(step); line(body, "step=" + Base64.getUrlEncoder().withoutPadding().encodeToString(encoded));
        }
        byte[] bytes = utf8(body + "sha256=" + sha256(utf8(body.toString())) + "\n");
        require(bytes.length <= MAX_BYTES, "scenario exceeds maximum size");
        return new Scenario(steps, bytes);
    }

    public static Scenario parse(byte[] input) {
        if (input == null) throw new NullPointerException("input");
        require(input.length > 0 && input.length <= MAX_BYTES, "invalid scenario size");
        String document = new String(input, StandardCharsets.UTF_8);
        require(Arrays.equals(input, utf8(document)), "scenario is not strict UTF-8");
        String[] lines = document.split("\n", -1);
        require(lines.length >= 3 && lines[lines.length - 1].isEmpty(), "invalid scenario framing");
        require(HEADER.equals(lines[0]), "unsupported scenario version");
        require(lines.length - 3 <= MAX_STEPS, "scenario has too many steps");
        List<String> steps = new ArrayList<>();
        for (int index = 1; index < lines.length - 2; index++) {
            byte[] decoded;
            try { decoded = Base64.getUrlDecoder().decode(value(lines[index], "step")); }
            catch (IllegalArgumentException error) { throw new IllegalArgumentException("invalid scenario step", error); }
            require(decoded.length > 0 && decoded.length <= MAX_STEP_BYTES, "invalid scenario step size");
            String step = new String(decoded, StandardCharsets.UTF_8);
            require(Arrays.equals(decoded, utf8(step)), "scenario step is not strict UTF-8");
            steps.add(step);
        }
        StringBuilder body = new StringBuilder();
        for (int index = 0; index < lines.length - 2; index++) line(body, lines[index]);
        require(value(lines[lines.length - 2], "sha256").equals(sha256(utf8(body.toString()))),
                "scenario checksum mismatch");
        Scenario result = of(steps); require(Arrays.equals(input, result.bytes), "scenario is not canonical");
        return result;
    }

    public List<String> steps() { return steps; }
    public int size() { return steps.size(); }
    public String step(int index) { return steps.get(index); }
    public byte[] bytes() { return bytes.clone(); }
    public String sha256() { return sha256(bytes); }

    @Override public boolean equals(Object other) {
        return other instanceof Scenario && Arrays.equals(bytes, ((Scenario) other).bytes);
    }
    @Override public int hashCode() { return Arrays.hashCode(bytes); }

    private static String value(String line, String key) {
        String prefix = key + "="; require(line.startsWith(prefix), "missing scenario field " + key);
        return line.substring(prefix.length());
    }
    private static byte[] utf8(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static void line(StringBuilder target, String value) { target.append(value).append('\n'); }
    private static String sha256(byte[] value) { try {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value); StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255)); return result.toString();
    } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
