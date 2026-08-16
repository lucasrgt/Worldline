package worldline.mods;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
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

    private ModDescriptor(String id, String version, String entrypoint,
            String worldlineApi, String runtime) {
        this.id = id;
        this.version = version;
        this.entrypoint = entrypoint;
        this.worldlineApi = worldlineApi;
        this.runtime = runtime;
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
        require(rows.length == 7 && rows[6].isEmpty(), "mod descriptor must contain six fields");
        value(rows[0], "format", "1");
        String id = value(rows[1], "id", null);
        String version = value(rows[2], "version", null);
        String entrypoint = value(rows[3], "entrypoint", null);
        String api = value(rows[4], "worldline.api", null);
        String runtime = value(rows[5], "runtime", null);
        require(ID.matcher(id).matches(), "invalid mod id");
        require(VERSION.matcher(version).matches(), "invalid mod version");
        require(TYPE.matcher(entrypoint).matches(), "invalid mod entrypoint");
        require(api.matches("[1-9][0-9]*"), "invalid Worldline API version");
        require(TOKEN.matcher(runtime).matches(), "invalid mod runtime");
        return new ModDescriptor(id, version, entrypoint, api, runtime);
    }

    public String id() { return id; }
    public String version() { return version; }
    public String entrypoint() { return entrypoint; }
    public String worldlineApi() { return worldlineApi; }
    public String runtime() { return runtime; }

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
