import java.util.List;

/** JSON and HTML fragments for sanitized local diagnostic identities. */
final class EvidenceExplorerDiagnosticFormat {
    private EvidenceExplorerDiagnosticFormat() { }

    static String json(EvidenceExplorerDiagnosticCaptures.Snapshot value) {
        StringBuilder out = new StringBuilder("{\"schema\":\"worldline.diagnostic-captures.v1\",")
                .append("\"content_included\":false,\"paths_included\":false,")
                .append("\"capture_count\":").append(value.captureCount())
                .append(",\"complete_count\":").append(value.completeCount())
                .append(",\"partial_count\":").append(value.partialCount())
                .append(",\"orphan_count\":").append(value.orphanCount())
                .append(",\"malformed_count\":").append(value.malformedCount())
                .append(",\"file_count\":").append(value.fileCount())
                .append(",\"byte_count\":").append(value.byteCount()).append(",\"captures\":[");
        append(out, value.captures().stream().map(EvidenceExplorerDiagnosticFormat::capture).toList());
        return out.append("]}").toString();
    }

    static String html(EvidenceExplorerDiagnosticCaptures.Snapshot value) {
        StringBuilder out = new StringBuilder("<section><h2>Local diagnostic captures</h2>")
                .append("<p>Sanitized content identities only; paths and log content are excluded.</p>")
                .append("<div class=\"table\"><table><thead><tr><th>Capture</th><th>Family</th>")
                .append("<th>Backend</th><th>Workload</th><th>State</th><th>Files</th>")
                .append("<th>Bytes</th></tr></thead><tbody>");
        for (EvidenceExplorerDiagnosticCaptures.Capture capture : value.captures()) {
            out.append("<tr><td><code>").append(shortHash(capture.id())).append("</code></td><td>")
                    .append(html(capture.family())).append("</td><td>").append(html(capture.backend()))
                    .append("</td><td><strong>").append(html(capture.workload()))
                    .append("</strong></td><td><span class=\"tag\">")
                    .append(capture.complete() ? "complete" : "partial")
                    .append(capture.orphan() ? " / orphan" : "").append("</span></td><td>")
                    .append(capture.fileCount()).append("</td><td>").append(capture.bytes())
                    .append("</td></tr>");
        }
        return out.append("</tbody></table></div></section>").toString();
    }

    private static String capture(EvidenceExplorerDiagnosticCaptures.Capture value) {
        StringBuilder out = new StringBuilder("{\"id\":").append(quote(value.id()))
                .append(",\"family\":").append(quote(value.family())).append(",\"backend\":")
                .append(quote(value.backend())).append(",\"workload\":").append(quote(value.workload()))
                .append(",\"complete\":").append(value.complete()).append(",\"orphan\":")
                .append(value.orphan()).append(",\"file_count\":").append(value.fileCount())
                .append(",\"bytes\":").append(value.bytes()).append(",\"artifacts\":[");
        append(out, value.artifacts().stream().map(EvidenceExplorerDiagnosticFormat::artifact).toList());
        return out.append("]}").toString();
    }
    private static String artifact(EvidenceExplorerDiagnosticCaptures.Artifact value) {
        return "{\"kind\":" + quote(value.kind()) + ",\"sha256\":" + quote(value.sha256())
                + ",\"bytes\":" + value.bytes() + "}";
    }
    private static String shortHash(String value) { return html(value.substring(0, 12)); }
    private static String quote(String value) { return EvidenceExplorerFormat.quote(value); }
    private static String html(String value) { return EvidenceExplorerFormat.html(value); }
    private static void append(StringBuilder out, List<String> values) {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) out.append(','); out.append(values.get(index));
        }
    }
}
