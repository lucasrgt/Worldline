package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import worldline.test.SuiteDefinition;
import worldline.test.TestDefinition;
import worldline.test.TestNode;
import worldline.test.TestPlan;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeProviders;
import worldline.test.WorldlineSpec;
import worldline.testkit.AgentReporter;
import worldline.testkit.CompositeReporter;
import worldline.testkit.DefaultReporter;
import worldline.testkit.DotReporter;
import worldline.testkit.JsonReporter;
import worldline.testkit.JUnitReporter;
import worldline.testkit.RunnerOptions;
import worldline.testkit.TestReporter;
import worldline.testkit.TestRunResult;
import worldline.testkit.TestRunner;
import worldline.testkit.VerboseReporter;

/** CLI front-end for Java specs and event-driven reporters. */
final class TestCommand {
    private static final String DEFAULT_PROVIDER = "worldline.b173.B173TestRuntimeProvider";
    private TestCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException, ReflectiveOperationException {
        if (arguments.length == 1 || arguments.length == 2 && !"--help".equals(arguments[1]))
            arguments = TestProjectConfig.expand(arguments);
        if (arguments.length == 2 && "--help".equals(arguments[1])) return usage(output, 0);
        if (arguments.length == 3 && command(arguments[1]) && "--help".equals(arguments[2]))
            return usage(output, 0);
        if (arguments.length < 3 || !command(arguments[1])) {
            return usage(error, 2);
        }
        Parsed parsed = parse(arguments);
        if ("list".equals(arguments[1]) || "inspect".equals(arguments[1])) {
            try (TestSpecLoader loader = new TestSpecLoader(parsed.source, parsed.classpath)) {
                List<WorldlineSpec> specs = loader.loadAll(parsed.spec);
                output.println("WORLDLINE_TEST_COLLECTION=PASS"); output.println("files=" + specs.size());
                for (WorldlineSpec spec : specs) {
                    TestPlan plan = spec.collect(); output.println("spec=" + plan.specName());
                    if ("inspect".equals(arguments[1])) inspect(plan.root(), "", output);
                    else list(plan.root(), "", output);
                }
                return 0;
            }
        }
        parsed.provider = provider(parsed.providerName);
        if ("watch".equals(arguments[1])) return TestWatch.run(parsed, output, error);
        if ("minimize".equals(arguments[1])) parsed.minimize = true;
        return execute(parsed, output).passed() ? 0 : 1;
    }

    static TestRunResult execute(Parsed parsed, PrintStream output)
            throws IOException, ReflectiveOperationException {
        try (TestSpecLoader loader = new TestSpecLoader(parsed.source, parsed.classpath)) {
            List<WorldlineSpec> specs = loader.loadAll(parsed.spec); RunnerOptions options = parsed.options();
            List<TestReporter> reporters = parsed.reporters(output);
            TestReporter reporter = reporters.size() == 1 ? reporters.get(0)
                    : new CompositeReporter(reporters.toArray(new TestReporter[0]));
            try { return new TestRunner().run(specs, options, reporter); }
            finally { reporter.close(); }
        }
    }

    private static Parsed parse(String[] arguments) throws ReflectiveOperationException {
        int firstOption = 3; String spec = null;
        if (arguments.length > 3 && !arguments[3].startsWith("-")) { spec = arguments[3]; firstOption = 4; }
        Parsed parsed = new Parsed(Paths.get(arguments[2]), spec);
        for (String option : Arrays.copyOfRange(arguments, firstOption, arguments.length)) {
            if (option.startsWith("--mod=")) parsed.mod = Paths.get(value(option));
            else if (option.startsWith("--classpath=")) parsed.classpath.addAll(classpath(value(option)));
            else if (option.startsWith("--world=")) parsed.world = Paths.get(value(option));
            else if (option.startsWith("--artifacts=")) parsed.artifacts = Paths.get(value(option));
            else if (option.startsWith("--snapshots=")) parsed.snapshots = Paths.get(value(option));
            else if (option.startsWith("--runtime-lock=")) parsed.runtimeLock = Paths.get(value(option));
            else if (option.startsWith("--provider=")) parsed.providerName = value(option);
            else if (option.startsWith("--reporter=")) parsed.reporterNames = value(option);
            else if (option.startsWith("--json=")) parsed.json = Paths.get(value(option));
            else if (option.startsWith("--junit=")) parsed.junit = Paths.get(value(option));
            else if (option.startsWith("--name=")) parsed.name = Pattern.compile(value(option));
            else if (option.startsWith("--file=")) parsed.file = Pattern.compile(value(option));
            else if (option.startsWith("--tag=")) parsed.tag = value(option);
            else if (option.startsWith("--line=")) parsed.line = integer(option);
            else if (option.startsWith("--seed=")) parsed.seed = Long.parseLong(value(option));
            else if (option.startsWith("--retry=")) parsed.retry = integer(option);
            else if (option.startsWith("--bail=")) parsed.bail = integer(option);
            else if (option.startsWith("--timeout=")) parsed.timeout = Long.parseLong(value(option));
            else if (option.equals("--ci")) parsed.ci = true;
            else if (option.equals("--allow-only")) parsed.allowOnly = true;
            else if (option.equals("--update-snapshots") || option.equals("-u")) parsed.update = true;
            else if (option.equals("--minimize")) parsed.minimize = true;
            else if (option.equals("--shuffle")) parsed.shuffle = true;
            else if (option.equals("--pass-with-no-tests")) parsed.allowEmpty = true;
            else if (option.equals("--no-unicode")) parsed.unicode = false;
            else if (option.equals("--no-runtime")) parsed.providerName = "none";
            else throw new IllegalArgumentException("unknown test option: " + option);
        }
        return parsed;
    }

