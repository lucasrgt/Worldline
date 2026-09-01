package worldline.atlas;

import java.util.List;
import java.util.Locale;

/** Text renderers for the Atlas CLI. */
public final class AtlasQuery {
    private AtlasQuery() {}

    public static String status(AtlasStore store) {
        StringBuilder text = new StringBuilder();
        text.append("records=").append(store.size()).append('\n');
        for (String kind : AtlasKind.values()) {
            text.append(kind.replace('-', '_')).append('=').append(store.kind(kind).size())
                    .append('\n');
        }
        for (String status : AtlasStatus.values()) {
            int count = 0;
            for (AtlasRecord record : store.records()) {
                if (status.equals(record.status())) count++;
            }
            text.append(status.toLowerCase(Locale.US)).append('=').append(count).append('\n');
        }
        text.append("sha256=").append(store.sha256()).append('\n');
        return text.toString();
    }

    public static String coverage(AtlasStore store) {
        StringBuilder text = new StringBuilder();
        text.append("dimension");
        for (String dimension : AtlasSubsystems.DIMENSIONS) text.append('\t').append(dimension);
        text.append('\n');
        for (String subsystem : AtlasSubsystems.ALL) {
            text.append(subsystem);
            for (String dimension : AtlasSubsystems.DIMENSIONS) {
                AtlasRecord unit = store.get("atlas.coverage-unit." + subsystem + "." + dimension);
                String filled = "1".equals(unit.control()) ? "1" : "0";
                text.append('\t').append(filled).append('/').append(unit.denominator());
            }
            text.append('\n');
        }
        text.append("source=declared-coverage-unit\n");
        return text.toString();
    }

    public static String evidence(AtlasRecord record) {
        StringBuilder text = new StringBuilder();
        text.append("id=").append(record.id()).append('\n');
        text.append("status=").append(record.status()).append('\n');
        for (String item : record.evidence()) text.append("evidence=").append(item).append('\n');
        for (String item : record.refs()) text.append("ref=").append(item).append('\n');
        return text.toString();
    }

    public static String taxonomy(AtlasStore store) {
        AtlasTaxonomy.validate(store);
        return AtlasTaxonomy.render(store);
    }

    public static String tags(AtlasStore store) {
        AtlasTaxonomy.validate(store);
        return AtlasTaxonomy.tagIndex(store);
    }

    public static String gaps(List<AtlasRecord> gaps) {
        StringBuilder text = new StringBuilder();
        text.append("gaps=").append(gaps.size()).append('\n');
        for (AtlasRecord record : gaps) {
            text.append(record.id()).append('=').append(record.status()).append('\n');
        }
        return text.toString();
    }

    public static String search(List<AtlasRecord> found) {
        StringBuilder text = new StringBuilder();
        text.append("matches=").append(found.size()).append('\n');
        for (AtlasRecord record : found) text.append(record.id()).append('\n');
        return text.toString();
    }
}
