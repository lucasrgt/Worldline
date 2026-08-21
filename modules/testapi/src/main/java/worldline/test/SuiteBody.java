package worldline.test;

/** Collector callback used by describe and suite. */
@FunctionalInterface
public interface SuiteBody {
    void define();
}
