package worldline.analysis;

/** Neutral contract for exporting the semantic UI tree as a page. */
public interface UiPageRunner {
    /** Self-contained deterministic HTML page of the open screen tree. */
    String html();
}
