import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Recovers one exact OpenCode session from stdout, provider logs, or a resume request. */
final class OxAlphaProviderFailure {
    private static final Pattern SESSION_ID = Pattern.compile("ses_[A-Za-z0-9]+");

    private OxAlphaProviderFailure() {
    }

    static String resolveSession(Set<String> stdoutSessions, String requestedSession,
            Path stderr, String selectedModel) throws IOException {
        Set<String> observed = sessions(stderr, selectedModel);
        for (String stdoutSession : stdoutSessions) {
            require(validSession(stdoutSession), "OpenCode stdout session has invalid syntax");
            observed.add(stdoutSession);
        }
        require(stdoutSessions.size() <= 1, "OpenCode stdout contains multiple session identities");
        if (requestedSession != null) {
            require(validSession(requestedSession), "requested OpenCode session has invalid syntax");
            require(observed.isEmpty() || observed.equals(Set.of(requestedSession)),
                    "OpenCode log session drifted from the requested session");
            return requestedSession;
        }
        require(observed.size() <= 1, "OpenCode logs contain multiple session identities");
        return observed.stream().findFirst().orElse(null);
    }

    static boolean validSession(String session) {
        return session != null && SESSION_ID.matcher(session).matches();
    }

    static void selfTest() throws Exception {
        Path failure = Files.createTempFile("worldline-provider-failure-", ".log");
        Path valid = Files.createTempFile("worldline-provider-valid-", ".log");
        try {
            Files.writeString(failure, historicalFailure(), StandardCharsets.UTF_8);
            Files.writeString(valid, "level=WARN message=created id=ses_benign\n",
                    StandardCharsets.UTF_8);
            require("ses_supplied".equals(resolveSession(Set.of(), "ses_supplied", valid,
                    OxAlphaProfile.DEFAULT_MODEL)), "receipt did not preserve its supplied session");
            require("ses_fbe3d4e8affetPnLauYbYTqDbE".equals(
                    resolveSession(Set.of(), null, failure, OxAlphaProfile.DEFAULT_MODEL)),
                    "created session was not recovered");
            requireRejected(() -> resolveSession(Set.of(), "ses_other", failure,
                    OxAlphaProfile.DEFAULT_MODEL), "conflicting requested session was accepted");
            requireRejected(() -> resolveSession(Set.of("ses_stdout"), null, failure,
                    OxAlphaProfile.DEFAULT_MODEL), "conflicting stdout and provider-log sessions were accepted");
            requireRejected(() -> resolveSession(Set.of("ses_one", "ses_two"), null, valid,
                    OxAlphaProfile.DEFAULT_MODEL), "multiple stdout sessions were accepted");
            requireRejected(() -> resolveSession(Set.of(), "not-a-session", valid,
                    OxAlphaProfile.DEFAULT_MODEL), "invalid requested session syntax was accepted");
            Files.writeString(valid, "level=INFO message=created id=ses_one\n"
                    + "level=INFO message=created id=ses_two\n", StandardCharsets.UTF_8);
            requireRejected(() -> resolveSession(Set.of(), null, valid, OxAlphaProfile.DEFAULT_MODEL),
                    "multiple stderr sessions were accepted");
        } finally {
            Files.deleteIfExists(failure);
            Files.deleteIfExists(valid);
        }
    }

    private static String historicalFailure() {
        return "level=INFO message=created id=ses_fbe3d4e8affetPnLauYbYTqDbE "
                + "directory=worldline-wave-m667-m691/m686-cactus-height-cap\n"
                + "level=ERROR message=\"stream error\" providerID=opencode-go "
                + "modelID=glm-5.3-flash session.id=ses_fbe3d4e8affetPnLauYbYTqDbE "
                + "error.error=\"AI_APICallError: Monthly usage limit reached.\"\n";
    }

    private static Set<String> sessions(Path path, String selectedModel) throws IOException {
        Set<String> values = new LinkedHashSet<>();
        if (!Files.exists(path)) {
            return values;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Files.newInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String session = "INFO".equals(OxAlphaProviderLogMonitor.field(line, "level"))
                        && "created".equals(OxAlphaProviderLogMonitor.field(line, "message"))
                        ? OxAlphaProviderLogMonitor.field(line, "id")
                        : OxAlphaProviderLogMonitor.providerFailureLine(line, selectedModel)
                        ? OxAlphaProviderLogMonitor.field(line, "session.id") : null;
                if (session != null) {
                    require(validSession(session), "OpenCode stderr session has invalid syntax");
                    values.add(session);
                }
            }
        }
        return values;
    }

    private static void requireRejected(Checked action, String message) throws Exception {
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    private interface Checked {
        void run() throws Exception;
    }
}
