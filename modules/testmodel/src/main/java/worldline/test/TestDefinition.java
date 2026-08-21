package worldline.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collected test definition with Vitest-style explicit modifiers. */
public final class TestDefinition implements TestNode {
    private final String name;
    private final TestBody body;
    private final TestLocation location;
    private final List<String> tags = new ArrayList<>();
    private boolean skip, todo, only, concurrent, frozen;
    private int retries;
    private long timeoutMillis = 5_000L;

    TestDefinition(String name, TestBody body, TestLocation location) {
        this.name = name; this.body = body; this.location = location;
    }
    @Override public String name() { return name; }
    public TestDefinition skip() { mutable(); skip = true; return this; }
    public TestDefinition todo() { mutable(); todo = true; return this; }
    public TestDefinition only() { mutable(); only = true; return this; }
    public TestDefinition concurrent() { mutable(); concurrent = true; return this; }
    public TestDefinition retry(int count) {
        mutable(); require(count >= 0 && count <= 10, "retry must be between 0 and 10");
        retries = count; return this;
    }
    public TestDefinition timeout(long millis) {
        mutable(); require(millis >= 1 && millis <= 3_600_000L, "invalid timeout");
        timeoutMillis = millis; return this;
    }
    public TestDefinition tag(String value) {
        mutable(); require(value != null && value.matches("[A-Za-z0-9._-]{1,64}"), "invalid tag");
        if (!tags.contains(value)) tags.add(value); return this;
    }
    void freeze() { frozen = true; }
    public TestBody body() { return body; }
    public TestLocation location() { return location; }
    public List<String> tags() { return Collections.unmodifiableList(tags); }
    public boolean skipped() { return skip; }
    public boolean todoMode() { return todo; }
    public boolean onlyMode() { return only; }
    public boolean concurrentMode() { return concurrent; }
    public int retries() { return retries; }
    public long timeoutMillis() { return timeoutMillis; }
    private void mutable() { if (frozen) throw new IllegalStateException("test plan is frozen"); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
