import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

/** Produces an evidence-bound census without modifying candidate worktrees. */
final class SwarmCensus {
    private static final Pattern ID = Pattern.compile("m([0-9]+)-[a-z0-9-]+");
    private static final Pattern JSON_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
    private SwarmCensus() { }

    static void audit(List<Wave> waves, Path outputValue, Path archiveValue, Path baselineValue)
            throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path output = outputValue.toAbsolutePath().normalize();
        Path archive = archiveValue == null ? null : archiveValue.toAbsolutePath().normalize();
        SwarmEvidenceArchive.Result bundle = archive == null ? SwarmEvidenceArchive.Result.empty()
                : SwarmEvidenceArchive.saveRepositoryBundle(root, archive);
        Map<String, Properties> handoffs = handoffs(root);
        Map<String, CensusDisposition.Decision> dispositions = CensusDisposition.load(root);
        CensusMetrics metrics = CensusMetrics.load(root, baselineValue);
        Set<String> integratedReceipts = integratedReceipts(root);
        List<Item> items = new ArrayList<>();
        for (Wave wave : waves) {
            inspectWave(root, wave, handoffs, dispositions, metrics, integratedReceipts,
                    archive, bundle, items);
        }
        items.sort(Comparator.comparingInt(Item::number));
        Map<String, Integer> counts = new HashMap<>();
        for (Item item : items) {
            counts.merge(item.state, 1, Integer::sum);
        }
        Files.createDirectories(output.getParent());
        Files.writeString(output, CensusJson.json(items, counts, waves, metrics.baselineSha()),
                StandardCharsets.UTF_8);
        System.out.println("swarm census: total=" + items.size() + ", qualified="
                + counts.getOrDefault("QUALIFIED", 0) + ", failed-gate="
                + counts.getOrDefault("FAILED_GATE", 0) + ", dirty-suspended="
                + counts.getOrDefault("DIRTY_SUSPENDED", 0) + ", not-started="
                + counts.getOrDefault("NOT_STARTED", 0) + ", rejected="
                + counts.getOrDefault("REJECTED", 0));
        System.out.println("  report: " + output);
        if (archive != null) System.out.println("  evidence archive: " + archive);
    }

    private static void inspectWave(Path root, Wave wave, Map<String, Properties> handoffs,
            Map<String, CensusDisposition.Decision> dispositions, CensusMetrics metrics,
            Set<String> integratedReceipts, Path archive, SwarmEvidenceArchive.Result bundle,
            List<Item> items) throws Exception {
        require(Files.isDirectory(wave.path), "missing wave root: " + wave.path);
        String base = git(root, "rev-parse", "--verify", wave.base + "^{commit}").trim();
        try (var paths = Files.list(wave.path)) {
            for (Path path : paths.filter(Files::isDirectory).sorted().toList()) {
                Matcher match = ID.matcher(path.getFileName().toString());
                if (!match.matches()) continue;
                items.add(inspect(root, path.toAbsolutePath().normalize(), base, handoffs,
                        dispositions, metrics, integratedReceipts, archive, bundle,
                        Integer.parseInt(match.group(1))));
            }
        }
    }

    private static Item inspect(Path root, Path path, String base, Map<String, Properties> handoffs,
            Map<String, CensusDisposition.Decision> dispositions, CensusMetrics metrics,
            Set<String> integratedReceipts, Path archive, SwarmEvidenceArchive.Result bundle,
            int number) throws Exception {
        String id = path.getFileName().toString();
        String branch = git(path, "branch", "--show-current").trim();
        String head = git(path, "rev-parse", "HEAD").trim();
        String tree = git(path, "rev-parse", "HEAD^{tree}").trim();
        String status = git(path, "status", "--porcelain=v1", "--untracked-files=all");
        boolean dirty = !status.isBlank(), atBase = head.equals(base);
        Path receipt = path.resolve(".worldline/reports/milestones/" + id + ".json");
        Receipt proof = receipt(receipt, id, head, tree);
        boolean receiptBase = proof.exact && SwarmProcess.status(path,
                List.of("merge-base", "--is-ancestor", proof.base, head), 60) == 0;
        String evidenceBase = receiptBase ? proof.base : base;
        boolean handoff = receiptBase && handoff(handoffs.get(head), branch, head,
                evidenceBase, receipt);
        Path descriptor = path.resolve("smokes").resolve(id).resolve("smoke.properties");
        boolean scaffold = Files.isRegularFile(descriptor)
                && Files.readString(descriptor, StandardCharsets.UTF_8).contains("scaffold.status=");
        boolean integrated = integratedReceipts.contains(head) || integrated(root, head);
        int commits = head.equals(evidenceBase) ? 0 : Integer.parseInt(git(path, "rev-list",
                "--count", evidenceBase + ".." + head).trim());
        boolean qualified = receiptBase && handoff && integrated && commits == 1 && !scaffold;
        String state = legacyState(dirty, qualified, atBase, !atBase);
        List<Path> logs = evidenceLogs(path, id);
        int retries = retries(logs);
        String cause = cause(state, status, logs);
        CensusDisposition.Decision disposition = dispositions.get(id);
        SwarmEvidenceArchive.Result saved;
        if (disposition != null) {
            disposition.requireExact(path, branch);
            state = disposition.state();
            cause = disposition.cause();
            evidenceBase = disposition.base();
            head = disposition.head();
            tree = disposition.tree();
            commits = Integer.parseInt(git(path, "rev-list", "--count",
                    evidenceBase + ".." + head).trim());
            if ("REJECTED".equals(disposition.state())) {
                integrated = false;
            }
            saved = disposition.archive();
        } else {
            saved = archive != null
                    && (state.equals("DIRTY_SUSPENDED") || state.equals("FAILED_GATE"))
                    ? SwarmEvidenceArchive.save(archive, id, path, branch, base, head, tree, state,
                            status, logs, receipt, bundle) : SwarmEvidenceArchive.Result.empty();
        }
        CensusMetrics.Entry metric = metrics.entry(id, head);
        return new Item(number, id, path, branch, evidenceBase, head, tree, state, dirty, commits,
                scaffold, proof.present, proof.exact, handoff, integrated, retries, cause, logs,
                saved, metric, disposition);
    }

    static String legacyState(boolean dirty, boolean qualified, boolean atBase, boolean hasCommit) {
        if (dirty) return "DIRTY_SUSPENDED";
        if (qualified) return "QUALIFIED";
        if (atBase && !hasCommit) return "NOT_STARTED";
        return "FAILED_GATE";
    }

    private static Receipt receipt(Path path, String id, String head, String tree)
            throws IOException {
        if (!Files.isRegularFile(path)) {
            return new Receipt(false, false, "", "");
        }
        Map<String, String> fields = new HashMap<>();
        Matcher matcher = JSON_FIELD.matcher(Files.readString(path, StandardCharsets.UTF_8));
        while (matcher.find()) fields.put(matcher.group(1), matcher.group(2));
        boolean exact = "passed".equals(fields.get("status")) && id.equals(fields.get("id"))
                && head.equals(fields.get("head")) && tree.equals(fields.get("tree"))
                && fields.getOrDefault("base", "").matches("[0-9a-f]{40}");
        return new Receipt(true, exact, fields.getOrDefault("qualified_at", ""),
                fields.getOrDefault("base", ""));
    }

    private static Map<String, Properties> handoffs(Path root) throws IOException {
        Map<String, Properties> result = new HashMap<>();
        Path directory = root.resolve("coordination/handoffs");
        if (!Files.isDirectory(directory)) return result;
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                Properties values = new Properties();
                try (var input = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    values.load(input);
                }
                result.put(values.getProperty("head", ""), values);
            }
        }
        return result;
    }

    private static Set<String> integratedReceipts(Path root) throws Exception {
        String text = git(root, "show", "main:smokes/train-reconciliation.lock");
        Properties values = new Properties(); values.load(new java.io.StringReader(text));
        Set<String> result = new HashSet<>();
        for (String key : values.stringPropertyNames())
            if (key.endsWith(".receipt.head")) result.add(values.getProperty(key));
        return Set.copyOf(result);
    }

    private static boolean handoff(Properties values, String branch, String head, String base,
            Path receipt) throws Exception {
        return values != null && branch.equals(values.getProperty("branch"))
                && head.equals(values.getProperty("head")) && base.equals(values.getProperty("base"))
                && "qualified".equals(values.getProperty("disposition"))
                && Files.isRegularFile(receipt)
                && SwarmEvidenceArchive.sha256(receipt).equals(values.getProperty("receipt.sha256"));
    }

    private static List<Path> evidenceLogs(Path path, String id) throws IOException {
        List<Path> result = new ArrayList<>();
        Path log = path.resolve(".worldline/smoke-logs").resolve(id + ".log");
        if (Files.isRegularFile(log)) result.add(log);
        Path report = path.resolve(".worldline/reports/milestones").resolve(id + ".json");
        if (Files.isRegularFile(report)) result.add(report);
        return List.copyOf(result);
    }

    private static int retries(List<Path> logs) throws IOException {
        Pattern pattern = Pattern.compile("WORLDLINE_FLAKE_TELEMETRY=.*retries=([0-9]+)");
        int result = 0;
        for (Path log : logs) if (log.toString().endsWith(".log")) {
            Matcher matcher = pattern.matcher(Files.readString(log, StandardCharsets.UTF_8));
            while (matcher.find()) result = Math.max(result, Integer.parseInt(matcher.group(1)));
        }
        return result;
    }

    private static boolean integrated(Path root, String head) throws Exception {
        String base = git(root, "rev-parse", "main^{commit}").trim();
        if (SwarmProcess.status(root, List.of("merge-base", "--is-ancestor", head, base), 60) == 0)
            return true;
        String cherry = git(root, "cherry", base, head).trim();
        return !cherry.isBlank() && cherry.lines().allMatch(line -> line.startsWith("- "));
    }

    private static String cause(String state, String status, List<Path> logs) throws IOException {
        if (state.equals("DIRTY_SUSPENDED")) return "worker exited with uncommitted changes: "
                + status.lines().findFirst().orElse("unknown change").trim();
        if (!state.equals("FAILED_GATE") || logs.isEmpty()) return "none";
        List<String> lines = Files.readAllLines(logs.get(0), StandardCharsets.UTF_8);
        return lines.stream().filter(line -> line.contains("failed:") || line.contains("Exception")
                || line.contains("timed out") || line.contains("artifact absent"))
                .findFirst().orElse(lines.isEmpty() ? "gate failed without log detail" : lines.get(0)).trim();
    }

    private static String git(Path directory, String... arguments) throws Exception {
        return SwarmProcess.output(directory, List.of(arguments), 120);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    record Wave(Path path, String base) {
        static Wave parse(String value) {
            int split = value.lastIndexOf('=');
            require(split > 0 && split < value.length() - 1, "wave must be PATH=BASE");
            return new Wave(Path.of(value.substring(0, split)).toAbsolutePath().normalize(),
                    value.substring(split + 1));
        }
    }
    private record Receipt(boolean present, boolean exact, String qualifiedAt, String base) { }
    record Item(int number, String id, Path path, String branch, String base, String head,
            String tree, String state, boolean dirty, int commits, boolean scaffold,
            boolean receiptPresent, boolean receiptExact, boolean handoffExact, boolean integrated,
            int retries, String cause, List<Path> logs, SwarmEvidenceArchive.Result archive,
            CensusMetrics.Entry metrics, CensusDisposition.Decision disposition) { }
}
