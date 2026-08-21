package worldline.testkit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.test.TestLocation;

/** Immutable result of one collected test. */
public final class TestResult {
    private final String id, spec, path, note, errorType, errorMessage, expected, received;
    private final String divergenceField, divergenceRole;
    private final TestStatus status;
    private final long durationMillis, seed, divergenceTick;
    private final int attempts;
    private final TestLocation location;
    private final List<String> tags;
    private final List<Path> artifacts;

    TestResult(Builder builder) {
        id = builder.id; spec = builder.spec; path = builder.path; status = builder.status;
        note = builder.note; errorType = builder.errorType; errorMessage = builder.errorMessage;
        expected = builder.expected; received = builder.received; durationMillis = builder.durationMillis;
        seed = builder.seed; attempts = builder.attempts; location = builder.location;
        divergenceTick = builder.divergenceTick; divergenceField = builder.divergenceField;
        divergenceRole = builder.divergenceRole;
        tags = Collections.unmodifiableList(new ArrayList<>(builder.tags));
        artifacts = Collections.unmodifiableList(new ArrayList<>(builder.artifacts));
    }

    public String id() { return id; }
    public String spec() { return spec; }
    public String path() { return path; }
    public TestStatus status() { return status; }
    public String note() { return note; }
    public String errorType() { return errorType; }
    public String errorMessage() { return errorMessage; }
    public String expected() { return expected; }
    public String received() { return received; }
    public long durationMillis() { return durationMillis; }
    public long seed() { return seed; }
    public int attempts() { return attempts; }
    public TestLocation location() { return location; }
    public long divergenceTick() { return divergenceTick; }
    public String divergenceField() { return divergenceField; }
    public String divergenceRole() { return divergenceRole; }
    public List<String> tags() { return tags; }
    public List<Path> artifacts() { return artifacts; }
    public boolean passed() { return status == TestStatus.PASSED; }

    static Builder builder(String id, String spec, String path, TestLocation location) {
        return new Builder(id, spec, path, location);
    }
    static final class Builder {
        final String id, spec, path; final TestLocation location;
        TestStatus status = TestStatus.QUEUED;
        String note, errorType, errorMessage, expected, received, divergenceField, divergenceRole;
        long durationMillis, seed, divergenceTick = -1; int attempts;
        final List<String> tags = new ArrayList<>();
        final List<Path> artifacts = new ArrayList<>();
        Builder(String id, String spec, String path, TestLocation location) {
            this.id = id; this.spec = spec; this.path = path; this.location = location;
        }
        TestResult build() { return new TestResult(this); }
    }
}