    private static TestRuntimeProvider provider(String name) throws ReflectiveOperationException {
        if ("none".equals(name)) return null;
        return TestRuntimeProviders.discover(name);
    }
    private static boolean command(String value) {
        return "run".equals(value) || "list".equals(value) || "inspect".equals(value)
                || "watch".equals(value) || "minimize".equals(value);
    }
    private static String value(String option) {
        int split = option.indexOf('='); String value = option.substring(split + 1);
        if (value.isEmpty()) throw new IllegalArgumentException("empty test option: " + option); return value;
    }
    private static int integer(String option) { return Integer.parseInt(value(option)); }
    private static List<Path> classpath(String value) {
        List<Path> paths = new ArrayList<>();
        for (String item : value.split(Pattern.quote(java.io.File.pathSeparator), -1)) {
            if (item.trim().isEmpty()) throw new IllegalArgumentException("blank test classpath entry");
            paths.add(Paths.get(item.trim()));
        }
        return paths;
    }
    private static void list(SuiteDefinition suite, String prefix, PrintStream output) {
        String next = "root".equals(suite.name()) ? prefix
                : prefix.isEmpty() ? suite.name() : prefix + " > " + suite.name();
        for (TestNode child : suite.children()) {
            if (child instanceof SuiteDefinition) list((SuiteDefinition) child, next, output);
            else output.println("test=" + (next.isEmpty() ? child.name() : next + " > " + child.name()));
        }
    }
    private static void inspect(SuiteDefinition suite, String prefix, PrintStream output) {
        String next = "root".equals(suite.name()) ? prefix
                : prefix.isEmpty() ? suite.name() : prefix + " > " + suite.name();
        for (TestNode child : suite.children()) {
            if (child instanceof SuiteDefinition) inspect((SuiteDefinition) child, next, output);
            else {
                TestDefinition test = (TestDefinition) child;
                output.println("test=" + (next.isEmpty() ? test.name() : next + " > " + test.name())
                        + " location=" + test.location() + " tags=" + test.tags()
                        + " skip=" + test.skipped() + " todo=" + test.todoMode()
                        + " only=" + test.onlyMode() + " concurrent=" + test.concurrentMode()
                        + " retry=" + test.retries() + " timeout=" + test.timeoutMillis());
            }
        }
    }
    private static int usage(PrintStream output, int status) {
        output.println("usage: worldline test <run|watch|list|inspect|minimize> <spec.jar|classes> [spec.class] [options]");
        output.println("options: --classpath=PATHS --mod=JAR --world=DIR --provider=CLASS|none --reporter=default,verbose,dot,json,junit,agent");
        output.println("         --name=REGEX --file=REGEX --tag=TAG --line=N --seed=N --timeout=MS --retry=N --bail=N");
        output.println("         --ci --allow-only -u --minimize --shuffle --pass-with-no-tests --no-unicode");
        return status;
    }

    static final class Parsed {
        final Path source; final String spec; Path mod, world = Paths.get("worldline-test-world");
        Path artifacts = Paths.get(".worldline/test-results"), snapshots = Paths.get("__snapshots__");
        Path runtimeLock = Paths.get(".worldline/official-runtime.lock");
        Path json, junit; String providerName = DEFAULT_PROVIDER, reporterNames = "default", tag;
        final List<Path> classpath = new ArrayList<>();
        Pattern name, file; TestRuntimeProvider provider; long seed = 173L, timeout; int retry, bail, line;
        boolean ci, allowOnly, update, minimize, shuffle, allowEmpty, unicode = true;
        Parsed(Path source, String spec) { this.source = source; this.spec = spec; }
        RunnerOptions options() {
            RunnerOptions value = new RunnerOptions().provider(provider).world(world).artifacts(artifacts)
                    .snapshots(snapshots).runtimeLock(runtimeLock).seed(seed).retry(retry).bail(bail).line(line).ci(ci)
                    .allowOnly(allowOnly).updateSnapshots(update).minimize(minimize).shuffle(shuffle).file(file)
                    .passWithNoTests(allowEmpty).unicode(unicode).name(name).tag(tag);
            if (mod != null) value.mod(mod); if (timeout > 0) value.timeout(timeout); return value;
        }
        List<TestReporter> reporters(PrintStream output) {
            List<TestReporter> values = new ArrayList<>();
            for (String name : reporterNames.split(",")) {
                if (name.equals("default")) values.add(new DefaultReporter(output, unicode));
                else if (name.equals("verbose")) values.add(new VerboseReporter(output, unicode));
                else if (name.equals("dot")) values.add(new DotReporter(output, unicode));
                else if (name.equals("agent")) values.add(new AgentReporter(output));
                else if (name.equals("json")) values.add(new JsonReporter(json == null
                        ? Paths.get(".worldline/reports/worldline-test.json") : json));
                else if (name.equals("junit")) values.add(new JUnitReporter(junit == null
                        ? Paths.get(".worldline/reports/worldline-test.xml") : junit));
                else throw new IllegalArgumentException("unknown reporter: " + name);
            }
            if (values.isEmpty()) throw new IllegalArgumentException("at least one reporter is required");
            return values;
        }
    }
}
