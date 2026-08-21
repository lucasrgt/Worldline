package worldline.test;

/** Base class for a collectable Java 8 Worldline spec. */
public abstract class WorldlineSpec {
    protected abstract void define();

    public final TestPlan collect() {
        Worldline.begin(getClass().getName());
        try { define(); return Worldline.end(); }
        catch (RuntimeException | Error failure) { Worldline.abort(); throw failure; }
    }
}
