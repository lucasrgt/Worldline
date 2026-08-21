package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import worldline.analysis.UiPageRunner;

/** Exports the open inventory semantic tree as a self-contained page. */
final class UiCommand {
    private UiCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length != 2 || !"ui".equals(arguments[0])) {
            return WorldlineCli.usage(error);
        }
        UiPageRunner runner = Checks.provider("worldline.ui.provider",
                "worldline.b173.B173UiPage", UiPageRunner.class);
        String html = runner.html();
        Files.write(Paths.get(arguments[1]), html.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        output.println("WORLDLINE_UI_EXPORT=PASS");
        output.println("page.sha256=" + Checks.sha256(html.getBytes(StandardCharsets.UTF_8)));
        return 0;
    }
}
