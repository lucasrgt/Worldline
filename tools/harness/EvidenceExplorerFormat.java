import java.util.List;

/** Canonical JSON and dependency-free static HTML for the evidence explorer. */
final class EvidenceExplorerFormat {
    private EvidenceExplorerFormat() { }

    static String json(EvidenceExplorerSnapshot value) {
        StringBuilder out = new StringBuilder("{\n  \"schema\":\"worldline.evidence-explorer.v1\",")
                .append("\n  \"verdict_authority\":false,\n  \"ephemeral_state_included\":false,")
                .append("\n  \"train\":").append(train(value.train())).append(',')
                .append("\n  \"summary\":{\"smokes\":").append(value.smokes().size())
                .append(",\"dispositions\":").append(value.dispositions().size())
                .append(",\"orphan_proofs\":").append(value.orphanProofs().size()).append("},")
                .append("\n  \"smokes\":[");
        append(out, value.smokes().stream().map(EvidenceExplorerFormat::smoke).toList());
        out.append("],\n  \"dispositions\":[");
        append(out, value.dispositions().stream().map(EvidenceExplorerFormat::disposition).toList());
        out.append("],\n  \"orphan_proofs\":[");
        append(out, value.orphanProofs().stream().map(EvidenceExplorerFormat::quote).toList());
        return out.append("]\n}\n").toString();
    }

