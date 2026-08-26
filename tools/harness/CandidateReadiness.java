import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Commit-independent proof that applicable NYA lessons were checked before Candidate Gate. */
final class CandidateReadiness {
    private static final String REQUIRED = "NYA-01M0VSCA8F3WSMVW32R9XME7DQ";
    private static final String SOURCE = "NYA-01M0X81N6TG6TQ4RM02X6PH7R7";
    private static final String LANES = "NYA-01M0XRE7GSKH7ARKM73DVCGQ7K";
    private static final String CLOSURE = "NYA-01M0XWB16KZB3JRYDGAAYF5SVB";
    private static final String COMPILE = "NYA-01M0YH9M17ETMZA0F5X7981K4P";
    private static final String API_RELEASE = "NYA-01M0YRVA4DD24Y22AHJQP2X3MF";
    private static final String TOKENS = "NYA-01M0XFV9TPVDKFE6RARDHC84T2";
    private static final String SYMBOLS = "NYA-01M0XM730NWRQDKFZ1VMP3732W";
    private static final String MAP_SIGNAL = "NYA-01M0YSJXNA3TK6FHQW4QJ5RJZ5";
    private static final String TRAVERSAL = "NYA-01M0XYP7T1RKYFD3SJHC4DMHZ3";
    private static final String MINECART_BARRIER = "NYA-01M0YCEZH1G2SKW1DVB1D4K3SB";
    private static final String MINECART_REACH = "NYA-01M0YDKWFZ4H1CCXE2TXCJC31G";
    private static final String MINECART_ATTACK = "NYA-01M0YM0FGRMPQ4DABMTVS4MNAF";
    private static final String MINECART_INITIATION = "NYA-01M0YMWRZX8V20G1SN0DYGB0MD";
    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+([A-Za-z0-9_$.]+);\\s*$");
    private static final Pattern PACKAGE = Pattern.compile("(?m)^\\s*package\\s+([A-Za-z0-9_$.]+);\\s*$");
    private static final Pattern INLINE_CALLABLE = Pattern.compile(
            "^\\s*(?:(?:public|protected|private|static|final|synchronized)\\s+)*"
                    + "(?:[A-Za-z_$][\\w$<>\\[\\].?,]*\\s+)?[A-Za-z_$][\\w$]*\\s*"
                    + "\\([^;{}]*\\)\\s*(?:throws\\s+[^{}]+)?\\{\\s*[^{}]*\\}\\s*$");
    private static final Pattern REMOTE_STONE = Pattern.compile(
            "B173FixtureSupport[.]place\\([^;]*,1\\);");

    private CandidateReadiness() {
    }

    static void selfTest() throws Exception {
        require(packedExecutable("    if (!value) throw new IllegalStateException();"),
                "packed control body was not recognized");
        require(packedExecutable("    private Arm(boolean axisZ) { this.axisZ = axisZ; }"),
                "packed constructor body was not recognized");
        require(packedExecutable("    private Smoke() { }"),
                "packed empty constructor was not recognized");
        require(packedExecutable("    static void run() { execute(); }"),
                "packed method body was not recognized");
        require(!packedExecutable("    if (!value) {"),
                "braced control body was rejected");
        require(!packedExecutable("    if (ready(value)) {"),
                "nested braced control body was rejected");
        require(packedExecutable("    if (ready(value)) throw new IllegalStateException();"),
                "nested packed control body was not recognized");
        require(!packedExecutable("    int[] values = {1, 2};"),
                "array initializer was rejected as executable packing");
        require(hasRemoteStonePlacement(
                "B173FixtureSupport.place(actor, pad, cross, 1);"),
                "remote stone support was not recognized");
        require(hasMinecartPacketAttack("B173MinecartBooster.push(actor, mover);"),
                "minecart Packet7 attack was not recognized");
        Properties collision = new Properties();
        collision.setProperty("behavior", "minecart-collision-transfer");
        require(minecartCollision("m1-renamed-contract", collision),
                "minecart collision behavior alias was not recognized");
        require(IMPORT.matcher("import worldline.smoke.privatecycle.Helper;").find(),
                "private import was not recognized");
        CandidatePolicyReadiness.selfTest();
    }

