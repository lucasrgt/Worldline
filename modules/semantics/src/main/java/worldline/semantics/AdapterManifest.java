package worldline.semantics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import worldline.api.SemanticMapping;

/**
 * Fail-closed adapter site list. Drivers implement a game runtime. Extensions
 * bind overlay sites to catalog roles. The catalog never owns Aero types.
 */
public final class AdapterManifest {
    public static final String SCHEMA = "worldline.adapter.semantics.v1";
    public static final List<String> DRIVERS = Collections.unmodifiableList(
            Arrays.asList("b173-client", "b173-server", "modloader-forge", "stationapi"));
    private static final Pattern ADAPTER = Pattern.compile("[a-z][a-z0-9-]{0,63}");
    private static final Pattern SITE = Pattern.compile("worldline/[A-Za-z0-9_$./-]+#[A-Za-z0-9_$.]+");
    private final String adapter, kind, prefix;
    private final List<Site> sites;

    private AdapterManifest(String adapter, String kind, String prefix, List<Site> sites) {
        this.adapter = adapter;
        this.kind = kind;
        this.prefix = prefix;
        this.sites = sites;
    }

    public static AdapterManifest load(Path path, SemanticCatalog catalog) throws IOException {
        if (path == null || catalog == null) throw new NullPointerException("manifest");
        Properties fields = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            fields.load(reader);
        }
        if (!SCHEMA.equals(required(fields, "schema"))) {
            throw new IllegalArgumentException("unsupported adapter schema");
        }
        String adapter = required(fields, "adapter");
        if (!ADAPTER.matcher(adapter).matches()) throw new IllegalArgumentException("adapter");
        String expected = ownerDirectory(path);
        if (!adapter.equals(expected)) throw new IllegalArgumentException("adapter directory mismatch");
        String kind = required(fields, "kind");
        if (!kind.equals("driver") && !kind.equals("extension")) {
            throw new IllegalArgumentException("kind");
        }
        if (kind.equals("driver") != DRIVERS.contains(adapter)) {
            throw new IllegalArgumentException("adapter kind");
        }
        String prefix = required(fields, "owner.prefix");
        rejectExternal(prefix);
        if (!prefix.startsWith("worldline/") || !prefix.endsWith("/")) {
            throw new IllegalArgumentException("owner.prefix must be a worldline/ package");
        }
        List<Site> sites = new ArrayList<Site>();
        Set<String> seen = new LinkedHashSet<String>();
        for (int index = 1; ; index++) {
            String site = fields.getProperty("site." + index);
            if (site == null) break;
            site = site.trim();
            String role = required(fields, "role." + index);
            String subject = fields.getProperty("subject." + index, "").trim();
            if (!SITE.matcher(site).matches() || !site.startsWith(prefix)) {
                throw new IllegalArgumentException("invalid adapter site " + site);
            }
            SemanticMapping mapping = catalog.role(role);
            if (!subject.isEmpty()) {
                int dot = subject.lastIndexOf('.');
                if (dot <= 0) throw new IllegalArgumentException("invalid subject " + subject);
                String owner = subject.substring(0, dot), name = subject.substring(dot + 1);
                rejectExternal(owner);
                SemanticMapping symbol = catalog.symbol(owner, name);
                if (!symbol.role().equals(role)) {
                    throw new IllegalArgumentException("subject " + subject + " is not " + role);
                }
            }
            if (mapping.owner().startsWith(prefix) && !site.equals(mapping.owner() + "#" + mapping.name())) {
                throw new IllegalArgumentException("site must name catalog owner " + role);
            }
            if (!seen.add(site)) throw new IllegalArgumentException("duplicate site " + site);
            sites.add(new Site(site, role, subject));
        }
        int highest = 0;
        for (String key : fields.stringPropertyNames()) {
            if (key.startsWith("site.")) {
                try { highest = Math.max(highest, Integer.parseInt(key.substring(5))); }
                catch (NumberFormatException error) {
                    throw new IllegalArgumentException("invalid site key " + key);
                }
            }
        }
        if (sites.isEmpty() || highest != sites.size()) {
            throw new IllegalArgumentException("adapter sites must be contiguous");
        }
        for (SemanticMapping mapping : catalog.mappings()) {
            if (!mapping.owner().startsWith(prefix)) continue;
            String expectedSite = mapping.owner() + "#" + mapping.name();
            if (!seen.contains(expectedSite)) {
                throw new IllegalArgumentException("unlisted adapter symbol " + expectedSite);
            }
        }
        return new AdapterManifest(adapter, kind, prefix, Collections.unmodifiableList(sites));
    }

    public static List<AdapterManifest> loadAll(Path adapters, SemanticCatalog catalog)
            throws IOException {
        if (adapters == null || catalog == null) throw new NullPointerException("adapters");
        List<AdapterManifest> manifests = new ArrayList<AdapterManifest>();
        collect(adapters, "semantics", catalog, manifests);
        return finish(manifests);
    }

    public static List<AdapterManifest> loadRepository(Path root, SemanticCatalog catalog)
            throws IOException {
        if (root == null || catalog == null) throw new NullPointerException("adapters");
        List<AdapterManifest> manifests = new ArrayList<AdapterManifest>();
        collect(root.resolve("adapters"), "semantics", catalog, manifests);
        collect(root.resolve("worldline").resolve("extensions"), null, catalog, manifests);
        return finish(manifests);
    }

    private static void collect(Path directory, String nested, SemanticCatalog catalog,
            List<AdapterManifest> manifests) throws IOException {
        if (!Files.isDirectory(directory)) return;
        try (DirectoryStream<Path> children = Files.newDirectoryStream(directory)) {
            for (Path child : children) {
                if (!Files.isDirectory(child)) continue;
                Path manifest = nested == null ? child.resolve("manifest.properties")
                        : child.resolve(nested).resolve("manifest.properties");
                if (Files.isRegularFile(manifest)) manifests.add(load(manifest, catalog));
            }
        }
    }

    private static List<AdapterManifest> finish(List<AdapterManifest> manifests) {
        Set<String> seen = new LinkedHashSet<String>();
        for (AdapterManifest manifest : manifests) {
            if (!seen.add(manifest.adapter)) throw new IllegalArgumentException("duplicate adapter");
        }
        Collections.sort(manifests, (left, right) -> left.adapter.compareTo(right.adapter));
        return Collections.unmodifiableList(manifests);
    }

    private static String ownerDirectory(Path manifest) {
        Path parent = manifest.getParent();
        if (parent == null) throw new IllegalArgumentException("adapter directory mismatch");
        if ("semantics".equals(parent.getFileName().toString())) {
            Path owner = parent.getParent();
            if (owner == null) throw new IllegalArgumentException("adapter directory mismatch");
            return owner.getFileName().toString();
        }
        return parent.getFileName().toString();
    }

    public String adapter() { return adapter; }
    public String kind() { return kind; }
    public String ownerPrefix() { return prefix; }
    public List<Site> sites() { return sites; }

    public String render() {
        StringBuilder text = new StringBuilder();
        text.append("adapter=").append(adapter).append('\n');
        text.append("kind=").append(kind).append('\n');
        text.append("sites=").append(sites.size()).append('\n');
        for (Site site : sites) {
            text.append(site.role).append('=').append(site.site);
            if (!site.subject.isEmpty()) text.append(" subject=").append(site.subject);
            text.append('\n');
        }
        return text.toString();
    }

    private static String required(Properties fields, String key) {
        String value = fields.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("missing " + key);
        return value.trim();
    }

    private static void rejectExternal(String owner) {
        String value = owner.replace('.', '/');
        if (value.startsWith("aero/") || value.contains("/aero/modellib")) {
            throw new IllegalArgumentException("catalog must not own Aero types");
        }
    }

    public static final class Site {
        private final String site, role, subject;
        private Site(String site, String role, String subject) {
            this.site = site; this.role = role; this.subject = subject;
        }
        public String site() { return site; }
        public String role() { return role; }
        public String subject() { return subject; }
    }
}
