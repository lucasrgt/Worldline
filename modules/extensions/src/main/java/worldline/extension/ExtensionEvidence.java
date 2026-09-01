package worldline.extension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Equatable evidence emitted in the same deterministic shape for every extension mode. */
public final class ExtensionEvidence {
    private final String extensionId, extensionVersion, contractId, subjectId;
    private final ExtensionMode mode;
    private final Map<String, String> observations;
    private final String signal, signature, canonical;

    private ExtensionEvidence(ExtensionManifest manifest, ExtensionContract contract,
            ExtensionMode mode, Map<String, String> observations) {
        if (manifest == null || contract == null || mode == null) throw new NullPointerException();
        if (!contract.modes().contains(mode)) throw new IllegalArgumentException("unsupported mode");
        extensionId = manifest.id(); extensionVersion = manifest.version();
        contractId = contract.id(); subjectId = contract.subjectId(); this.mode = mode;
        TreeMap<String, String> copy = checked(observations);
        this.observations = Collections.unmodifiableMap(copy);
        signal = signal(copy); signature = signature(copy);
        canonical = "WORLDLINE-EXTENSION-EVIDENCE/1\n" + "extension=" + extensionId + "\n"
                + "version=" + extensionVersion + "\ncontract=" + contractId + "\nsubject="
                + subjectId + "\nmode=" + mode.token() + "\nsignal=" + signal
                + "\nsignature=" + signature + "\n";
    }

    public static ExtensionEvidence capture(ExtensionManifest manifest, ExtensionContract contract,
            ExtensionMode mode, Map<String, String> observations) {
        return new ExtensionEvidence(manifest, contract, mode, observations);
    }

    public static String signature(Map<String, String> observations) {
        return sha256(observationDocument(checked(observations)));
    }

    public String extensionId() { return extensionId; }
    public String extensionVersion() { return extensionVersion; }
    public String contractId() { return contractId; }
    public String subjectId() { return subjectId; }
    public ExtensionMode mode() { return mode; }
    public Map<String, String> observations() { return observations; }
    public String signal() { return signal; }
    public String signature() { return signature; }
    public String canonical() { return canonical; }

    private static TreeMap<String, String> checked(Map<String, String> source) {
        if (source == null || source.isEmpty()) throw new IllegalArgumentException("observations");
        TreeMap<String, String> copy = new TreeMap<String, String>();
        for (Map.Entry<String, String> row : source.entrySet()) {
            String key = row.getKey(), value = row.getValue();
            if (key == null || !key.matches("[a-z][a-z0-9.-]{0,62}"))
                throw new IllegalArgumentException("observation key");
            if (value == null || value.length() > 1024 || value.indexOf('\n') >= 0
                    || value.indexOf('\r') >= 0 || value.indexOf('=') >= 0)
                throw new IllegalArgumentException("observation value");
            if (copy.put(key, value) != null) throw new IllegalArgumentException("duplicate observation");
        }
        return copy;
    }

    private static String observationDocument(TreeMap<String, String> values) {
        StringBuilder text = new StringBuilder("WORLDLINE-EXTENSION-OBSERVATIONS/1\n");
        for (Map.Entry<String, String> row : values.entrySet())
            text.append(row.getKey()).append('=').append(row.getValue()).append('\n');
        return text.toString();
    }

    private static String signal(TreeMap<String, String> values) {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, String> row : values.entrySet()) {
            if (text.length() > 0) text.append(',');
            text.append(row.getKey()).append(':').append(row.getValue());
        }
        return text.toString();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    value.getBytes(StandardCharsets.UTF_8));
            StringBuilder text = new StringBuilder();
            for (byte item : digest) text.append(String.format("%02x", item & 255));
            return text.toString();
        } catch (Exception error) { throw new IllegalStateException(error); }
    }
}