    static void prepare(String id) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        String branch = git(root, "branch", "--show-current").trim();
        require(branch.equals("codex/milestone-" + id), "readiness requires the exact worker branch");
        Map<String, Object> preflight = preflight(root, id);
        String base = MiniJson.string(preflight, "base");
        require(MiniJson.string(preflight, "head").equals(base), "preflight was not frozen at base");
        require(MiniJson.string(preflight, "status").equals("PASS"), "preflight did not pass");
        require(gitStatus(root, "merge-base", "--is-ancestor", base, "HEAD") == 0,
                "authorized base is not an ancestor");
        List<String> scars = applicable(root, id, base);
        Path recall = root.resolve(".worldline/reports/swarm/readiness-recall-" + id + ".log");
        require(Files.isRegularFile(recall), "missing readiness recall proof");
        String recalled = Files.readString(recall, StandardCharsets.UTF_8);
        for (String scar : scars) {
            require(recalled.contains(scar), "applicable scar absent: " + scar);
        }
        objective(root, id);
        CandidateSourceClosure.compile(id);
        String manifest = manifest(root, base);
        Path report = root.resolve(".worldline/reports/swarm/readiness-" + id + ".json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, "{\n  \"schema\":1,\n  \"id\":\"" + id
                + "\",\n  \"base\":\"" + base + "\",\n  \"manifest_sha256\":\"" + manifest
                + "\",\n  \"recall_sha256\":\"" + digest(recall) + "\",\n  \"scars\":["
                + scars.stream().map(value -> "\"" + value + "\"").collect(
                        java.util.stream.Collectors.joining(","))
                + "],\n  \"status\":\"PASS\"\n}\n", StandardCharsets.UTF_8);
        System.out.println("candidate readiness PASS: " + id + " manifest=" + manifest);
    }

