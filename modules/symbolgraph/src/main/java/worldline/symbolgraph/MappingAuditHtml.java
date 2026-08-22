package worldline.symbolgraph;

import java.util.LinkedHashMap;
import java.util.Map;

/** Deterministic, self-contained, navigable mapping qualification audit. */
public final class MappingAuditHtml {
    private static final String[] STATES = {
        "UNQUALIFIED", "SUPPORTED", "CORROBORATED", "CONFLICT"
    };

    private MappingAuditHtml() {}

    public static String render(MappingQualificationQueue queue, MappingEvidenceReport evidence) {
        if (queue == null || evidence == null) throw new NullPointerException("mapping audit input");
        Map<String, Integer> counts = counts(queue, evidence);
        StringBuilder html = new StringBuilder();
        line(html, "<!DOCTYPE html>");
        line(html, "<html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>Worldline Mapping Audit</title>");
        line(html, "<style>body{font:14px system-ui;margin:24px;background:#10131a;color:#e8ebf2}"
                + "h1{margin-bottom:4px}.meta{color:#aab4c8;font-family:monospace}"
                + ".filters{display:flex;gap:8px;flex-wrap:wrap;margin:20px 0}"
                + "button{border:1px solid #5c6880;background:#1c2230;color:inherit;padding:7px 10px;cursor:pointer}"
                + "table{border-collapse:collapse;width:100%;font-size:12px}th{position:sticky;top:0;background:#222a3a}"
                + "th,td{border:1px solid #394258;padding:6px;text-align:left;vertical-align:top}"
                + "tr:target{outline:3px solid #ffd166}.CONFLICT{background:#4a2028}.CORROBORATED{background:#173c2c}"
                + "code{white-space:pre-wrap;overflow-wrap:anywhere}.muted{color:#9ba7bc}[hidden]{display:none}</style>");
        line(html, "</head><body><h1>Worldline Mapping Audit</h1>");
        line(html, "<p class=\"meta\">queue.sha256=" + queue.sha256() + "<br>evidence.sha256="
                + evidence.sha256() + "<br>items=" + queue.items().size() + "</p>");
        html.append("<nav class=\"filters\" aria-label=\"Status filters\"><button data-filter=\"ALL\">ALL ")
                .append(queue.items().size()).append("</button>");
        for (String state : STATES) html.append("<button data-filter=\"").append(state).append("\">")
                .append(state).append(' ').append(counts.get(state)).append("</button>");
        line(html, "</nav><table><thead><tr><th>item</th><th>status</th><th>priority / gap</th>"
                + "<th>identity</th><th>symbol</th><th>namespace names</th><th>evidence</th></tr></thead><tbody>");
        for (MappingQualificationQueue.Item item : queue.items()) row(html, item, evidence);
        line(html, "</tbody></table><script>(()=>{const rows=[...document.querySelectorAll('tbody tr')];"
                + "for(const b of document.querySelectorAll('[data-filter]'))b.onclick=()=>{const f=b.dataset.filter;"
                + "for(const r of rows)r.hidden=f!=='ALL'&&r.dataset.status!==f;};})();</script></body></html>");
        return html.toString();
    }

    private static Map<String, Integer> counts(MappingQualificationQueue queue,
            MappingEvidenceReport evidence) {
        Map<String, Integer> values = new LinkedHashMap<String, Integer>();
        for (String state : STATES) values.put(state, Integer.valueOf(0));
        for (MappingQualificationQueue.Item item : queue.items()) {
            String state = evidence.status(item.id());
            Integer count = values.get(state);
            if (count == null) throw new IllegalArgumentException("unknown evidence status " + state);
            values.put(state, Integer.valueOf(count.intValue() + 1));
        }
        return values;
    }

    private static void row(StringBuilder html, MappingQualificationQueue.Item item,
            MappingEvidenceReport evidence) {
        String id = item.id(), status = evidence.status(id);
        html.append("<tr id=\"item-").append(id).append("\" data-status=\"").append(status)
                .append("\" class=\"").append(status).append("\"><td><a href=\"#item-")
                .append(id).append("\"><code>").append(id, 0, 12).append("</code></a></td><td>")
                .append(status).append("</td><td>").append(item.priority()).append("<br>")
                .append(escape(item.gap())).append("</td><td>").append(escape(item.identity()))
                .append("<br><span class=\"muted\">").append(escape(item.side())).append(" / ")
                .append(escape(item.kind())).append("</span></td><td><code>")
                .append(escape(symbol(item))).append("</code></td><td><code>")
                .append(escape(names(item))).append("</code></td><td>sources: ")
                .append(escape(evidence.sources(id))).append("<br>aliases: ")
                .append(escape(evidence.aliases(id))).append("<br>next: ")
                .append(escape(item.nextEvidence())).append("</td></tr>\n");
    }

    private static String symbol(MappingQualificationQueue.Item item) {
        return dash(item.owner()) + "/" + item.name() + " " + dash(item.descriptor());
    }

    private static String names(MappingQualificationQueue.Item item) {
        return "nostalgia=" + dash(item.nostalgia()) + "\nretroClient=" + dash(item.retroClient())
                + "\nretroServer=" + dash(item.retroServer());
    }

    private static String dash(String value) { return value.isEmpty() ? "-" : value; }
    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    private static void line(StringBuilder target, String value) { target.append(value).append('\n'); }
}