    static String html(EvidenceExplorerSnapshot value) {
        StringBuilder out = new StringBuilder("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
                .append("<title>Worldline Evidence Explorer</title><style>").append(css())
                .append("</style></head><body><main><header><p class=\"eyebrow\">Worldline</p>")
                .append("<h1>Evidence Explorer</h1><p>Derived read model; never a verdict source.</p></header>")
                .append("<section class=\"cards\">").append(card("Smoke pins", value.smokes().size()))
                .append(card("Executed here", value.train().executed()))
                .append(card("Carried", value.train().carried()))
                .append(card("Dispositions", value.dispositions().size())).append("</section>")
                .append("<section><div class=\"bar\"><h2>Behavior evidence</h2>")
                .append("<input id=\"filter\" placeholder=\"Filter smoke, source, lane…\"></div>")
                .append("<div class=\"table\"><table id=\"smokes\"><thead><tr><th>Smoke</th><th>Lane</th>")
                .append("<th>Pin provenance</th><th>Train</th><th>Last exact head</th><th>Fingerprint</th>")
                .append("</tr></thead><tbody>");
        for (EvidenceExplorerSnapshot.Smoke smoke : value.smokes()) out.append(smokeRow(smoke));
        out.append("</tbody></table></div></section><section><h2>Supervised dispositions</h2>")
                .append("<div class=\"table\"><table><thead><tr><th>ID</th><th>State</th><th>Cause</th>")
                .append("<th>Scar</th></tr></thead><tbody>");
        for (EvidenceExplorerSnapshot.Disposition item : value.dispositions()) {
            out.append(dispositionRow(item));
        }
        out.append("</tbody></table></div></section><footer>Machine model: ")
                .append("<a href=\"evidence.json\">evidence.json</a>")
                .append(" · train base <code>").append(html(value.train().base())).append("</code></footer></main>")
                .append("<script>const f=document.querySelector('#filter'),")
                .append("r=[...document.querySelectorAll('#smokes tbody tr')];")
                .append("f.oninput=()=>{const q=f.value.toLowerCase();")
                .append("r.forEach(x=>x.hidden=!x.innerText.toLowerCase().includes(q))}</script>")
                .append("</body></html>\n"); return out.toString();
    }

    private static String train(EvidenceExplorerSnapshot.Train value) {
        return "{\"base\":" + quote(value.base()) + ",\"pins\":" + value.pins()
                + ",\"carried\":" + value.carried() + ",\"imported\":" + value.imported()
                + ",\"executed\":" + value.executed() + ",\"pending\":" + value.pending() + "}";
    }
    private static String smoke(EvidenceExplorerSnapshot.Smoke value) {
        return "{\"id\":" + quote(value.id()) + ",\"proof_kind\":" + quote(value.proofKind())
                + ",\"fingerprint\":" + quote(value.fingerprint()) + ",\"observation_sha256\":"
                + quote(value.observation()) + ",\"proof_sha256\":" + quote(value.evidence())
                + ",\"pin_source\":" + quote(value.source()) + ",\"train_kind\":"
                + quote(value.trainKind()) + ",\"prior_fingerprint\":" + nullable(value.priorFingerprint())
                + ",\"last_executed_head\":" + nullable(value.lastExecutedHead()) + "}";
    }
    private static String disposition(EvidenceExplorerSnapshot.Disposition value) {
        return "{\"id\":" + quote(value.id()) + ",\"state\":" + quote(value.state())
                + ",\"cause\":" + quote(value.cause()) + ",\"scar\":" + nullable(value.scar())
                + ",\"head\":" + nullable(value.head()) + ",\"archive_sha256\":"
                + nullable(value.archiveSha256()) + ",\"next_action\":" + nullable(value.nextAction()) + "}";
    }
    private static String smokeRow(EvidenceExplorerSnapshot.Smoke value) {
        return "<tr><td><strong>" + html(value.id()) + "</strong></td><td>" + html(value.proofKind())
                + "</td><td><span class=\"tag\">" + html(value.source()) + "</span></td><td>"
                + html(value.trainKind()) + "</td><td><code>" + shortHash(value.lastExecutedHead())
                + "</code></td><td><code title=\"" + html(value.fingerprint()) + "\">"
                + shortHash(value.fingerprint()) + "</code></td></tr>";
    }
    private static String dispositionRow(EvidenceExplorerSnapshot.Disposition value) {
        return "<tr><td>" + html(value.id()) + "</td><td><span class=\"tag warn\">"
                + html(value.state()) + "</span></td><td>" + html(value.cause()) + "</td><td><code>"
                + html(value.scar() == null ? "—" : value.scar()) + "</code></td></tr>";
    }
    private static String card(String label, int value) {
        return "<article><strong>" + value + "</strong><span>" + html(label) + "</span></article>";
    }
    private static void append(StringBuilder out, List<String> values) {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) out.append(','); out.append("\n    ").append(values.get(index));
        }
        if (!values.isEmpty()) out.append('\n').append("  ");
    }
    private static String nullable(String value) { return value == null ? "null" : quote(value); }
    private static String quote(String value) {
        StringBuilder out = new StringBuilder().append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (character < 0x20) out.append("\\u%04x".formatted((int) character));
                    else out.append(character);
                }
            }
        }
        return out.append('"').toString();
    }
    private static String html(String value) { return value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
    private static String shortHash(String value) { return value == null ? "—" : html(
            value.length() <= 12 ? value : value.substring(0, 12)); }
    private static String css() { return "*{box-sizing:border-box}body{margin:0;background:#0b1020;color:#e8edf7;"
            + "font:14px system-ui}main{max-width:1440px;margin:auto;padding:48px}header{padding:28px 0}"
            + ".eyebrow{color:#7dd3fc;text-transform:uppercase;letter-spacing:.18em}h1{font-size:42px;margin:6px 0}"
            + "h2{margin:34px 0 14px}.cards{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}"
            + ".cards article,.table{background:#121a2d;border:1px solid #26324b;border-radius:14px}"
            + ".cards article{padding:20px}.cards strong{display:block;font-size:30px}.cards span{color:#9aabc5}"
            + ".bar{display:flex;align-items:end;justify-content:space-between}input{background:#0b1020;color:#fff;"
            + "border:1px solid #33415f;border-radius:8px;padding:10px 14px;min-width:280px}.table{overflow:auto}"
            + "table{border-collapse:collapse;width:100%}th,td{text-align:left;padding:11px 14px;"
            + "border-bottom:1px solid #26324b}"
            + "th{color:#9aabc5;font-size:12px;text-transform:uppercase}.tag{background:#16334a;color:#7dd3fc;"
            + "border-radius:999px;padding:3px 8px}.warn{background:#482d18;color:#fdba74}code{color:#c4b5fd}"
            + "footer{color:#9aabc5;padding:28px 0}a{color:#7dd3fc}@media(max-width:800px){main{padding:20px}"
            + ".cards{grid-template-columns:repeat(2,1fr)}.bar{display:block}input{width:100%}}"; }
}
