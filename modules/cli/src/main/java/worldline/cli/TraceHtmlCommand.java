package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import worldline.analysis.TraceHtml;

/** Self-contained HTML evidence for one trace or a structural diff. */
final class TraceHtmlCommand {
    private TraceHtmlCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        boolean single = arguments.length == 4 && "html".equals(arguments[1]);
        boolean diff = arguments.length == 5 && "html".equals(arguments[1]);
        if (!"trace".equals(arguments[0]) || !(single || diff)) {
            return WorldlineCli.usage(error);
        }
        worldline.trace.CanonicalStateDocument left = Traces.read(arguments[2]);
        worldline.trace.CanonicalStateDocument right = diff ? Traces.read(arguments[3]) : null;
        String html = TraceHtml.render(left, right);
        Files.write(Paths.get(arguments[arguments.length - 1]),
                html.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("WORLDLINE_TRACE_HTML=PASS");
        output.println("mode=" + (diff ? "diff" : "trace"));
        output.println("left.sha256=" + left.signature());
        if (right != null) output.println("right.sha256=" + right.signature());
        output.println("bytes=" + html.getBytes(StandardCharsets.UTF_8).length);
        return 0;
    }
}
