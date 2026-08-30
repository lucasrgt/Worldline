package worldline.modloader.testkit;

/** SPI provider for a fresh controlled ModLoader Beta 1.7.3 client. */
public final class ModLoaderTestRuntimeProvider extends LegacyTestRuntimeProvider {
    public ModLoaderTestRuntimeProvider() { super("modloader"); }
}
