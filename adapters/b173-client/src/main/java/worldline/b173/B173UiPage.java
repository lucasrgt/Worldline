package worldline.b173;

import worldline.analysis.UiPageRunner;
import worldline.api.GameUiNode;
import worldline.api.WorldSource;
import java.nio.file.Paths;
import java.util.List;

/** Exports the open inventory semantic tree as a self-contained page. */
public final class B173UiPage implements UiPageRunner {
    private static final long SEED = 17320110707L;

    @Override
    public String html() {
        B173Runtime runtime = B173Runtimes.create(SEED);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "ui-export")));
            runtime.gui().open(new net.minecraft.src.GuiInventory(
                    runtime.backend().client().thePlayer));
            List<GameUiNode> nodes = runtime.ui().nodes();
            return render(runtime.ui().screen(), nodes);
        } finally { runtime.close(); }
    }

    private static String render(String screen, List<GameUiNode> nodes) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html><head><meta charset=\"utf-8\">")
                .append("<title>Worldline UI Export</title><style>")
                .append("body{font-family:monospace;background:#111;color:#ddd;margin:24px}")
                .append("table{border-collapse:collapse}td,th{border:1px solid #444;padding:3px 10px}")
                .append("th{background:#222}.slot{color:#9cf}.empty{color:#666}</style></head><body>\n")
                .append("<h1>Worldline UI Export</h1>\n<p>screen=")
                .append(escape(screen)).append(" nodes=").append(nodes.size()).append("</p>\n")
                .append("<table><tr><th>#</th><th>role</th><th>name</th><th>item</th>")
                .append("<th>count</th></tr>\n");
        for (int index = 0; index < nodes.size(); index++) {
            GameUiNode node = nodes.get(index);
            String css = node.role().equals(GameUiNode.SLOT)
                    ? (node.empty() ? "slot empty" : "slot") : "";
            html.append("<tr").append(css.isEmpty() ? "" : " class=\"" + css + "\"")
                    .append("><td>").append(index)
                    .append("</td><td>").append(escape(node.role()))
                    .append("</td><td>").append(escape(node.name()))
                    .append("</td><td>").append(node.empty() ? "-" : node.itemId())
                    .append("</td><td>").append(node.empty() ? "-" : node.count())
                    .append("</td></tr>\n");
        }
        html.append("</table></body></html>\n");
        return html.toString();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
