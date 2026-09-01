package worldline.extension;

import java.util.regex.Pattern;

/** Declarative bridge from one legacy loader to a public TestKit provider. */
public final class ExtensionRuntimeAdapter {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9_.-]{0,62}");
    private static final Pattern TYPE = Pattern.compile(
            "[A-Za-z_$][A-Za-z0-9_$]*(?:[.][A-Za-z_$][A-Za-z0-9_$]*)*");
    private final String loaderId, runtimeId, providerClass;

    private ExtensionRuntimeAdapter(String loaderId, String runtimeId, String providerClass) {
        if (loaderId == null || !TOKEN.matcher(loaderId).matches())
            throw new IllegalArgumentException("loader id");
        if (runtimeId == null || !TOKEN.matcher(runtimeId).matches())
            throw new IllegalArgumentException("runtime id");
        if (providerClass == null || !TYPE.matcher(providerClass).matches())
            throw new IllegalArgumentException("provider class");
        this.loaderId = loaderId; this.runtimeId = runtimeId; this.providerClass = providerClass;
    }

    public static ExtensionRuntimeAdapter of(String loaderId, String runtimeId,
            String providerClass) {
        return new ExtensionRuntimeAdapter(loaderId, runtimeId, providerClass);
    }

    public String loaderId() { return loaderId; }
    public String runtimeId() { return runtimeId; }
    public String providerClass() { return providerClass; }
}
