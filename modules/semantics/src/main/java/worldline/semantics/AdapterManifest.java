package worldline.semantics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import worldline.api.SemanticMapping;

/**
 * Fail-closed adapter site list. Adapters depend on catalog roles; the catalog
 * never owns Aero or other external types.
 */
public final class AdapterManifest {
    public static final String SCHEMA = "worldline.adapter.semantics.v1";
    private static final Pattern ADAPTER = Pattern.compile("[a-z][a-z0-9-]{0,63}");
    private static final Pattern SITE = Pattern.compile("worldline/[A-Za-z0-9_$./-]+#[A-Za-z0-9_$.]+");
    private final String adapter, prefix;
    private final List<Site> sites;

    private AdapterManifest(String adapter, String prefix, List<Site> sites) {
        this.adapter = adapter;
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
        String expected = path.getParent().getParent().getFileName().toString();
        if (!adapter.equals(expected)) throw new IllegalArgumentException("adapter directory mismatch");
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
        return new AdapterManifest(adapter, prefix, Collections.unmodifiableList(sites));
    }

    public static List<AdapterManifest> loadAll(Path adapters, SemanticCatalog catalog)
            throws IOException {
        if (adapters == null || catalog == null) throw new NullPointerException("adapters");
        List<AdapterManifest> manifests = new ArrayList<AdapterManifest>();
        if (!Files.isDirectory(adapters)) return Collections.unmodifiableList(manifests);
        try (DirectoryStream<Path> children = Files.newDirectoryStream(adapters)) {
            for (Path child : children) {
                Path manifest = child.resolve("semantics").resolve("manifest.properties");
                if (Files.isRegularFile(manifest)) manifests.add(load(manifest, catalog));
            }
        }
        Collections.sort(manifests, (left, right) -> left.adapter.compareTo(right.adapter));
        return Collections.unmodifiableList(manifests);
    }

    public String adapter() { return adapter; }
    public String ownerPrefix() { return prefix; }
    public List<Site> sites() { return sites; }

    public String render() {
        StringBuilder text = new StringBuilder();
        text.append("adapter=").append(adapter).append('\n');
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
        if (value.startsWith("aero/") || value.contains("/aero/")) {
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
