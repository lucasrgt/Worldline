package worldline.atlas;

import java.util.regex.Pattern;

/** Stable identifiers and artifact scope for WORLDLINE-ATLAS/1 records. */
public final class AtlasSchema {
    public static final String HEADER = "WORLDLINE-ATLAS/1";
    public static final String STORE = "WORLDLINE-ATLAS-STORE/1";
    public static final String SCOPE = "b1.7.3";
    public static final String CLIENT = "minecraft-b1.7.3-client";
    public static final String SERVER = "minecraft-b1.7.3-server";
    public static final String WORLDLINE = "worldline";
    static final Pattern ID = Pattern.compile(
            "atlas\\.(role|boundary|invariant|experiment|scenario|claim|subsystem|coverage-unit|"
            + "hypothesis|field|loader|api|mapping-set|namespace|ecosystem-claim)\\."
            + "[A-Za-z0-9][A-Za-z0-9_.-]*");
    static final Pattern HEX = Pattern.compile("[a-f0-9]{64}");
    static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]*");

    private AtlasSchema() {}

    public static String requireId(String id) {
        if (id == null || !ID.matcher(id).matches()) throw new IllegalArgumentException("id");
        AtlasKind.ofId(id);
        return id;
    }

    public static boolean shaToken(String value) {
        return value != null && HEX.matcher(value).matches();
    }
}
