package worldline.analysis;

import java.util.Collections;
import java.util.List;
import worldline.trace.CanonicalStateDocument;

/**
 * Deterministic self-contained HTML evidence for one trace or a structural
 * diff of two. Pure function of the inputs: no scripts, no assets, no
 * timestamps, byte-identical re-renders.
 */
public final class TraceHtml {
    private TraceHtml() {}

    public static String render(CanonicalStateDocument left, CanonicalStateDocument right) {
        if (left == null) throw new NullPointerException("left trace");
        boolean diff = right != null;
        StringBuilder html = new StringBuilder();
        line(html, "<!DOCTYPE html>");
        line(html, "<html><head><meta charset=\"utf-8\"><title>Worldline Evidence</title>");
        line(html, "<style>body{font-family:monospace;margin:24px;background:#111;color:#ddd}"
                + "table{border-collapse:collapse;margin-top:12px}"
                + "td,th{border:1px solid #444;padding:4px 10px;text-align:left}"
                + "th{background:#222}.same td{color:#888}"
                + ".div{background:#3a1515;color:#ff9c9c;font-weight:bold}"
                + ".fix{background:#12351a;color:#9cffb0;font-weight:bold}"
                + ".first td{outline:2px solid #ffb000}"
                + ".meta{color:#9cf}h2{color:#fff}</style></head><body>");
        line(html, "<h1>Worldline Evidence</h1>");
        line(html, "<p class=\"meta\">" + (diff ? "DIFF" : "TRACE") + " mode</p>");
        meta(html, "left", left);
        if (diff) meta(html, "right", right);
        Integer first = null;
        if (diff) {
            TraceDiff difference = TraceDiff.compare(left, right);
            if (!difference.diverged()) {
                line(html, "<p class=\"fix\">RESULT: traces are EQUAL</p>");
            } else {
                first = difference.recordIndex();
                line(html, "<p class=\"div\">RESULT: DIVERGED at record " + first
                        + ", field " + difference.field() + "</p>");
            }
        }
        records(html, left, right, first);
        line(html, "</body></html>");
        return html.toString();
    }

    private static void meta(StringBuilder html, String role, CanonicalStateDocument doc) {
        line(html, "<h2>" + role + "</h2>");
        line(html, "<table><tr><th>seed</th><th>signature</th></tr><tr><td>" + doc.seed()
                + "</td><td>" + doc.signature() + "</td></tr></table>");
        line(html, "<p>schema: " + escape(String.join(", ", doc.fields()))
                + " | records: " + doc.records().size() + "</p>");
    }

    private static void records(StringBuilder html, CanonicalStateDocument left,
            CanonicalStateDocument right, Integer first) {
        List<String> fields = left.fields();
        StringBuilder head = new StringBuilder("<tr><th>#</th><th>label</th>");
        for (String field : fields) head.append("<th>").append(escape(field)).append("</th>");
        if (right != null) for (String field : fields) {
            head.append("<th>").append(escape(field)).append("</th>");
        }
        line(html, head + "</tr>");
        int rows = Math.max(left.records().size(), right == null ? 0 : right.records().size());
        for (int index = 0; index < rows; index++) {
            String css = css(left, right, index, first);
            StringBuilder row = new StringBuilder("<tr").append(css.isEmpty() ? "" :
                    " class=\"" + css + "\"").append("><td>").append(index)
                    .append("</td><td>").append(escape(label(left, right, index))).append("</td>");
            for (Long value : values(left, index)) row.append(cell(value));
            if (right != null) for (Long value : values(right, index)) row.append(cell(value));
            line(html, row + "</tr>");
        }
        line(html, "</table>");
    }

    private static String css(CanonicalStateDocument left, CanonicalStateDocument right,
            int index, Integer first) {
        if (right == null) return "";
        if (first != null && first == index) return "first div";
        return sameValues(left, right, index) ? "same" : "div";
    }

    private static String cell(Long value) {
        return "<td>" + value + "</td>";
    }

    private static boolean sameValues(CanonicalStateDocument left, CanonicalStateDocument right,
            int index) {
        if (index >= left.records().size() || index >= right.records().size()) return false;
        return left.records().get(index).values().equals(right.records().get(index).values());
    }

    private static String label(CanonicalStateDocument left, CanonicalStateDocument right,
            int index) {
        return index < left.records().size()
                ? left.records().get(index).label() : right.records().get(index).label();
    }

    private static List<Long> values(CanonicalStateDocument doc, int index) {
        return index < doc.records().size()
                ? doc.records().get(index).values() : Collections.<Long>emptyList();
    }

    private static String escape(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char item = text.charAt(index);
            if (item == '<') escaped.append("&lt;");
            else if (item == '>') escaped.append("&gt;");
            else if (item == '&') escaped.append("&amp;");
            else if (item == '"') escaped.append("&quot;");
            else escaped.append(item);
        }
        return escaped.toString();
    }

    private static void line(StringBuilder target, String value) { target.append(value).append('\n'); }
}
