import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/** Moves deterministic Candidate policy failures ahead of the frozen readiness manifest. */
final class CandidatePolicyReadiness {
    static final String STATEMENTS = "NYA-01M0ZNQMM5YM02Z1FQQGB97HE1";
    static final String FLOWING_WATER = "NYA-01M0ZNQNP5W87J0NNW7219ZNFQ";
    static final String DATA_TRACE = "NYA-01M0ZNQP9E7K1X69DBZXFGB8PR";
    private static final Pattern NOTIFYING_FLOWING_WATER = Pattern.compile(
            "setBlockAndMetadataWithNotify\\s*\\([^;]{0,400}Block[.]waterMoving[.]blockID",
            Pattern.DOTALL);

    private CandidatePolicyReadiness() {
    }

    static void verify(Path root, String id, Properties descriptor) throws Exception {
        Path milestone = root.resolve("smokes").resolve(id);
        Path runner = root.resolve(required(descriptor, "runner.source")).normalize();
        new SmokeStatementBudget(root).candidate(runner, milestone);
        Path symbols = milestone.resolve("symbols.map");
        if (Files.isRegularFile(symbols)) {
            verifySymbols(root, descriptor, symbols);
        }
        if ("tools/smoke/DataDrivenCycle.java".equals(descriptor.getProperty("runner.source"))) {
            verifyDataDrivenTrace(milestone, descriptor);
        }
        if ("flowing-water-freeze".equals(descriptor.getProperty("behavior"))) {
            verifyFlowingWaterFixture(milestone);
        }
    }

    static List<String> applicable(Path root, String id, Properties descriptor) {
        var result = new java.util.ArrayList<String>();
        if (Files.isDirectory(root.resolve("smokes").resolve(id).resolve("src"))) {
            result.add(STATEMENTS);
        }
        if ("tools/smoke/DataDrivenCycle.java".equals(descriptor.getProperty("runner.source"))) {
            result.add(DATA_TRACE);
        }
        if ("flowing-water-freeze".equals(descriptor.getProperty("behavior"))) {
            result.add(FLOWING_WATER);
        }
        return List.copyOf(result);
    }

    static void selfTest() throws Exception {
        List<String> mappings = List.of("c\tOwner\tclientOwner\tserverOwner",
                "\tm\t()V\tnamed\t\tb");
        verifySymbolRows(mappings, List.of("Owner\tm\t()V\tnamed\t\tb"));
        rejects(() -> verifySymbolRows(mappings, List.of("Owner\tm\t()V\tnamed\tb\tb")),
                "wrong client mapping was accepted");
        String trace = "v1|fixture=valid|oracle=MATCH";
        Properties descriptor = new Properties();
        descriptor.setProperty("expected.trace", trace);
        descriptor.setProperty("expected.signature", digest(trace));
        verifyTrace(descriptor, trace);
        descriptor.setProperty("expected.signature", "0".repeat(64));
        rejects(() -> verifyTrace(descriptor, trace), "drifted trace signature was accepted");
        verifyFlowSource("setBlockAndMetadata(x, y, z, Block.waterMoving.blockID, 1);");
        rejects(() -> verifyFlowSource(
                "setBlockAndMetadataWithNotify(x, y, z, Block.waterMoving.blockID, 1);"),
                "notifying flowing-water setup was accepted");
    }

    private static void verifySymbols(Path root, Properties descriptor, Path symbols)
            throws Exception {
        Path workspace = root.resolve(required(descriptor, "workspace")).normalize();
        Path mappings = workspace.resolve("conf/mappings.tiny");
        require(Files.isRegularFile(mappings), "frozen mappings.tiny is unavailable for readiness");
        require(digest(Files.readString(mappings, StandardCharsets.UTF_8)).equals(
                required(descriptor, "mappings.tiny.sha256")),
                "readiness mappings.tiny hash drifted");
        verifySymbolRows(Files.readAllLines(mappings, StandardCharsets.UTF_8),
                Files.readAllLines(symbols, StandardCharsets.UTF_8));
    }

    private static void verifySymbolRows(List<String> mappings, List<String> symbols) {
        for (String row : symbols) {
            if (row.isEmpty() || row.startsWith("#")) {
                continue;
            }
            String[] columns = row.split("\\t", -1);
            require(columns.length == 6, "invalid symbols.map row: " + row);
            String owner = "c\t" + columns[0];
            int start = -1;
            for (int index = 0; index < mappings.size(); index++) {
                if (mappings.get(index).startsWith(owner + "\t")) {
                    start = index;
                    break;
                }
            }
            require(start >= 0, "mapped owner is absent: " + columns[0]);
            int end = start + 1;
            while (end < mappings.size() && !mappings.get(end).startsWith("c\t")) {
                end++;
            }
            String expected = columns[1].equals("c")
                    ? "c\t" + columns[3] + "\t" + columns[4] + "\t" + columns[5]
                    : "\t" + columns[1] + "\t" + columns[2] + "\t" + columns[3]
                            + "\t" + columns[4] + "\t" + columns[5];
            require(mappings.subList(start, end).contains(expected),
                    "mapped symbol is absent from owner " + columns[0] + ": " + expected);
        }
    }

    private static void verifyDataDrivenTrace(Path milestone, Properties descriptor)
            throws Exception {
        String trace = required(descriptor, "expected.trace");
        verifyTrace(descriptor, trace);
        require(Files.readString(milestone.resolve("MAP.md"), StandardCharsets.UTF_8)
                .contains(trace), "semantic map missing exact expected.trace");
    }

    private static void verifyTrace(Properties descriptor, String trace) throws Exception {
        require(digest(trace).equals(required(descriptor, "expected.signature")),
                "expected.trace does not produce expected.signature");
    }

    private static void verifyFlowingWaterFixture(Path milestone) throws Exception {
        StringBuilder source = new StringBuilder();
        for (Path file : SafeTreeDelete.paths(milestone.resolve("src")).stream()
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java")).toList()) {
            source.append(Files.readString(file, StandardCharsets.UTF_8)).append('\n');
        }
        verifyFlowSource(source.toString());
    }

    private static void verifyFlowSource(String source) {
        require(!NOTIFYING_FLOWING_WATER.matcher(source).find(),
                "flowing-water fixture notifies neighbors before its initial-state proof");
    }

    private static String digest(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key + " for Candidate readiness");
        return value.trim();
    }

    private static void rejects(Checked action, String message) throws Exception {
        try {
            action.run();
            throw new IllegalStateException(message);
        } catch (IllegalStateException expected) {
            require(!expected.getMessage().equals(message), message);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    private interface Checked {
        void run() throws Exception;
    }
}
