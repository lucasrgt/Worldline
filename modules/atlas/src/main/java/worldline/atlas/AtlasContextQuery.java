package worldline.atlas;

import java.util.List;

/** Text and JSON renderers for bounded agent context. */
public final class AtlasContextQuery {
    private AtlasContextQuery() {}

    public static String index(List<AtlasHit> hits) {
        StringBuilder text = new StringBuilder();
        text.append("matches=").append(hits.size()).append('\n');
        for (AtlasHit hit : hits) {
            AtlasRecord record = hit.record();
            text.append("score=").append(hit.score()).append('\t').append(record.id())
                    .append('\t').append(record.status()).append('\t')
                    .append(AtlasCertainty.of(record)).append('\t')
                    .append(record.subject()).append('\n');
        }
        return text.toString();
    }

    public static String text(String query, List<AtlasHit> hits) {
        StringBuilder text = new StringBuilder();
        text.append("query=").append(query).append('\n');
        text.append("records=").append(hits.size()).append('\n');
        for (AtlasHit hit : hits) {
            AtlasRecord record = hit.record();
            text.append("\n[").append(record.id()).append("]\n");
            text.append("kind=").append(record.kind()).append('\n');
            text.append("status=").append(record.status()).append('\n');
            text.append("certainty=").append(AtlasCertainty.of(record)).append('\n');
            text.append("relation=").append(hit.relation()).append('\n');
            text.append("subject=").append(record.subject()).append('\n');
            for (String evidence : record.evidence()) text.append("evidence=").append(evidence).append('\n');
            for (String ref : record.refs()) text.append("ref=").append(ref).append('\n');
        }
        return text.toString();
    }

    public static String json(String query, List<AtlasHit> hits) {
        StringBuilder text = new StringBuilder();
        text.append("{\"schema\":\"WORLDLINE-ATLAS-CONTEXT/1\",\"query\":\"")
                .append(escape(query)).append("\",\"records\":[");
        for (int index = 0; index < hits.size(); index++) {
            if (index > 0) text.append(',');
            AtlasHit hit = hits.get(index); AtlasRecord record = hit.record();
            text.append("{\"id\":\"").append(escape(record.id()))
                    .append("\",\"kind\":\"").append(record.kind())
                    .append("\",\"status\":\"").append(record.status())
                    .append("\",\"certainty\":\"").append(AtlasCertainty.of(record))
                    .append("\",\"relation\":\"").append(hit.relation())
                    .append("\",\"score\":").append(hit.score())
                    .append(",\"subject\":\"").append(escape(record.subject())).append("\"")
                    .append(",\"evidence\":").append(array(record.evidence()))
                    .append(",\"refs\":").append(array(record.refs())).append('}');
        }
        return text.append("]}\n").toString();
    }

    private static String array(List<String> values) {
        StringBuilder text = new StringBuilder("[");
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) text.append(',');
            text.append('\"').append(escape(values.get(index))).append('\"');
        }
        return text.append(']').toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
