package worldline.modloader.testkit;

/** SPI provider for a fresh controlled Forge 1.0.6 Beta 1.7.3 client. */
public final class ForgeTestRuntimeProvider extends LegacyTestRuntimeProvider {
    public ForgeTestRuntimeProvider() { super("forge"); }
}
