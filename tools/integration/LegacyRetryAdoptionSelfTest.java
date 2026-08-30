import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Positive and negative fixtures for legacy retry adoption. */
final class LegacyRetryAdoptionSelfTest {
    private LegacyRetryAdoptionSelfTest() {
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length == 2) {
            LegacyRetryAdoption.verifyArchiveWorktree(Path.of(arguments[0]), Path.of(arguments[1]));
            System.out.println("legacy archive worktree identity self-test passed");
            return;
        }
        require(arguments.length == 0, "expected zero arguments or archive and worktree paths");
        String windowsPath = "C:\\Users\\lucas\\Documents\\ChatGPT\\worldline";
        require(LegacyRetryAdoption.rawManifest("schema=1\nworktree=" + windowsPath + "\n")
                .get("worktree").equals(windowsPath),
                "archive manifest parser altered a literal Windows path");
        expectFailure(() -> LegacyRetryAdoption.rawManifest("schema=1\nschema=2\n"),
                "archive manifest parser accepted a duplicate key");
        Path parent = Files.createTempDirectory("worldline-legacy-adoption-");
        Path root = parent.resolve("control");
        try {
            Files.createDirectories(root);
            git(root, "init", "--quiet");
            git(root, "config", "user.email", "worldline@example.invalid");
            git(root, "config", "user.name", "Worldline Test");
            Files.writeString(root.resolve("README.md"), "base\n", StandardCharsets.UTF_8);
            Files.writeString(root.resolve(".gitignore"), ".worldline/\n", StandardCharsets.UTF_8);
            git(root, "add", ".");
            git(root, "commit", "--quiet", "-m", "base");
            String base = git(root, "rev-parse", "HEAD").trim();
            historicalMigration(parent, base);
            Fixture resume = fixture(parent, root, base, "m1-contract");
            String session = "ses_exact_one";
            OpenCodeSessionExport.Result exported = OpenCodeSessionExport.capture(resume.worktree,
                    session, OpenCodeSessionExportSelfTest.childPrefix("valid", resume.worktree));
            Path sessionLog = Path.of(exported.path());
            LegacyRetryAdoption.Request resumeRequest = new LegacyRetryAdoption.Request(resume.id,
                    "codex:test", base, "resume-session", session, sessionLog,
                    exported.sha(), "", 0);
            Path receipt = LegacyRetryAdoption.adopt(root, resumeRequest);
            String receiptText = Files.readString(receipt, StandardCharsets.UTF_8);
            require(receiptText.contains("\"authorized_attempt\":2")
                    && receiptText.contains("\"session\":\"" + session + "\"")
                    && receiptText.contains("\"recovery_sessions_allowed\":0"),
                    "resume-session receipt was not sealed");
            OxAlphaLegacyAdoption.validate(resume.worktree, new OxAlphaRequest(resume.id,
                    "goal", base, base, "checkpoint", 2, session, 900,
                    receipt.toString(), LegacyRetryAdoption.sha(receipt)));

            Fixture recovery = fixture(parent, root, base, "m2-recovery");
            Path recoveryDisposition = root.resolve("coordination/swarm/dispositions/"
                    + recovery.id + ".properties");
            Files.writeString(recoveryDisposition,
                    Files.readString(recoveryDisposition, StandardCharsets.UTF_8).replace(
                            "next.action=resume-same-session-and-worktree",
                            "next.action=adopt-legacy-retry;repair-process;"
                                    + "resume-same-milestone-and-worktree"),
                    StandardCharsets.UTF_8);
            LegacyRetryAdoption.Request recoveryRequest = new LegacyRetryAdoption.Request(recovery.id,
                    "codex:test", base, "process-recovery", null, null, "",
                    "No historical OpenCode session exists in the preserved evidence", 900);
            Path recoveryReceipt = LegacyRetryAdoption.adopt(root, recoveryRequest);
            String recoveryText = Files.readString(recoveryReceipt, StandardCharsets.UTF_8);
            require(recoveryText.contains("\"session\":null")
                    && recoveryText.contains("\"recovery_sessions_allowed\":1")
                    && recoveryText.contains("\"recovery_timeout_seconds\":900"),
                    "process-recovery receipt was not bounded");
            OxAlphaControlMigration.Request migration = new OxAlphaControlMigration.Request(
                    recovery.id, "goal", base, "", "", base, null, 1, recovery.archive,
                    LegacyRetryAdoption.sha(recovery.archive), recoveryReceipt,
                    LegacyRetryAdoption.sha(recoveryReceipt));
            OxAlphaControlMigration.validateArchive(migration);
            OxAlphaControlMigration.validateAdoption(recovery.worktree, migration);
            Files.writeString(root.resolve("control-generation.txt"), "next\n",
                    StandardCharsets.UTF_8);
            git(root, "add", "control-generation.txt");
            git(root, "commit", "--quiet", "-m", "next control");
            String nextControl = git(root, "rev-parse", "HEAD").trim();
            OxAlphaControlMigration.Request remigration = new OxAlphaControlMigration.Request(
                    recovery.id, "goal", base, "", "", nextControl, null, 1,
                    recovery.archive, LegacyRetryAdoption.sha(recovery.archive), recoveryReceipt,
                    LegacyRetryAdoption.sha(recoveryReceipt));
            OxAlphaControlMigration.validateAdoption(recovery.worktree, remigration);
            OxAlphaLegacyAdoption.validate(recovery.worktree, new OxAlphaRequest(recovery.id,
                    "goal", nextControl, nextControl, "checkpoint", 2, null, 900,
                    recoveryReceipt.toString(), LegacyRetryAdoption.sha(recoveryReceipt)));
            String unrelated = git(root, "commit-tree", base + "^{tree}", "-m",
                    "unrelated control").trim();
            expectFailure(() -> OxAlphaControlMigration.validateAdoption(recovery.worktree,
                    new OxAlphaControlMigration.Request(recovery.id, "goal", base, "", "",
                            unrelated, null, 1, recovery.archive,
                            LegacyRetryAdoption.sha(recovery.archive), recoveryReceipt,
                            LegacyRetryAdoption.sha(recoveryReceipt))),
                    "adoption receipt authorized an unrelated control base");
            expectFailure(() -> OxAlphaLegacyAdoption.validate(recovery.worktree,
                    new OxAlphaRequest(recovery.id, "goal", unrelated, unrelated,
                            "checkpoint", 2, null, 900, recoveryReceipt.toString(),
                            LegacyRetryAdoption.sha(recoveryReceipt))),
                    "launcher adoption authorized an unrelated control base");
            OxAlphaControlMigration.Request parsedAdoption = OxAlphaControlMigration.Request.parse(
                    new String[] {"--id", recovery.id, "--goal", "goal", "--archive-base", base,
                            "--new-base", base, "--prior-attempt", "1", "--archive",
                            recovery.archive.toString(), "--archive-sha256",
                            LegacyRetryAdoption.sha(recovery.archive), "--adoption-receipt",
                            recoveryReceipt.toString(), "--adoption-sha256",
                            LegacyRetryAdoption.sha(recoveryReceipt)});
            require(parsedAdoption.session() == null && parsedAdoption.preflightBase().isBlank()
                    && parsedAdoption.receiptBase().isBlank(),
                    "legacy migration manufactured historical identities");
            OxAlphaLegacyAdoption.validate(recovery.worktree, new OxAlphaRequest(recovery.id,
                    "goal", base, base, "checkpoint", 2, null, 900,
                    recoveryReceipt.toString(), LegacyRetryAdoption.sha(recoveryReceipt)));
            String decoy = recoveryText.replace("\"id\":\"" + recovery.id + "\"",
                    "\"id\":\"m999-wrong\",\"decoy\":\"id=" + recovery.id
                            + ";authorized_attempt=2;status=PASS\"");
            Files.writeString(recoveryReceipt, decoy, StandardCharsets.UTF_8);
            String decoySha = LegacyRetryAdoption.sha(recoveryReceipt);
            expectFailure(() -> OxAlphaLegacyAdoption.validate(recovery.worktree,
                    new OxAlphaRequest(recovery.id, "goal", base, base, "checkpoint", 2,
                            null, 900, recoveryReceipt.toString(), decoySha)),
                    "launcher accepted adoption fields hidden in a decoy string");
            expectFailure(() -> OxAlphaControlMigration.validateAdoption(recovery.worktree,
                    new OxAlphaControlMigration.Request(recovery.id, "goal", base, "", "",
                            base, null, 1, recovery.archive,
                            LegacyRetryAdoption.sha(recovery.archive), recoveryReceipt, decoySha)),
                    "migration accepted adoption fields hidden in a decoy string");
            Files.writeString(recoveryReceipt, recoveryText + "{}", StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaAdoptionReceipt.read(recoveryReceipt),
                    "adoption receipt accepted trailing JSON data");
            Files.writeString(recoveryReceipt, recoveryText.replaceFirst("\\{",
                    "{\\\"id\\\":\\\"duplicate\\\","), StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaAdoptionReceipt.read(recoveryReceipt),
                    "adoption receipt accepted a duplicate key");
            Files.writeString(recoveryReceipt, recoveryText, StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaControlMigration.validateAdoption(recovery.worktree,
                    new OxAlphaControlMigration.Request(recovery.id, "goal", base, "", "",
                            base, null, 1, recovery.archive, LegacyRetryAdoption.sha(recovery.archive),
                            recoveryReceipt, "0".repeat(64))),
                    "migration accepted a drifted adoption receipt hash");
            expectFailure(() -> OxAlphaLegacyAdoption.validate(recovery.worktree,
                    new OxAlphaRequest(recovery.id, "goal", base, base, "checkpoint", 2,
                            null, 901, recoveryReceipt.toString(), "0".repeat(64))),
                    "launcher accepted a drifted adoption receipt hash");

            Fixture badSession = fixture(parent, root, base, "m3-bad-session");
            String badSessionId = "ses_bad_hash";
            OpenCodeSessionExport.Result badExport = OpenCodeSessionExport.capture(
                    badSession.worktree, badSessionId,
                    OpenCodeSessionExportSelfTest.childPrefix("valid", badSession.worktree));
            expectFailure(() -> LegacyRetryAdoption.adopt(root,
                    new LegacyRetryAdoption.Request(badSession.id, "codex:test", base,
                            "resume-session", badSessionId, Path.of(badExport.path()),
                            "0".repeat(64), "", 0)),
                    "session evidence hash mismatch was accepted");
            Fixture external = fixture(parent, root, base, "m7-external-evidence");
            expectFailure(() -> LegacyRetryAdoption.adopt(root,
                    new LegacyRetryAdoption.Request(external.id, "codex:test", base,
                            "resume-session", session, sessionLog, exported.sha(), "", 0)),
                    "noncanonical session evidence path was accepted");

            Fixture dirty = fixture(parent, root, base, "m4-status-drift");
            Files.writeString(dirty.worktree.resolve("untracked.txt"), "drift\n", StandardCharsets.UTF_8);
            expectFailure(() -> LegacyRetryAdoption.adopt(root,
                    new LegacyRetryAdoption.Request(dirty.id, "codex:test", base,
                            "process-recovery", null, null, "", "No session", 900)),
                    "worktree status drift was accepted");

            Fixture unbounded = fixture(parent, root, base, "m5-unbounded");
            expectFailure(() -> LegacyRetryAdoption.adopt(root,
                    new LegacyRetryAdoption.Request(unbounded.id, "codex:test", base,
                            "process-recovery", null, null, "", "No session", 3601)),
                    "unbounded process recovery was accepted");

            Fixture replacement = fixture(parent, root, base, "m6-replacement");
            Path replacementDisposition = root.resolve("coordination/swarm/dispositions/"
                    + replacement.id + ".properties");
            Files.writeString(replacementDisposition,
                    Files.readString(replacementDisposition, StandardCharsets.UTF_8).replace(
                            "next.action=resume-same-session-and-worktree",
                            "next.action=adopt-legacy-retry;replace-milestone;"
                                    + "resume-same-milestone-and-worktree"),
                    StandardCharsets.UTF_8);
            expectFailure(() -> LegacyRetryAdoption.adopt(root,
                    new LegacyRetryAdoption.Request(replacement.id, "codex:test", base,
                            "process-recovery", null, null, "", "No session", 900)),
                    "recovery action allowed a milestone-replacement step");
        } finally {
            SafeTreeDelete.delete(parent);
        }
    }

    private static void historicalMigration(Path parent, String base) throws Exception {
        String id = "m0-history";
        String session = "session-history";
        Path archive = parent.resolve("historical.zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            entry(zip, "manifest.properties", "id=" + id + "\nbase=" + base
                    + "\nstate=DIRTY_SUSPENDED\n");
            entry(zip, "evidence/opencode-" + id + "-qualify-attempt1.json",
                    "{\"id\":\"" + id + "\",\"base\":\"" + base
                            + "\",\"session\":\"" + session
                            + "\",\"phase\":\"qualify\",\"attempt\":1,\"completed\":true}");
        }
        OxAlphaControlMigration.Request valid = new OxAlphaControlMigration.Request(id, "goal",
                base, base, base, base, session, 1, archive, LegacyRetryAdoption.sha(archive),
                null, "");
        OxAlphaControlMigration.validateArchive(valid);
        expectFailure(() -> OxAlphaControlMigration.validateArchive(
                new OxAlphaControlMigration.Request(id, "goal", base, base, base, base, session,
                        1, archive, "0".repeat(64), null, "")),
                "historical archive hash mismatch was accepted");
        OxAlphaControlMigration.Request parsed = OxAlphaControlMigration.Request.parse(new String[] {
                "--id", id, "--goal", "goal", "--archive-base", base, "--preflight-base", base,
                "--receipt-base", base, "--new-base", base, "--session", session,
                "--prior-attempt", "1", "--archive", archive.toString(), "--archive-sha256",
                LegacyRetryAdoption.sha(archive)});
        require(parsed.preflightBase().equals(base) && parsed.receiptBase().equals(base),
                "historical migration bases were not parsed independently");
        String full = "== wtw ==\n== rtw ==\n== nya ==\n== nwc ==\nNYA-01M0VSCA8F3WSMVW32R9XME7DQ";
        require(OxAlphaControlMigration.contextAccepted(new OxAlphaControlMigration.Result(0,
                        full, "")), "complete CSM context was rejected");
        require(!OxAlphaControlMigration.contextAccepted(new OxAlphaControlMigration.Result(1,
                        full, "Why This Way is not initialized; run wtw init\n")),
                "uninitialized-store context escaped the mandatory bootstrap");
        require(!OxAlphaControlMigration.contextAccepted(new OxAlphaControlMigration.Result(0,
                        "== nya ==\n", "")), "partial context fan-out was accepted");
        require(!OxAlphaControlMigration.contextAccepted(new OxAlphaControlMigration.Result(1,
                        full, "unknown failure\n")), "unknown CSM context failure was accepted");
    }

    private static Fixture fixture(Path parent, Path root, String base, String id) throws Exception {
        String branch = "codex/milestone-" + id;
        git(root, "branch", branch, base);
        Path worktree = parent.resolve(id);
        git(root, "worktree", "add", "--quiet", worktree.toString(), branch);
        String head = git(worktree, "rev-parse", "HEAD").trim();
        String tree = git(worktree, "rev-parse", "HEAD^{tree}").trim();
        Path archive = parent.resolve(id + ".zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            entry(zip, "manifest.properties", "schema=1\nid=" + id + "\nstate=FAILED_GATE\nworktree="
                    + worktree + "\nbranch=" + branch + "\nbase=" + base + "\nhead=" + head
                    + "\ntree=" + tree + "\n");
            entry(zip, "status.txt", "");
            entry(zip, "working-tree.patch", "");
        }
        Path disposition = root.resolve("coordination/swarm/dispositions/" + id + ".properties");
        Files.createDirectories(disposition.getParent());
        Files.writeString(disposition, "schema=1\nid=" + id + "\ndisposition=RETRYABLE\n"
                + "prior.state=FAILED_GATE\nbranch=" + branch + "\nworktree=" + slash(worktree)
                + "\nbase=" + base + "\nhead=" + head + "\ntree=" + tree + "\ncause=test\n"
                + "scar=NYA-TEST\nattempt=1\nmax.attempts=2\narchive=" + slash(archive)
                + "\narchive.sha256=" + LegacyRetryAdoption.sha(archive)
                + "\nnext.action=resume-same-session-and-worktree\n", StandardCharsets.UTF_8);
        return new Fixture(id, worktree, archive);
    }

    private static void entry(ZipOutputStream zip, String name, String value) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(value.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments) + "\n" + output);
        return output;
    }

    private static void expectFailure(Throwing action, String message) throws Exception {
        boolean rejected = false;
        try {
            action.run();
        } catch (RuntimeException expected) {
            rejected = true;
        }
        require(rejected, message);
    }

    private static String slash(Path value) {
        return value.toString().replace('\\', '/');
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record Fixture(String id, Path worktree, Path archive) { }
    @FunctionalInterface private interface Throwing { void run() throws Exception; }
}
