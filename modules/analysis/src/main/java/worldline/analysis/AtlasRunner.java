package worldline.analysis;

/** Neutral contract for rendering a seed terrain atlas page. */
public interface AtlasRunner {
    /** Self-contained deterministic HTML page for the requested seed area. */
    String render(AtlasRequest request);
}
