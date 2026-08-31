package worldline.extension;

/** Public entrypoint implemented by one external mod extension. */
public interface WorldlineExtension {
    void register(WorldlineExtensionRegistry registry);
}
