package worldline.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Stable capability tokens used for fail-closed SDK negotiation. */
public final class ExtensionCapabilities {
    public static final String TESTKIT_V1 = "testkit.v1";
    public static final String ATLAS_V1 = "atlas.v1";
    public static final String CUSTOM_CONTRACT_V1 = "custom-contract.v1";
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9.-]{0,62}");
    private final Set<String> values;

    private ExtensionCapabilities(Set<String> values) {
        this.values = Collections.unmodifiableSet(values);
    }

    public static ExtensionCapabilities of(String... tokens) {
        Set<String> values = new LinkedHashSet<String>();
        if (tokens != null) for (String token : tokens) add(values, token);
        return new ExtensionCapabilities(values);
    }

    static ExtensionCapabilities parse(String csv) {
        if (csv == null || csv.trim().isEmpty()) return of();
        Set<String> values = new LinkedHashSet<String>();
        for (String item : csv.split(",", -1)) add(values, item.trim());
        return new ExtensionCapabilities(values);
    }

    public boolean contains(String capability) { return values.contains(capability); }
    public boolean containsAll(ExtensionCapabilities required) {
        return values.containsAll(required.values);
    }
    public List<String> values() { return Collections.unmodifiableList(
            new ArrayList<String>(values)); }
    public String csv() { return join(values); }

    private static void add(Set<String> values, String token) {
        if (token == null || !TOKEN.matcher(token).matches())
            throw new IllegalArgumentException("capability token");
        if (!values.add(token)) throw new IllegalArgumentException("duplicate capability " + token);
    }

    private static String join(Set<String> values) {
        StringBuilder text = new StringBuilder();
        for (String value : values) { if (text.length() > 0) text.append(','); text.append(value); }
        return text.toString();
    }
}