    static void requireIfSupervised(String id) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        String branch = git(root, "branch", "--show-current").trim();
        if (!branch.equals("codex/milestone-" + id)) {
            return;
        }
        Path report = root.resolve(".worldline/reports/swarm/readiness-" + id + ".json");
        require(Files.isRegularFile(report), "missing mandatory pre-Candidate readiness report");
        Map<String, Object> values = MiniJson.object(Files.readString(report, StandardCharsets.UTF_8));
        require(MiniJson.string(values, "id").equals(id)
                && MiniJson.string(values, "status").equals("PASS"), "readiness report drifted");
        String base = MiniJson.string(values, "base");
        require(MiniJson.string(values, "manifest_sha256").equals(manifest(root, base)),
                "candidate changed after the readiness interlock");
        objective(root, id);
        System.out.println("  recursive readiness interlock: PASS");
    }

    static List<String> applicable(Path root, String id, String base) throws Exception {
        Properties descriptor = StrictProperties.load(root.resolve("smokes").resolve(id)
                .resolve("smoke.properties"));
        List<String> result = new ArrayList<>(List.of(REQUIRED, SOURCE));
        if (gitStatus(root, "cat-file", "-e", base + ":smokes/" + id + "/smoke.properties") != 0) {
            result.add(LANES);
        }
        String runner = descriptor.getProperty("runner.source", "");
        if (runner.endsWith("DataDrivenCycle.java") || runner.endsWith("CompositeCycle.java")) {
            result.add(CLOSURE);
        }
        if (Files.isDirectory(root.resolve("smokes").resolve(id).resolve("src"))) {
            result.add(COMPILE);
        }
        if (git(root, "diff", "--name-only", base).lines()
                .anyMatch(path -> path.startsWith("modules/api/src/main/java/"))) {
            result.add(API_RELEASE);
        }
        if (descriptor.containsKey("testkit.fixture")) {
            result.add(TOKENS);
        }
        if (Files.isRegularFile(root.resolve("smokes").resolve(id).resolve("symbols.map")))
            result.add(SYMBOLS);
        result.addAll(CandidatePolicyReadiness.applicable(root, id, descriptor));
        if (Files.isRegularFile(root.resolve("smokes").resolve(id).resolve("MAP.md"))) {
            result.add(MAP_SIGNAL);
        }
        if (git(root, "diff", "--name-only", base).lines().anyMatch(path ->
                path.startsWith("tools/harness/") || path.startsWith("tools/integration/")))
            result.add(TRAVERSAL);
        if (minecartCollision(id, descriptor)) {
            result.add(MINECART_BARRIER);
            result.add(MINECART_REACH);
            result.add(MINECART_ATTACK);
            result.add(MINECART_INITIATION);
        }
        return List.copyOf(result);
    }

    private static void objective(Path root, String id) throws Exception {
        Path milestone = root.resolve("smokes").resolve(id);
        Properties descriptor = StrictProperties.load(milestone.resolve("smoke.properties"));
        require(!descriptor.containsKey("scaffold.status"), "scaffold is not Candidate-ready");
        CandidatePolicyReadiness.verify(root, id, descriptor);
        MilestoneNarrative.validate(root, descriptor);
        SmokeLane.validate(root);
        String signal = descriptor.getProperty("expected.signal");
        require(signal != null && !signal.isBlank(), "missing expected semantic signal");
        require(Files.readString(milestone.resolve("MAP.md"), StandardCharsets.UTF_8)
                .contains(signal), "semantic map missing exact expected signal");
        List<Path> sources = javaFiles(milestone.resolve("src"));
        for (Path source : sources) {
            for (String line : Files.readAllLines(source, StandardCharsets.UTF_8)) {
                require(line.length() <= 120, "long smoke line: " + root.relativize(source));
                require(!packedExecutable(line),
                        "packed executable body repeats source scar: " + root.relativize(source));
            }
        }
        if (minecartCollision(id, descriptor)) {
            for (Path source : sources) {
                String text = Files.readString(source, StandardCharsets.UTF_8);
                require(!hasRemoteStonePlacement(text),
                        "remote stone support repeats minecart fixture scars: "
                                + root.relativize(source));
                require(!hasMinecartPacketAttack(text),
                        "Packet7 booster attack cannot initialize same-rail collision: "
                                + root.relativize(source));
            }
        }
        Map<String, Path> smokeTypes = smokeTypes(root.resolve("smokes"));
        for (Path source : sources) {
            Matcher imports = IMPORT.matcher(Files.readString(source, StandardCharsets.UTF_8));
            while (imports.find()) {
                Path owner = smokeTypes.get(imports.group(1));
                if (owner == null) {
                    continue;
                }
                Path relative = root.relativize(owner);
                require(relative.startsWith(Path.of("smokes", id))
                                || relative.startsWith(Path.of("smokes", "shared")),
                        "milestone-private smoke import is outside source closure: " + imports.group(1));
            }
        }
    }

    private static Map<String, Path> smokeTypes(Path root) throws Exception {
        Map<String, Path> result = new LinkedHashMap<>();
        for (Path source : javaFiles(root)) {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            Matcher owner = PACKAGE.matcher(text);
            if (!owner.find()) {
                continue;
            }
            String name = source.getFileName().toString().replaceFirst("[.]java$", "");
            result.put(owner.group(1) + "." + name, source);
        }
        return result;
    }

    private static List<Path> javaFiles(Path root) throws Exception {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        return SafeTreeDelete.paths(root).stream().filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java")).sorted().toList();
    }

    private static boolean packedExecutable(String line) {
        return packedControl(line) || INLINE_CALLABLE.matcher(line).matches();
    }

    private static boolean packedControl(String line) {
        String text = line.stripLeading();
        int open = text.indexOf('(');
        if (open < 0 || !List.of("if", "for", "while").contains(
                text.substring(0, open).stripTrailing())) {
            return false;
        }
        int depth = 0;
        for (int index = open; index < text.length(); index++) {
            if (text.charAt(index) == '(') {
                depth++;
            } else if (text.charAt(index) == ')' && --depth == 0) {
                String body = text.substring(index + 1).stripLeading();
                return !body.isEmpty() && !body.startsWith("{");
            }
        }
        return false;
    }

    private static boolean hasRemoteStonePlacement(String source) {
        return REMOTE_STONE.matcher(source.replaceAll("\\s+", "")).find();
    }

    private static boolean hasMinecartPacketAttack(String source) {
        return source.replaceAll("\\s+", "").contains("B173MinecartBooster.push(");
    }

    private static boolean minecartCollision(String id, Properties descriptor) {
        return id.contains("minecart-collision")
                || descriptor.getProperty("behavior", "").contains("minecart-collision");
    }

    private static Map<String, Object> preflight(Path root, String id) throws Exception {
        Path path = root.resolve(".worldline/reports/swarm/preflight-" + id + ".json");
        require(Files.isRegularFile(path), "missing mandatory supervised preflight report");
        Map<String, Object> value = MiniJson.object(Files.readString(path, StandardCharsets.UTF_8));
        require(MiniJson.string(value, "id").equals(id), "preflight milestone drifted");
        return value;
    }

    private static String manifest(Path root, String base) throws Exception {
        List<String> paths = new ArrayList<>(git(root, "diff", "--name-only", base).lines().toList());
        paths.addAll(git(root, "ls-files", "--others", "--exclude-standard").lines().toList());
        paths = paths.stream().filter(path -> !path.isBlank() && !path.startsWith(".worldline/"))
                .distinct().sorted().toList();
        StringBuilder value = new StringBuilder();
        for (String item : paths) {
            Path path = root.resolve(item);
            value.append(item).append('\0');
            value.append(Files.isRegularFile(path) ? digest(path) : "DELETED").append('\n');
        }
        return digest(value.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = process.waitFor();
        require(exit == 0, "git failed: " + output);
        return output;
    }
    private static int gitStatus(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        process.waitFor();
        return process.exitValue();
    }

    private static String digest(Path path) throws Exception {
        return digest(Files.readAllBytes(path));
    }

    private static String digest(byte[] value) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value));
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
