package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory persistence journal with deterministic one-shot failure injection. */
public final class B173VirtualFileSystem {
    private final List<String> operations = new ArrayList<>();
    private String failure;

    public synchronized void failNext(String operation) {
        validate(operation);
        failure = operation;
    }

    public synchronized List<String> operations() {
        return Collections.unmodifiableList(new ArrayList<>(operations));
    }

    public synchronized void clearJournal() { operations.clear(); }

    synchronized void record(String operation) {
        validate(operation);
        operations.add(operation);
        if (operation.equals(failure)) {
            failure = null;
            throw new IllegalStateException("injected filesystem failure: " + operation);
        }
    }

    private static void validate(String operation) {
        if (operation == null || operation.trim().isEmpty()) {
            throw new IllegalArgumentException("filesystem operation must not be empty");
        }
    }
}
