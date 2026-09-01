import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Seals one adaptive set of parallel Candidates behind a completed learning barrier. */
final class SwarmMicroWave {
    private SwarmMicroWave() { }

    static void plan(Path closureValue, Path censusValue, Path outputValue, String baseValue,
            List<String> ids) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path closure = closureValue.toAbsolutePath().normalize();
        String closureText = text(closure), base = commit(root, baseValue);
        String head = git(root, "rev-parse", "HEAD").trim();
        String tree = git(root, "show", "-s", "--format=%T", "HEAD").trim();
        require(head.equals(base) && head.equals(string(closureText, "head"))
                && tree.equals(string(closureText, "tree"))
                && base.equals(string(closureText, "base")), "micro-wave closure identity drifted");
        require(bool(closureText, "next_candidate_allowed"), "learning barrier blocks Candidates");
        int width = integer(closureText, "recommended_candidate_parallelism");
        require(!ids.isEmpty() && ids.size() <= width && new HashSet<>(ids).size() == ids.size(),
                "micro-wave exceeds adaptive Candidate width " + width);
        WaveCensus.Snapshot census = WaveCensus.read(censusValue.toAbsolutePath().normalize());
        Set<String> reservedClaims = new HashSet<>();
        for (String id : ids) {
            WaveCensus.Row row = census.rows().stream().filter(value -> value.id().equals(id))
                    .findFirst().orElseThrow(() -> new IllegalStateException("candidate absent: " + id));
            require("NOT_STARTED".equals(row.state()), "candidate is not pristine: " + id);
            MilestoneObjective objective = MilestoneObjective.load(root, id);
            for (String claim : objective.claims()) {
                require(reservedClaims.add(claim),
                        "functional census claim overlaps parallel objectives: " + claim);
            }
            RejectedContractCheck.requireAllowed(root, id, objective.outcome());
        }
        String closureSha = SwarmEvidenceArchive.sha256(closure);
        Path canonical = canonicalReceipt(closure, closureSha);
        Path output = outputValue == null ? canonical : outputValue.toAbsolutePath().normalize();
        require(output.equals(canonical), "micro-wave receipt must use canonical closure path: "
                + canonical);
        require(!Files.exists(output), "immutable micro-wave receipt already exists: " + output);
        Files.createDirectories(output.getParent());
        StringBuilder json = new StringBuilder("{\n  \"schema\":1,\n  \"created\":\"")
                .append(Instant.now()).append("\",\n  \"base\":\"").append(base)
                .append("\",\n  \"head\":\"").append(head).append("\",\n  \"tree\":\"")
                .append(tree).append("\",\n  \"closure_sha256\":\"")
                .append(closureSha).append("\",\n  \"census_sha256\":\"")
                .append(SwarmEvidenceArchive.sha256(census.path())).append("\",\n  \"width\":")
                .append(ids.size()).append(",\n  \"limit\":").append(width)
                .append(",\n  \"official_runtime_parallelism\":1,\n  \"candidates\":[");
        for (int index = 0; index < ids.size(); index++) {
            if (index > 0) json.append(',');
            json.append('\"').append(ids.get(index)).append('\"');
        }
        Files.writeString(output, json.append("],\n  \"status\":\"OPEN\"\n}\n").toString(),
                StandardCharsets.UTF_8);
        System.out.println("supervised micro-wave open: candidates=" + ids.size() + "/" + width);
        System.out.println("  receipt: " + output);
    }

    static void verify(Path root, Path receiptValue, Path closureValue, String id, String base)
            throws Exception {
        Path receipt = receiptValue.toAbsolutePath().normalize();
        Path closure = closureValue.toAbsolutePath().normalize();
        String value = text(receipt), closureText = text(closure);
        String closureSha = SwarmEvidenceArchive.sha256(closure);
        Path canonical = canonicalReceipt(closure, closureSha);
        require(receipt.equals(canonical), "worker receipt is not the canonical micro-wave lease");
        String head = git(root, "rev-parse", "HEAD").trim();
        String tree = git(root, "show", "-s", "--format=%T", "HEAD").trim();
        require("OPEN".equals(string(value, "status")) && head.equals(base)
                && head.equals(string(value, "head")) && tree.equals(string(value, "tree"))
                && base.equals(string(value, "base")), "micro-wave receipt identity drifted");
        require(closureSha.equals(string(value, "closure_sha256"))
                && bool(closureText, "next_candidate_allowed"), "learning barrier receipt drifted");
        require(strings(value, "candidates").contains(id), "worker is outside supervised micro-wave");
        MilestoneObjective.load(root, id);
        require(integer(value, "width") <= integer(value, "limit"), "micro-wave width exceeded");
    }

    static void selfTest() {
        String valid = "{\"head\":\"" + "a".repeat(40) + "\",\"next_candidate_allowed\":true,"
                + "\"recommended_candidate_parallelism\":4,\"candidates\":[\"m1-one\"]}";
        require(bool(valid, "next_candidate_allowed")
                && integer(valid, "recommended_candidate_parallelism") == 4
                && strings(valid, "candidates").contains("m1-one"), "micro-wave parser drifted");
        require(canonicalReceipt(Path.of("closure.json").toAbsolutePath(), "a".repeat(64))
                .getFileName().toString().equals("micro-wave-" + "a".repeat(64) + ".json"),
                "canonical micro-wave path drifted");
    }

    private static Path canonicalReceipt(Path closure, String closureSha) {
        return closure.getParent().resolve("micro-wave-" + closureSha + ".json").normalize();
    }

    private static String text(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing micro-wave input: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
    private static String commit(Path root, String value) throws Exception {
        require(value.matches("[0-9a-f]{40}"), "base must be an exact SHA");
        return git(root, "rev-parse", "--verify", value + "^{commit}").trim();
    }
    private static String string(String text, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*\"([^\"]*)\"").matcher(text);
        require(matcher.find(), "missing micro-wave field: " + name); return matcher.group(1);
    }
    private static int integer(String text, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*([0-9]+)").matcher(text);
        require(matcher.find(), "missing micro-wave field: " + name);
        return Integer.parseInt(matcher.group(1));
    }
    private static boolean bool(String text, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*(true|false)").matcher(text);
        require(matcher.find(), "missing micro-wave field: " + name);
        return Boolean.parseBoolean(matcher.group(1));
    }
    private static Set<String> strings(String text, String name) {
        Matcher array = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*\\[([^]]*)]", Pattern.DOTALL).matcher(text);
        require(array.find(), "missing micro-wave array: " + name);
        Set<String> result = new HashSet<>(); Matcher item = Pattern.compile("\"([^\"]+)\"")
                .matcher(array.group(1)); while (item.find()) result.add(item.group(1));
        return Set.copyOf(result);
    }
    private static String git(Path root, String... arguments) throws Exception {
        return SwarmProcess.output(root, List.of(arguments), 120);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
