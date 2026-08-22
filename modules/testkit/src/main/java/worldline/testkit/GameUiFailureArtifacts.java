package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.GameUi;
import worldline.api.GameUiCapability;
import worldline.api.GameUiImage;
import worldline.api.GameUiNode;
import worldline.api.UiMinecraftRuntime;

/** Best-effort semantic and visual evidence that never replaces the original failure. */
final class GameUiFailureArtifacts {
    private static final int MAX_NODES = 4096;

    private GameUiFailureArtifacts() {}

    static void capture(AutomatedMinecraftRuntime runtime, ArtifactStore artifacts) {
        if (!(runtime instanceof UiMinecraftRuntime)) return;
        try {
            GameUi ui = ((UiMinecraftRuntime) runtime).ui();
            artifacts.write("failure.gui.txt", tree(ui).getBytes(StandardCharsets.UTF_8));
            if (ui.supports(GameUiCapability.SCREENSHOT)) {
                GameUiImage image = ui.screenshot();
                artifacts.write("failure.gui.ppm", image.ppm());
            }
        } catch (Exception | AssertionError captureFailure) {
            try {
                String message = captureFailure.getClass().getName() + ": "
                        + String.valueOf(captureFailure.getMessage()) + "\n";
                artifacts.write("failure.gui-capture.txt", message.getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) { /* The original test failure remains authoritative. */ }
        }
    }

    private static String tree(GameUi ui) {
        if (ui == null) throw new IllegalStateException("runtime returned no semantic UI");
        java.util.List<GameUiNode> nodes = ui.nodes();
        if (nodes == null || nodes.size() > MAX_NODES) throw new IllegalStateException("invalid UI node count");
        StringBuilder value = new StringBuilder("screen=").append(escape(ui.screen())).append('\n');
        for (int index = 0; index < nodes.size(); index++) {
            GameUiNode node = nodes.get(index);
            value.append(index).append('\t').append(escape(node.role())).append('\t')
                    .append(escape(node.name())).append('\t').append(node.index()).append('\t')
                    .append(node.itemId()).append('\t').append(node.count());
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(node.attributes()).entrySet()) {
                value.append('\t').append(escape(entry.getKey())).append('=').append(escape(entry.getValue()));
            }
            value.append('\n');
        }
        return value.toString();
    }

    private static String escape(String input) {
        if (input == null) return "<null>";
        return input.replace("\\", "\\\\").replace("\t", "\\t")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
