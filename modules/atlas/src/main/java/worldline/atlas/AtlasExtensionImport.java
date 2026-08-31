package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.extension.ExtensionAtlasPage;
import worldline.extension.ExtensionCapabilities;
import worldline.extension.ExtensionManifest;
import worldline.extension.WorldlineExtensionPlan;

/** Converts discovered public extension pages into canonical Atlas records. */
public final class AtlasExtensionImport {
    private AtlasExtensionImport() { }

    public static List<AtlasRecord> load(List<WorldlineExtensionPlan> plans) {
        if (plans == null) throw new NullPointerException("extension plans");
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        for (WorldlineExtensionPlan plan : plans) {
            if (plan == null) throw new NullPointerException("extension plan");
            ExtensionManifest manifest = plan.manifest();
            for (ExtensionAtlasPage page : plan.atlasIndexPages()) {
                List<String> evidence = evidence(manifest, page);
                List<String> refs = new ArrayList<String>(page.relations());
                if (!refs.contains("atlas.subsystem.mod-ecosystem")) {
                    refs.add("atlas.subsystem.mod-ecosystem");
                }
                records.add(AtlasRecord.of(page.id(), AtlasKind.ofId(page.id()),
                        AtlasStatus.OBSERVATIONAL, manifest.id() + "@" + manifest.version(),
                        AtlasSchema.SCOPE, page.title(), control(manifest, page), 0,
                        evidence, refs));
            }
        }
        return Collections.unmodifiableList(records);
    }

    private static List<String> evidence(ExtensionManifest manifest, ExtensionAtlasPage page) {
        List<String> values = new ArrayList<String>();
        values.add("extension-manifest");
        values.add(page.provenance());
        capabilities(values, "requires", manifest.requires());
        capabilities(values, "provides", manifest.provides());
        for (String tag : page.tags()) values.add("extension-tag:" + tag);
        return values;
    }

    private static void capabilities(List<String> target, String label,
            ExtensionCapabilities capabilities) {
        for (String value : capabilities.values()) target.add(label + ":" + value);
    }

    private static String control(ExtensionManifest manifest, ExtensionAtlasPage page) {
        return "extension=" + manifest.id() + ";version=" + manifest.version()
                + ";provenance=" + page.provenance() + ";extension-tags="
                + join(page.tags());
    }

    private static String join(List<String> values) {
        StringBuilder text = new StringBuilder();
        for (String value : values) {
            if (text.length() > 0) text.append('+');
            text.append(value);
        }
        return text.toString();
    }
}
