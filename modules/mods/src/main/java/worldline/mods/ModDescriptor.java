package worldline.mods;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Immutable, strictly parsed metadata embedded in a Worldline mod JAR. */
public final class ModDescriptor {
    public static final String ENTRY = "META-INF/worldline-mod.properties";
    public static final int MAX_BYTES = 4096;
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9.-]{0,63}");
    private static final Pattern VERSION = Pattern.compile(
            "[0-9]+\\.[0-9]+\\.[0-9]+(?:-[a-z0-9][a-z0-9.-]*)?");
    private static final Pattern TYPE = Pattern.compile(
            "[A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)+");
    private static final Pattern TOKEN = Pattern.compile("[a-z0-9][a-z0-9._-]{0,63}");

    private final String id;
    private final String version;
    private final String entrypoint;
    private final String worldlineApi;
    private final String runtime;
    private final List<ModDependency> requires;

    private ModDescriptor(String id, String version, String entrypoint,
            String worldlineApi, String runtime, List<ModDependency> requires) {
        this.id = id;
        this.version = version;
        this.entrypoint = entrypoint;
        this.worldlineApi = worldlineApi;
        this.runtime = runtime;
        this.requires = Collections.unmodifiableList(new ArrayList<>(requires));
    }

    public static ModDescriptor read(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry descriptor = null;
            int matches = 0;
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry candidate = entries.nextElement();
                if (ENTRY.equals(candidate.getName())) { descriptor = candidate; matches++; }
            }
            require(matches == 1 && descriptor != null && !descriptor.isDirectory(),
                    "mod JAR must contain exactly one " + ENTRY);
            require(descriptor.getSize() >= 0 && descriptor.getSize() <= MAX_BYTES,
                    "mod descriptor is too large");
            try (InputStream input = jar.getInputStream(descriptor)) {
                return parse(readBounded(input));
            }
        }
    }

    static ModDescriptor parse(byte[] bytes) {
        require(bytes.length > 0 && bytes.length <= MAX_BYTES, "invalid mod descriptor size");
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException error) {
            throw new IllegalArgumentException("mod descriptor is not strict UTF-8", error);
        }
        require(text.endsWith("\n") && text.indexOf('\r') < 0,
                "mod descriptor must use canonical LF lines");
        String[] rows = text.split("\n", -1);
        String format = value(rows[0], "format", null);
        if ("1".equals(format)) {
            require(rows.length == 7 && rows[6].isEmpty(), "mod descriptor must contain six fields");
            return new ModDescriptor(checkedId(value(rows[1], "id", null)),
                    checkedVersion(value(rows[2], "version", null)),
                    checkedEntrypoint(value(rows[3], "entrypoint", null)),
                    checkedApi(value(rows[4], "worldline.api", null)),
                    checkedRuntime(value(rows[5], "runtime", null)),
                    Collections.<ModDependency>emptyList());
        }
        require("2".equals(format) && rows.length == 8 && rows[7].isEmpty(),
                "unsupported mod descriptor format");
        return new ModDescriptor(checkedId(value(rows[1], "id", null)),
                checkedVersion(value(rows[2], "version", null)),
                checkedEntrypoint(value(rows[3], "entrypoint", null)),
                checkedApi(value(rows[4], "worldline.api", null)),
                checkedRuntime(value(rows[5], "runtime", null)),
                dependencies(optional(rows[6], "requires")));
    }

    /** Reads one optional field whose value may be empty. */
    private static String optional(String row, String key) {
        String prefix = key + "=";
        require(row.startsWith(prefix), "expected mod descriptor field " + key);
        return row.substring(prefix.length());
    }

    private static String checkedId(String id) {
        require(ID.matcher(id).matches(), "invalid mod id"); return id;
    }

    private static String checkedVersion(String version) {
        require(VERSION.matcher(version).matches(), "invalid mod version"); return version;
    }

    private static String checkedEntrypoint(String entrypoint) {
        require(TYPE.matcher(entrypoint).matches(), "invalid mod entrypoint"); return entrypoint;
    }

    private static String checkedApi(String api) {
        require(api.matches("[1-9][0-9]*"), "invalid Worldline API version"); return api;
    }

    private static String checkedRuntime(String runtime) {
        require(TOKEN.matcher(runtime).matches(), "invalid mod runtime"); return runtime;
    }

    private static List<ModDependency> dependencies(String field) {
        List<ModDependency> parsed = new ArrayList<>();
        if (field.isEmpty()) return parsed;
        for (String token : field.split(",", -1)) {
            ModDependency dependency = ModDependency.parse(token);
            require(parsed.indexOf(dependency) < 0, "duplicate mod dependency");
            parsed.add(dependency);
        }
        return parsed;
    }

    public String id() { return id; }
    public String version() { return version; }
    public String entrypoint() { return entrypoint; }
    public String worldlineApi() { return worldlineApi; }
    public String runtime() { return runtime; }

    /** Declared dependencies in canonical order; empty for format 1 packages. */
    public List<ModDependency> requires() { return requires; }

    static boolean validId(String value) { return ID.matcher(value).matches(); }

    static boolean validVersion(String value) { return VERSION.matcher(value).matches(); }

    private static String value(String row, String key, String expected) {
        String prefix = key + "=";
        require(row.startsWith(prefix), "expected mod descriptor field " + key);
        String value = row.substring(prefix.length());
        require(!value.isEmpty() && (expected == null || expected.equals(value)),
                "invalid mod descriptor field " + key);
        return value;
    }

    private static byte[] readBounded(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        for (int count; (count = input.read(buffer)) >= 0;) {
            output.write(buffer, 0, count);
            require(output.size() <= MAX_BYTES, "mod descriptor is too large");
        }
        return output.toByteArray();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
