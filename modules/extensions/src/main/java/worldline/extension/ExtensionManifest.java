package worldline.extension;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/** Strict {@code worldline.extension.v1} discovery manifest. */
public final class ExtensionManifest {
    public static final String SCHEMA = "worldline.extension.v1";
    private static final Set<String> KEYS = new HashSet<String>(Arrays.asList(
            "schema", "id", "version", "entrypoint", "worldline.api", "requires", "provides"));
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]{0,62}");
    private static final Pattern VERSION = Pattern.compile("[0-9]+[.][0-9]+[.][0-9]+(?:[-+][A-Za-z0-9.-]+)?");
    private static final Pattern TYPE = Pattern.compile(
            "[A-Za-z_$][A-Za-z0-9_$]*(?:[.][A-Za-z_$][A-Za-z0-9_$]*)*");
    private final String id, version, entrypoint;
    private final ExtensionCapabilities requires, provides;

    private ExtensionManifest(Properties values) {
        for (String key : values.stringPropertyNames()) if (!KEYS.contains(key))
            throw new IllegalArgumentException("unknown extension manifest key " + key);
        require(SCHEMA.equals(required(values, "schema")), "extension manifest schema");
        id = required(values, "id"); version = required(values, "version");
        entrypoint = required(values, "entrypoint");
        require(ID.matcher(id).matches(), "extension id");
        require(VERSION.matcher(version).matches(), "extension version");
        require(TYPE.matcher(entrypoint).matches(), "extension entrypoint");
        require("1".equals(required(values, "worldline.api")), "extension api");
        requires = ExtensionCapabilities.parse(values.getProperty("requires"));
        provides = ExtensionCapabilities.parse(values.getProperty("provides"));
    }

    public static ExtensionManifest load(Path path) throws IOException {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return new ExtensionManifest(values);
    }

    public String id() { return id; }
    public String version() { return version; }
    public String entrypoint() { return entrypoint; }
    public ExtensionCapabilities requires() { return requires; }
    public ExtensionCapabilities provides() { return provides; }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key);
        return value.trim();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
